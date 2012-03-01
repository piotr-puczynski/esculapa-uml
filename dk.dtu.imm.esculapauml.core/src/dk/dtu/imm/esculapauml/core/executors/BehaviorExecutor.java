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
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.BehavioralFeature;
import org.eclipse.uml2.uml.CallEvent;
import org.eclipse.uml2.uml.Class;
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

import dk.dtu.imm.esculapauml.core.checkers.BehaviorChecker;
import dk.dtu.imm.esculapauml.core.checkers.TransitionReplyChecker;
import dk.dtu.imm.esculapauml.core.executors.behaviors.TransitionChooser;
import dk.dtu.imm.esculapauml.core.executors.guards.GuardEvaluatorsFactory;
import dk.dtu.imm.esculapauml.core.utils.StateMachineUtils;

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
		super(checker, lifeline.getName(), (Class) checker.getCheckedObject().getContext());
		this.checker = checker;
		checkee = checker.getCheckedObject();
		this.lifeline = lifeline;
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
		activeConfiguration.clear();
		// enable initials
		for (Region r : checkee.getRegions()) {
			activeConfiguration.add(StateMachineUtils.getInitial(r));
		}
		// dispatch completion event
		TransitionReplyChecker trc = new TransitionReplyChecker(checker, null);
		trc.setAllowedToHaveReply(false);
		calculateEnabledTransitions(trc);
	}

	/**
	 * This function calculates the enabled transitions. It also fires the
	 * completion transition. It should be called after every run-to-completion
	 * step.
	 * 
	 */
	protected void calculateEnabledTransitions(TransitionReplyChecker trc) {
		enabledTransitions.clear();
		// check for dummy (empty) transitions and fire them
		boolean hasCompletionTransitions;
		// for loops detection
		ArrayList<Transition> completionTransitionTaken = new ArrayList<Transition>();
		do {
			hasCompletionTransitions = false;
			for (Vertex vertex : activeConfiguration) {
				EList<Transition> completionTransitionsOurgoingFromVertex = new BasicEList<Transition>();
				EList<Transition> satisfiedTransitions = GuardEvaluatorsFactory.getInstance().getGuardEvaluator(this, vertex).getTransitionsWithEnabledGuards();
				for (Transition transition : satisfiedTransitions) {
					if (transition.getTriggers().isEmpty()) {
						if (completionTransitionTaken.contains(transition)) {
							// check for loops created by completion transitions
							checker.addOtherProblem(Diagnostic.ERROR,
									"Transition is ill-formed. Loop has been detected during firing of completion transitions.", transition);
						} else {
							completionTransitionTaken.add(transition);
						}
						completionTransitionsOurgoingFromVertex.add(transition);
					}
				}
				if (!completionTransitionsOurgoingFromVertex.isEmpty()) {
					hasCompletionTransitions = true;
					trc.setNextTransition(TransitionChooser.choose(this, completionTransitionsOurgoingFromVertex));
					// fire completion transition
					fireTransition(trc);
					break;
				}
			}
			if (checker.hasErrors()) {
				return;
			}
		} while (hasCompletionTransitions);
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
		// TODO: add check of guards in these transitions
		EList<Transition> goodTransitions = getEnabledTransitionsForOperation(operation);
		if (goodTransitions.isEmpty()) {
			if (operationOwner.getMessageSort() == MessageSort.SYNCH_CALL_LITERAL) {
				checker.addOtherProblem(Diagnostic.ERROR, "StateMachine instance \"" + instanceSpecification.getName()
						+ "\" is not ready to respond to an event \"" + operation.getLabel() + "\".", operationOwner);
			} else {
				// warning, the machine is not able to process an operation
				// event
				checker.addOtherProblem(Diagnostic.WARNING, "StateMachine instance \"" + instanceSpecification.getName() + "\" is not ready for an event \""
						+ operation.getLabel() + "\". Event is lost.", operationOwner);
			}
		} else {
			TransitionReplyChecker trc = fireTransition(TransitionChooser.choose(this, goodTransitions));
			// dispatch completion event
			calculateEnabledTransitions(trc);
			return trc.getReply();
		}
		return null;
	}

	/**
	 * @param operation
	 * @return
	 */
	private EList<Transition> getEnabledTransitionsForOperation(Operation operation) {
		EList<Transition> result = new BasicEList<Transition>();
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
				OpaqueBehaviorExecutor obe = new OpaqueBehaviorExecutor(this, ftr);
				obe.prepare();
				obe.execute();
			}
		}

	}

}
