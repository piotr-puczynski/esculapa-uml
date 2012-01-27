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
package dk.dtu.imm.esculapauml.core.executors;

import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.flatten;
import static ch.lambdaj.Lambda.collect;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.CallEvent;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Region;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.Trigger;
import org.eclipse.uml2.uml.Vertex;

import dk.dtu.imm.esculapauml.core.checkers.AbstractStateMachineChecker;
import dk.dtu.imm.esculapauml.core.utils.StateMachineUtils;

/**
 * @author Piotr J. Puczynski
 * 
 */
public abstract class AbstractStateMachineExecutor<T extends AbstractStateMachineChecker> extends AbstractInstanceExecutor<T> {

	protected ArrayList<Vertex> activeConfiguration = new ArrayList<Vertex>();
	protected ArrayList<Transition> enabledTransitions = new ArrayList<Transition>();
	protected StateMachine checkee;

	/**
	 * @param checker
	 */
	public AbstractStateMachineExecutor(T checker) {
		super(checker);
		checkee = checker.getCheckedObject();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dk.dtu.imm.esculapauml.core.executors.ExecutorInterface#prepare()
	 */
	@Override
	public void prepare() {
		activeConfiguration.clear();
		// enable initials
		for (Region r : checkee.getRegions()) {
			activeConfiguration.add(StateMachineUtils.getInitial(r));
		}
		// calculate enabled transitions
		calculateEnabledTransitions();
	}

	/**
	 * 
	 */
	protected void calculateEnabledTransitions() {
		enabledTransitions.clear();
		// check for dummy (empty) transitions and fire them
		boolean hasDummies;
		do {
			hasDummies = false;
			for (Vertex vertex : activeConfiguration) {
				ArrayList<Transition> dummiesInVertex = new ArrayList<Transition>();
				for (Transition transition : vertex.getOutgoings()) {
					if (isGuardSatisfied(transition.getGuard()) && transition.getTriggers().size() == 0) {
						// TODO check for bad empty transitions (if source and
						// target are the same)
						dummiesInVertex.add(transition);

					}
				}
				if (dummiesInVertex.size() > 0) {
					hasDummies = true;
					// if there is only one dummy
					if (dummiesInVertex.size() == 1) {
						fireTransition(dummiesInVertex.get(0));
					} else {
						// TODO error: state machine not deterministic
						// for now we take the first transition
						fireTransition(dummiesInVertex.get(0));
					}
					break;
				}
			}
		} while (hasDummies);
		// add outgoing transitions of active states
		for (Vertex vertex : activeConfiguration) {
			enabledTransitions.addAll(vertex.getOutgoings());
		}

	}

	/**
	 * @param operation
	 */
	public void runOperation(Element operationOwner, Operation operation) {
		List<Transition> goodTransitions = getEnabledTransitionsForOperation(operation);
		if (goodTransitions.size() > 0) {
			if (isGuardSatisfied(goodTransitions.get(0).getGuard())) {
				fireTransition(goodTransitions.get(0));
				calculateEnabledTransitions();
			}
		} else {
			// warning, the machine is not able to process an operation event
			checker.addOtherProblem(Diagnostic.WARNING, "StateMachine instance \"" + instanceSpecification.getName() + "\" is not ready for an event \""
					+ operation.getLabel() + "\".", operationOwner);
		}
	}

	/**
	 * @param operation
	 * @return
	 */
	private List<Transition> getEnabledTransitionsForOperation(Operation operation) {
		ArrayList<Transition> result = new ArrayList<Transition>();
		for (Transition transition : enabledTransitions) {
			List<Trigger> triggers = filter(having(on(Trigger.class).getEvent(), is(CallEvent.class)), transition.getTriggers());
			for (Trigger trigger : triggers) {
				if (((CallEvent) trigger.getEvent()).getOperation() == operation) {
					result.add(transition);
					break;
				}
			}
		}
		return result;
	}

	/**
	 * @param operation
	 * @return
	 */
	public boolean hasTriggerForOperation(Operation operation) {
		List<Trigger> triggers = flatten(collect(enabledTransitions, on(Transition.class).getTriggers()));
		triggers = filter(having(on(Trigger.class).getEvent(), is(CallEvent.class)), triggers);
		for (Trigger trigger : triggers) {
			if (((CallEvent) trigger.getEvent()).getOperation() == operation) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Check if guard is satisfied
	 * 
	 * @param t
	 * @return
	 */
	protected boolean isGuardSatisfied(Constraint guard) {
		if (null == guard) {
			// no guard
			return true;
		}
		// TODO implement guards
		return true;
	}

	/**
	 * Fire transition
	 * 
	 * @param transition
	 */
	protected void fireTransition(Transition transition) {
		// remove the source vertex from active configuration
		Vertex source = transition.getSource();
		activeConfiguration.remove(source);
		// add target vertex to active configuration
		Vertex target = transition.getTarget();
		activeConfiguration.add(target);

		// run effect of transition
		runEffect(transition.getEffect());

	}

	/**
	 * executes an effect
	 * 
	 * @param effect
	 */
	protected void runEffect(Behavior effect) {
		// TODO Auto-generated method stub

	}

}
