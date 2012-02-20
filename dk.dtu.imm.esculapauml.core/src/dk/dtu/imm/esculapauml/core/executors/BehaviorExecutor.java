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
import org.eclipse.uml2.uml.FunctionBehavior;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageSort;
import org.eclipse.uml2.uml.OpaqueBehavior;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Region;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.Trigger;
import org.eclipse.uml2.uml.ValueSpecification;
import org.eclipse.uml2.uml.Vertex;
import org.hamcrest.Matcher;

import ch.lambdaj.function.matcher.Predicate;

import dk.dtu.imm.esculapauml.core.checkers.BehaviorChecker;
import dk.dtu.imm.esculapauml.core.checkers.TransitionReplyChecker;
import dk.dtu.imm.esculapauml.core.utils.StateMachineUtils;
import dk.dtu.imm.esculapauml.core.validators.OCLValidator;
import dk.dtu.imm.esculapauml.core.validators.Validator;
import dk.dtu.imm.esculapauml.core.validators.ValidatorsFactory;

/**
 * @author Piotr J. Puczynski
 * 
 */
public class BehaviorExecutor extends AbstractInstanceExecutor {

	protected Lifeline lifeline;
	protected ArrayList<Vertex> activeConfiguration = new ArrayList<Vertex>();
	protected ArrayList<Transition> enabledTransitions = new ArrayList<Transition>();
	protected StateMachine checkee;
	protected BehaviorChecker checker;

	/**
	 * @param checker
	 */
	public BehaviorExecutor(BehaviorChecker checker, Lifeline lifeline) {
		super(checker);
		this.checker = checker;
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
		TransitionReplyChecker trc = new TransitionReplyChecker(checker, null);
		trc.setAllowedToHaveReply(false);
		calculateEnabledTransitions(trc);
	}

	/**
	 * 
	 */
	protected void calculateEnabledTransitions(TransitionReplyChecker trc) {
		enabledTransitions.clear();
		// check for dummy (empty) transitions and fire them
		boolean hasDummies;
		ArrayList<Transition> dummiesTaken = new ArrayList<Transition>();
		do {
			hasDummies = false;
			for (Vertex vertex : activeConfiguration) {
				ArrayList<Transition> dummiesInVertex = new ArrayList<Transition>();
				for (Transition transition : vertex.getOutgoings()) {
					if (isGuardSatisfied(transition.getGuard()) && transition.getTriggers().size() == 0) {
						if (dummiesTaken.contains(transition)) {
							// check for bad empty transitions (if source and
							// target are the same)
							checker.addOtherProblem(Diagnostic.ERROR, "Transition is ill-formed.", transition);
						} else {
							dummiesTaken.add(transition);
						}
						dummiesInVertex.add(transition);

					}
				}
				if (dummiesInVertex.size() > 0) {
					hasDummies = true;
					// if there is only one dummy
					if (dummiesInVertex.size() == 1) {
						trc.setNextTransition(dummiesInVertex.get(0));
						fireTransition(trc);
					} else {
						// error: conflicting transitions
						checker.addOtherProblem(Diagnostic.ERROR, "Conflicting transitions.", dummiesInVertex.toArray());
					}
					break;
				}
			}
		} while (hasDummies && !checker.hasErrors());
		// add outgoing transitions of active states
		logger.debug(checkee.getLabel() + "[" + instanceName + "]: active states:");
		for (Vertex vertex : activeConfiguration) {
			enabledTransitions.addAll(vertex.getOutgoings());
			logger.debug(checkee.getLabel() + "[" + instanceName + "]: " + vertex.getLabel());
		}
		logger.debug(checkee.getLabel() + "[" + instanceName + "]: active states end");

	}

	/**
	 * Operation used to trigger event in state machine.
	 * 
	 * @param operation
	 */
	public ValueSpecification runOperation(Message operationOwner, Operation operation) {
		logger.debug(checkee.getLabel() + "[" + instanceName + "]: event arrived: " + operation.getLabel());
		List<Transition> goodTransitions = getEnabledTransitionsForOperation(operation);
		if (goodTransitions.size() > 0) {
			goodTransitions = filterTransitionsWithValidGuards(goodTransitions);
			if (goodTransitions.size() == 1) {
				TransitionReplyChecker trc = fireTransition(goodTransitions.get(0));
				calculateEnabledTransitions(trc);
				return trc.getReply();
			} else if (goodTransitions.size() == 0) {
				checker.addOtherProblem(Diagnostic.WARNING, "StateMachine instance \"" + instanceSpecification.getName() + "\" cannot process an event \""
						+ operation.getLabel() + "\" because guards are not satisfied. Event is lost.", operationOwner);
			} else {
				checker.addOtherProblem(Diagnostic.ERROR, "StateMachine instance \"" + instanceSpecification.getName()
						+ "\" contains conflicting transitions and cannot process an event \"" + operation.getLabel() + "\".", operationOwner);
				checker.addOtherProblem(Diagnostic.ERROR, "Conflicting transitions.", goodTransitions.toArray());
			}
		} else {
			if (operationOwner.getMessageSort() == MessageSort.SYNCH_CALL_LITERAL) {
				checker.addOtherProblem(Diagnostic.ERROR, "StateMachine instance \"" + instanceSpecification.getName()
						+ "\" is not ready to respond to an event \"" + operation.getLabel() + "\".", operationOwner);
			} else {
				// warning, the machine is not able to process an operation
				// event
				checker.addOtherProblem(Diagnostic.WARNING, "StateMachine instance \"" + instanceSpecification.getName() + "\" is not ready for an event \""
						+ operation.getLabel() + "\". Event is lost.", operationOwner);
			}
		}
		return null;
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
		Validator validator = ValidatorsFactory.getInstance().getValidatorFor(this, guard);
		if (null == validator) {
			// we do not have validator for this type of constraint
			checker.addOtherProblem(Diagnostic.WARNING, "Guard on the transition is not supported by EsculapaUML.", guard.getOwner());
			return true;
		}
		return validator.validateConstraint();
	}

	/**
	 * Fire transition with default FTR.
	 * 
	 * @param transition
	 * @return
	 */
	protected TransitionReplyChecker fireTransition(Transition transition) {
		TransitionReplyChecker ftr = new TransitionReplyChecker(checker, transition);
		return fireTransition(ftr);
	}

	/**
	 * Fire transition in FTR.
	 * 
	 * @param transition
	 * @return
	 */
	protected TransitionReplyChecker fireTransition(TransitionReplyChecker ftr) {
		logger.info(checkee.getLabel() + "[" + instanceName + "]: firing transition: " + ftr.getCheckedObject().getLabel());
		// remove the source vertex from active configuration
		Vertex source = ftr.getCheckedObject().getSource();
		activeConfiguration.remove(source);
		// add target vertex to active configuration
		Vertex target = ftr.getCheckedObject().getTarget();
		activeConfiguration.add(target);

		// run effect of transition
		runEffect(ftr);
		return ftr;
	}

	/**
	 * executes an effect
	 * 
	 * @param ftr
	 */
	protected void runEffect(TransitionReplyChecker ftr) {
		Behavior effect = ftr.getCheckedObject().getEffect();
		if (null != effect) {
			if (effect instanceof FunctionBehavior) {
				BehavioralFeature bf = effect.getSpecification();
				if (bf instanceof Operation) {
					checker.getSystemState().getMainExecutor().behaviorExecution(this, (Operation) bf);
				} else {
					// this shouldn't happen as function behavior should be an
					// operation
					ftr.addProblem(Diagnostic.ERROR, "Using FunctionBehavior effect on transition without defining correct specification.");
				}
			} else if (effect instanceof OpaqueBehavior) {
				OpaqueBehaviorExecutor obe = new OpaqueBehaviorExecutor(checker, instanceSpecification, ftr);
				obe.prepare();
				obe.execute();
			}
		}

	}

}
