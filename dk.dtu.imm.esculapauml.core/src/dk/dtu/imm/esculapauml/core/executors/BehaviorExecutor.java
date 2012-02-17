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
	 * Class used only internally to exchange a result of firing a transition.
	 * @author Piotr J. Puczynski
	 *
	 */
	protected class FireTransitionResult {
		private ValueSpecification reply = null;
		private boolean allowedToHaveReply = true;
		Transition transition;
		/**
		 * @return the transition
		 */
		public Transition getTransition() {
			return transition;
		}

		/**
		 * @param transition
		 */
		public FireTransitionResult(Transition transition) {
			super();
			this.transition = transition;
		}

		/**
		 * @return the reply
		 */
		public ValueSpecification getReply() {
			return reply;
		}

		/**
		 * @param reply
		 *            the reply to set
		 */
		public void setReply(ValueSpecification reply) {
			if (isAllowedToHaveReply()) {
				this.reply = reply;
			} else {
				checker.addOtherProblem(Diagnostic.ERROR, "The transition is not allowed to have reply.", transition);
			}
			
		}

		/**
		 * @return the allowedToHaveReply
		 */
		public boolean isAllowedToHaveReply() {
			return allowedToHaveReply;
		}

		/**
		 * @param allowedToHaveReply
		 *            the allowedToHaveReply to set
		 */
		public void setAllowedToHaveReply(boolean allowedToHaveReply) {
			this.allowedToHaveReply = allowedToHaveReply;
		}
	}

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
						FireTransitionResult ftr = new FireTransitionResult(dummiesInVertex.get(0));
						ftr.setAllowedToHaveReply(false);
						fireTransition(ftr);
					} else {
						// error: conflicting transitions
						checker.addOtherProblem(Diagnostic.ERROR, "Conflicting transitions.", dummiesInVertex.toArray());
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
				FireTransitionResult ftr = fireTransition(goodTransitions.get(0));
				calculateEnabledTransitions();
				return ftr.getReply();
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
		// TODO implement guards
		return true;
	}
	
	/**
	 * Fire transition with default FTR.
	 * @param transition
	 * @return
	 */
	protected FireTransitionResult fireTransition(Transition transition) {
		FireTransitionResult ftr = new FireTransitionResult(transition);
		return fireTransition(ftr);
	}

	/**
	 * Fire transition in FTR.
	 * 
	 * @param transition
	 * @return
	 */
	protected FireTransitionResult fireTransition(FireTransitionResult ftr) {
		logger.info(checkee.getLabel() + "[" + instanceName + "]: firing transition: " + ftr.getTransition().getLabel());
		// remove the source vertex from active configuration
		Vertex source = ftr.getTransition().getSource();
		activeConfiguration.remove(source);
		// add target vertex to active configuration
		Vertex target = ftr.getTransition().getTarget();
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
	protected void runEffect(FireTransitionResult ftr) {
		Behavior effect = ftr.getTransition().getEffect();
		if (null != effect) {
			if (effect instanceof FunctionBehavior) {
				BehavioralFeature bf = effect.getSpecification();
				if (bf instanceof Operation) {
					checker.getSystemState().getMainExecutor().behaviorExecution(this, (Operation) bf);
				} else {
					// this shouldn't happen as function behavior should be an
					// operation
					checker.addOtherProblem(Diagnostic.ERROR, "Using FunctionBehavior effect on transition without defining correct specification.", ftr.getTransition());
				}
			} else if (effect instanceof OpaqueBehavior) {
				OpaqueBehaviorExecutor obe = new OpaqueBehaviorExecutor(checker, instanceSpecification, ftr.getTransition(), (OpaqueBehavior) effect);
				obe.prepare();
				obe.execute();
				if (obe.hasReply()) {
					ftr.setReply(obe.getReply());
				}
			}
		}

	}

}
