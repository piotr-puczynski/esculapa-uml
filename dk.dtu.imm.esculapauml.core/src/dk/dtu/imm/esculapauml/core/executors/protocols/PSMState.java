/******************************************************************************
 * Copyright (c) 2011, 2012 Piotr J. Puczynski (DTU Informatics).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Piotr J. Puczynski (DTU Informatics) - initial API and implementation 
 *    
 ****************************************************************************/
package dk.dtu.imm.esculapauml.core.executors.protocols;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.CallEvent;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.ProtocolStateMachine;
import org.eclipse.uml2.uml.Region;
import org.eclipse.uml2.uml.State;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.Trigger;
import org.eclipse.uml2.uml.Vertex;

import ch.lambdaj.function.matcher.Predicate;

import dk.dtu.imm.esculapauml.core.checkers.Checker;
import dk.dtu.imm.esculapauml.core.executors.TransitionChooser;
import dk.dtu.imm.esculapauml.core.executors.guards.GuardEvaluator;
import dk.dtu.imm.esculapauml.core.executors.guards.GuardEvaluatorsFactory;
import dk.dtu.imm.esculapauml.core.utils.StateMachineUtils;

/**
 * Represents one possible state in PSM. Used for executing all possible states
 * in PSM.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class PSMState {

	private Set<Vertex> activeConfiguration;
	private PathsChecker pathsChecker;
	private Checker checker;
	private ProtocolStateMachine protocol;
	private boolean terminated = false;
	protected static Predicate<Transition> isCompletionTransition = new Predicate<Transition>() {
		public boolean apply(Transition item) {
			return item.getTriggers().isEmpty();
		}
	};

	/**
	 * @param pathsChecker
	 * @param activeConfiguration
	 */
	public PSMState(PathsChecker pathsChecker, Set<Vertex> activeConfiguration) {
		this.activeConfiguration = new HashSet<Vertex>(activeConfiguration);
		this.pathsChecker = pathsChecker;
		checker = pathsChecker.getExecutor().getChecker();
		protocol = pathsChecker.getProtocolVerifier().getProtocol();
		recalculateActiveState();
	}

	/**
	 * @param pathsChecker
	 * @param activeConfiguration
	 * @param transition
	 */
	public PSMState(PathsChecker pathsChecker, Set<Vertex> activeConfiguration, Transition transition) {
		this.activeConfiguration = new HashSet<Vertex>(activeConfiguration);
		this.pathsChecker = pathsChecker;
		checker = pathsChecker.getExecutor().getChecker();
		protocol = pathsChecker.getProtocolVerifier().getProtocol();
		fireTransition(transition);
		recalculateActiveState();
	}

	/**
	 * This function calculates the enabled transitions. It also fires the
	 * completion transition. It should be called after every run-to-completion
	 * step.
	 * 
	 */
	protected void recalculateActiveState() {
		// check for empty transitions and fire them
		boolean hasCompletionTransitions;
		// for loops detection
		ArrayList<Transition> completionTransitionTaken = new ArrayList<Transition>();
		// this is to avoid looping of two self completion transition on PSM
		// state
		Map<Transition, Set<Vertex>> completionTransitionNotTaken = new HashMap<Transition, Set<Vertex>>();

		do {
			hasCompletionTransitions = false;
			for (Vertex vertex : activeConfiguration) {
				GuardEvaluator ge = GuardEvaluatorsFactory.getGuardEvaluator(pathsChecker.getExecutor(), vertex);
				ge.setPreconditions(isCompletionTransition);
				EList<Transition> satisfiedCompletionTransitions = ge.getTransitionsWithEnabledGuards();
				Transition transitionToTake = TransitionChooser.choose(pathsChecker.getExecutor(), satisfiedCompletionTransitions);
				if (satisfiedCompletionTransitions.size() > 1) {
					for (Transition transition : satisfiedCompletionTransitions) {
						if (transition != transitionToTake) {
							completionTransitionNotTaken.put(transition, new HashSet<Vertex>(activeConfiguration));
						}
					}
				}
				if (null != transitionToTake) {

					// detection of loops created by completion transitions
					if (completionTransitionTaken.contains(transitionToTake)) {
						checker.addOtherProblem(Diagnostic.ERROR, "Transition is ill-formed. Loop has been detected during firing of completion transitions.",
								transitionToTake);
						break;
					} else {
						completionTransitionTaken.add(transitionToTake);
					}
					hasCompletionTransitions = true;
					// fire completion transition
					fireTransition(transitionToTake);
					break;
				}

			}
			if (checker.hasErrors()) {
				return;
			}
		} while (hasCompletionTransitions);
		
		for (Entry<Transition, Set<Vertex>> entry : completionTransitionNotTaken.entrySet()) {
			pathsChecker.createNewState(entry.getValue(), entry.getKey());
		}

	}

	/**
	 * Fire transition in FTR.
	 * 
	 * @param transition
	 */
	protected void fireTransition(Transition transition) {
		Vertex source = transition.getSource();
		Vertex target = transition.getTarget();
		if (target instanceof State) {
			State targetState = (State) target;
			// composite states
			if (!targetState.getRegions().isEmpty()) {
				enterCompositeState(targetState);
			}
		}
		if (source.getContainer() != target.getContainer() && source instanceof State && target instanceof State) {
			// we crossed border of composite state
			// check if the source contains target
			// calculate LCA
			State compositeSource = (State) source;
			State compositeTarget = (State) target;
			Namespace lca = protocol.LCA(compositeSource, compositeTarget);
			// TODO: add removal of nested composite states, then lca will not
			// be null
			if (null == lca) {
				if (protocol.ancestor(compositeSource, compositeTarget)) {
					// we enter composite state
					if (null != compositeTarget.getContainer().getState()) {
						enterCompositeState(compositeTarget.getContainer().getState());
					}
				} else {
					// we leave composite state
					if (null != compositeTarget.getContainer().getState()) {
						exitCompositeState(compositeSource.getContainer().getState());
					}
				}
			}

		}

		// remove the source vertex from active configuration
		activeConfiguration.remove(source);
		// add target vertex to active configuration
		activeConfiguration.add(target);
	}

	/**
	 * @param state
	 */
	private void exitCompositeState(State compositeState) {
		activeConfiguration.remove(compositeState);
		Iterator<Vertex> it = activeConfiguration.iterator();
		while (it.hasNext()) {
			Vertex vertex = it.next();
			if (vertex.getContainer().getState() != null && protocol.ancestor(compositeState, vertex.getContainer().getState())) {
				it.remove();
			}
		}

	}

	/**
	 * @param targetState
	 */
	private void enterCompositeState(State compositeState) {
		activeConfiguration.add(compositeState);
		for (Region r : compositeState.getRegions()) {
			activeConfiguration.add(StateMachineUtils.getInitial(r));
		}
	}
	
	/**
	 * Executes operation call and calculates precondition for operation.
	 * 
	 * @param operation
	 */
	public void preCall(final Operation operation) {
		Predicate<Transition> hasTriggerForOperation = new Predicate<Transition>() {
			public boolean apply(Transition item) {
				for (Trigger t : item.getTriggers()) {
					if (t.getEvent() instanceof CallEvent) {
						if (((CallEvent) t.getEvent()).getOperation() == operation) {
							return true;
						}
					}
				}
				return false;
			}
		};
		EList<Transition> transitions = new BasicEList<Transition>();
		for (Vertex vertex : activeConfiguration) {
			GuardEvaluator ge = GuardEvaluatorsFactory.getGuardEvaluator(pathsChecker.getExecutor(), vertex);
			ge.setPreconditions(hasTriggerForOperation);
			transitions.addAll(ge.getTransitionsWithEnabledGuards());
		}

		Transition transitionToTake = TransitionChooser.choose(pathsChecker.getExecutor(), transitions);

		if (transitions.size() > 1) {
			for (Transition transition : transitions) {
				if (transition != transitionToTake) {
					pathsChecker.createNewState(this, transition);
				}
			}
		}

		if (null == transitionToTake) {
			setTerminated(true);
		} else {
			fireTransition(transitionToTake);
			recalculateActiveState();
		}

	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((activeConfiguration == null) ? 0 : activeConfiguration.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PSMState other = (PSMState) obj;
		if (activeConfiguration == null) {
			if (other.activeConfiguration != null)
				return false;
		} else if (!activeConfiguration.equals(other.activeConfiguration))
			return false;
		return true;
	}

	/**
	 * @return the terminated
	 */
	public boolean isTerminated() {
		return terminated;
	}
	
	/**
	 * @return the activeConfiguration
	 */
	public Set<Vertex> getActiveConfiguration() {
		return activeConfiguration;
	}

	/**
	 * @param terminated
	 *            the terminated to set
	 */
	public void setTerminated(boolean terminated) {
		this.terminated = terminated;
	}

	
}
