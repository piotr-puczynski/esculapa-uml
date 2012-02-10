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

import static ch.lambdaj.Lambda.collect;
import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.flatten;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.BehavioralFeature;
import org.eclipse.uml2.uml.CallEvent;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.FunctionBehavior;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Region;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.Trigger;
import org.eclipse.uml2.uml.Vertex;
import org.hamcrest.Matcher;

import ch.lambdaj.function.matcher.Predicate;

import dk.dtu.imm.esculapauml.core.checkers.BehaviorChecker;
import dk.dtu.imm.esculapauml.core.utils.StateMachineUtils;

/**
 * @author Piotr J. Puczynski
 * 
 */
public class BehaviorExecutor extends AbstractInstanceExecutor<BehaviorChecker> {

	protected Lifeline lifeline;
	protected ArrayList<Vertex> activeConfiguration = new ArrayList<Vertex>();
	protected ArrayList<Transition> enabledTransitions = new ArrayList<Transition>();
	protected StateMachine checkee;

	/**
	 * @param checker
	 */
	public BehaviorExecutor(BehaviorChecker checker, Lifeline lifeline) {
		super(checker);
		checkee = checker.getCheckedObject();
		this.lifeline = lifeline;
		instanceName = lifeline.getName();
		logger = Logger.getLogger(BehaviorExecutor.class);
		logger.debug(checkee.getLabel() + "[" + instanceName + "]: executor created");
	}

	/**
	 * @return the lifeline
	 */
	public Lifeline getLifeline() {
		return lifeline;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dk.dtu.imm.esculapauml.core.executors.ExecutorInterface#prepare()
	 */
	@Override
	public void prepare() {
		logger.debug(checkee.getLabel() + "[" + instanceName + "]: loading initial conf");
		instanceSpecification.getClassifiers().add(checker.getCheckedObject().getContext());
		instanceSpecification.setName(instanceName);
		// add to instance package
		checker.getSystemState().getInstancePackage().getPackagedElements().add(instanceSpecification);
		checker.getSystemState().addGeneratedElement(instanceSpecification);

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
		logger.debug(checkee.getLabel() + "[" + instanceName + "]: active states:");
		for (Vertex vertex : activeConfiguration) {
			enabledTransitions.addAll(vertex.getOutgoings());
			logger.debug(checkee.getLabel() + "[" + instanceName + "]: " + vertex.getLabel());
		}
		logger.debug(checkee.getLabel() + "[" + instanceName + "]: active states end");

	}

	/**
	 * @param operation
	 */
	public void runOperation(Element operationOwner, Operation operation, boolean executionRequired) {
		logger.debug(checkee.getLabel() + "[" + instanceName + "]: event arrived: " + operation.getLabel());
		List<Transition> goodTransitions = getEnabledTransitionsForOperation(operation);
		if (goodTransitions.size() > 0) {
			goodTransitions = filterTransitionsWithValidGuards(goodTransitions);
			if (goodTransitions.size() == 1) {
				fireTransition(goodTransitions.get(0));
				calculateEnabledTransitions();
			} else if (goodTransitions.size() == 0) {
				checker.addOtherProblem(Diagnostic.WARNING, "StateMachine instance \"" + instanceSpecification.getName() + "\" cannot process an event \""
						+ operation.getLabel() + "\" because guards are not satisfied. Event is lost.", operationOwner);
			} else {
				checker.addOtherProblem(Diagnostic.ERROR, "StateMachine instance \"" + instanceSpecification.getName()
						+ "\" contains conflicting transitions and cannot process an event \"" + operation.getLabel() + "\".", operationOwner);
				checker.addOtherProblem(Diagnostic.ERROR, "Conflicting transitions.", goodTransitions.toArray());
			}
		} else {
			if (executionRequired) {
				checker.addOtherProblem(Diagnostic.ERROR, "StateMachine instance \"" + instanceSpecification.getName()
						+ "\" is not ready to respond to an event \"" + operation.getLabel() + "\".", operationOwner);
			} else {
				// warning, the machine is not able to process an operation
				// event
				checker.addOtherProblem(Diagnostic.WARNING, "StateMachine instance \"" + instanceSpecification.getName() + "\" is not ready for an event \""
						+ operation.getLabel() + "\". Event is lost.", operationOwner);
			}
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
	private List<Transition> filterTransitionsWithValidGuards(List<Transition> transitions) {
		Matcher<Transition> satisfied = new Predicate<Transition>() {
			public boolean apply(Transition item) {
				return isGuardSatisfied(item.getGuard());
			}
		};
		return filter(satisfied, transitions);
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
		logger.info(checkee.getLabel() + "[" + instanceName + "]: firing transition: " + transition.getLabel());
		// remove the source vertex from active configuration
		Vertex source = transition.getSource();
		activeConfiguration.remove(source);
		// add target vertex to active configuration
		Vertex target = transition.getTarget();
		activeConfiguration.add(target);

		// run effect of transition
		runEffect(transition);

	}

	/**
	 * executes an effect
	 * 
	 * @param effect
	 */
	protected void runEffect(Transition transition) {
		Behavior effect = transition.getEffect();
		if (null != effect) {
			if (effect instanceof FunctionBehavior) {
				BehavioralFeature bf = effect.getSpecification();
				if (bf instanceof Operation) {
					checker.getSystemState().getMainExecutor().behaviorExecution(this, (Operation) bf);
				} else {
					// this shouldn't happen as function behavior should be an
					// operation
					checker.addOtherProblem(Diagnostic.ERROR, "Using FunctionBehavior effect on transition without defining correct specification.", transition);
				}
			}
		}

	}

}
