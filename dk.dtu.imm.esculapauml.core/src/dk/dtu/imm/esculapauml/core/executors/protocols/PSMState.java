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
import org.eclipse.uml2.uml.OpaqueBehavior;
import org.eclipse.uml2.uml.OpaqueExpression;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.CallEvent;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.ProtocolStateMachine;
import org.eclipse.uml2.uml.ProtocolTransition;
import org.eclipse.uml2.uml.Region;
import org.eclipse.uml2.uml.State;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.Trigger;
import org.eclipse.uml2.uml.Vertex;

import ch.lambdaj.function.matcher.Predicate;

import dk.dtu.imm.esculapauml.core.checkers.Checker;
import dk.dtu.imm.esculapauml.core.executors.coordination.EsculapaCallEvent;
import dk.dtu.imm.esculapauml.core.executors.coordination.EsculapaReplyEvent;
import dk.dtu.imm.esculapauml.core.executors.guards.GuardEvaluator;
import dk.dtu.imm.esculapauml.core.executors.guards.GuardEvaluatorsFactory;
import dk.dtu.imm.esculapauml.core.utils.StateMachineUtils;
import dk.dtu.imm.esculapauml.core.validators.Validator;
import dk.dtu.imm.esculapauml.core.validators.ValidatorsFactory;

/**
 * Represents one possible state in PSM. Used for executing all possible states
 * in PSM.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class PSMState {

	private Set<Vertex> activeConfiguration;
	private PathsAnalyzer pathsAnalyzer;
	private Checker checker;
	private ProtocolStateMachine protocol;
	private boolean terminated = false;
	// holds id and taken transitions for synchronous events to evaluate
	// post-conditions
	private Map<Long, Transition> synchronousEvents = new HashMap<Long, Transition>();
	protected static Predicate<Transition> isCompletionTransition = new Predicate<Transition>() {
		public boolean apply(Transition item) {
			return item.getTriggers().isEmpty();
		}
	};

	/**
	 * Constructor used during initialization.
	 * 
	 * @param pathsAnalyzer
	 * @param activeConfiguration
	 */
	public PSMState(PathsAnalyzer pathsAnalyzer, Set<Vertex> activeConfiguration) {
		this.activeConfiguration = new HashSet<Vertex>(activeConfiguration);
		this.pathsAnalyzer = pathsAnalyzer;
		checker = pathsAnalyzer.getExecutor().getChecker();
		protocol = pathsAnalyzer.getProtocolVerifier().getProtocol();
		recalculateActiveState();
	}

	/**
	 * Copy constructor.
	 */
	public PSMState(PSMState parent) {
		copyFields(parent);
	}

	/**
	 * Internal copy constructor method.
	 * 
	 * @param parent
	 */
	private void copyFields(PSMState parent) {
		activeConfiguration = new HashSet<Vertex>(parent.getActiveConfiguration());
		synchronousEvents.putAll(parent.synchronousEvents);
		pathsAnalyzer = parent.pathsAnalyzer;
		checker = parent.checker;
		protocol = parent.protocol;
	}

	/**
	 * Constructor used during creating path for completion transition.
	 * 
	 * @param parent
	 * @param transition
	 */
	public PSMState(PSMState parent, Transition transition) {
		copyFields(parent);
		fireTransition(transition);
		recalculateActiveState();
	}

	/**
	 * Constructor used during creating path for event transition.
	 * 
	 * @param parent
	 * @param event
	 * @param transition
	 */
	public PSMState(PSMState parent, EsculapaCallEvent event, Transition transition) {
		copyFields(parent);
		fireEvent(transition, event);
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
		Map<Transition, PSMState> completionTransitionNotTaken = new HashMap<Transition, PSMState>();

		do {
			hasCompletionTransitions = false;
			for (Vertex vertex : activeConfiguration) {
				GuardEvaluator ge = GuardEvaluatorsFactory.getGuardEvaluator(pathsAnalyzer.getExecutor(), vertex);
				ge.setPreconditions(isCompletionTransition);
				EList<Transition> satisfiedCompletionTransitions = ge.getTransitionsWithEnabledGuards();

				Iterator<Transition> it = satisfiedCompletionTransitions.iterator();
				// validate post-conditions
				while (it.hasNext()) {
					Transition trans = it.next();
					if (!validatePostCondition(trans, false)) {
						it.remove();
					}
				}

				Transition transitionToTake = satisfiedCompletionTransitions.isEmpty() ? null : satisfiedCompletionTransitions.get(0);
				if (satisfiedCompletionTransitions.size() > 1) {
					for (Transition transition : satisfiedCompletionTransitions) {
						if (transition != transitionToTake) {
							completionTransitionNotTaken.put(transition, new PSMState(this));
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

		for (Entry<Transition, PSMState> entry : completionTransitionNotTaken.entrySet()) {
			pathsAnalyzer.createNewState(entry.getValue(), entry.getKey());
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
	 * @param event
	 */
	public void preCall(final Operation operation, EsculapaCallEvent event) {
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
			GuardEvaluator ge = GuardEvaluatorsFactory.getGuardEvaluator(pathsAnalyzer.getExecutor(), vertex);
			ge.setPreconditions(hasTriggerForOperation);
			transitions.addAll(ge.getTransitionsWithEnabledGuards());
		}

		Transition transitionToTake = transitions.isEmpty() ? null : transitions.get(0);

		if (transitions.size() > 1) {
			for (Transition transition : transitions) {
				if (transition != transitionToTake) {
					pathsAnalyzer.createNewState(this, event, transition);
				}
			}
		}

		if (null == transitionToTake) {
			setTerminated(true);
		} else {
			fireEvent(transitionToTake, event);
		}

	}

	/**
	 * Fires given event by firing the chosen transition.
	 * 
	 * @param transitionToTake
	 * @param event
	 */
	private void fireEvent(Transition transitionToTake, EsculapaCallEvent event) {
		if (event.isSynchronousCall()) {
			synchronousEvents.put(event.getSequenceId(), transitionToTake);
		} else {
			validatePostCondition(transitionToTake, true);
			if (!isTerminated()) {
				fireTransition(transitionToTake);
				recalculateActiveState();
			}
		}
	}

	/**
	 * Finishes execution of operation call and validates postcondition for
	 * operation.
	 * 
	 * @param event
	 */
	public void postCall(EsculapaReplyEvent event) {
		Transition transition = synchronousEvents.get(event.getInitiatingCallSequenceNumber());
		if (null == transition) {
			// should never happen
			setTerminated(true);
		} else {
			synchronousEvents.remove(event.getInitiatingCallSequenceNumber());
			validatePostCondition(transition, true);
			if (!isTerminated()) {
				fireTransition(transition);
				recalculateActiveState();
			}
		}
	}

	/**
	 * Validates post-condition (if any) on given transition. In case
	 * post-condition is not fulfilled the state is terminated.
	 * 
	 * @param transition
	 */
	private boolean validatePostCondition(Transition transition, boolean setTerminatedIfNotValid) {
		Validator validator = null;
		if (transition instanceof ProtocolTransition) {
			validator = ValidatorsFactory.getInstance().getValidatorFor(pathsAnalyzer.getExecutor(), ((ProtocolTransition) transition).getPostCondition());
		} else {
			// for Topcased that does not support ProtocolTransition in diagrams
			// we will treat effect as postcondition
			Constraint c = UMLFactory.eINSTANCE.createConstraint();
			if (transition.getEffect() instanceof OpaqueBehavior) {
				c.setSpecification(getOCLFromEffect((OpaqueBehavior) transition.getEffect()));
			}
			validator = ValidatorsFactory.getInstance().getValidatorFor(pathsAnalyzer.getExecutor(), c);
			// necessary to set error context as evaluation is on element in the
			// model
			validator.setErrorContext(transition);
		}
		if (null != validator) {
			if (!validator.validateConstraint()) {
				if (setTerminatedIfNotValid) {
					setTerminated(true);
				}
				return false;
			}
		}
		return true;
	}

	/**
	 * Internal method to get OCL post-condition from effect on the transition.
	 * It is used to support tools (Topcased) that are not able to handle
	 * postconditions in diagrams.
	 * 
	 * @param ob
	 * @return
	 */
	private OpaqueExpression getOCLFromEffect(OpaqueBehavior ob) {
		String ocl = ob.getName().trim();
		if (ocl.startsWith("[") && ocl.endsWith("]")) {
			ocl = ocl.substring(1, ocl.length() - 1).trim();
			if (!ocl.isEmpty()) {
				OpaqueExpression oe = UMLFactory.eINSTANCE.createOpaqueExpression();
				oe.getBodies().add(ocl);
				oe.getLanguages().add("ocl");
				return oe;
			}
		}
		return null;
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
		result = prime * result + ((synchronousEvents == null) ? 0 : synchronousEvents.hashCode());
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
		if (synchronousEvents == null) {
			if (other.synchronousEvents != null)
				return false;
		} else if (!synchronousEvents.equals(other.synchronousEvents))
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
