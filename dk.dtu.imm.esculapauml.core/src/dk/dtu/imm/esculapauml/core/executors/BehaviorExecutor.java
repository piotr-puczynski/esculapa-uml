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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.UniqueEList;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.CallEvent;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.InstanceSpecification;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.OpaqueBehavior;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterDirectionKind;
import org.eclipse.uml2.uml.Region;
import org.eclipse.uml2.uml.Slot;
import org.eclipse.uml2.uml.State;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.Trigger;
import org.eclipse.uml2.uml.ValueSpecification;
import org.eclipse.uml2.uml.Vertex;
import org.eclipse.uml2.uml.VisibilityKind;

import ch.lambdaj.function.matcher.Predicate;

import dk.dtu.imm.esculapauml.core.checkers.BehaviorChecker;
import dk.dtu.imm.esculapauml.core.checkers.TransitionReplyChecker;
import dk.dtu.imm.esculapauml.core.executors.coordination.EsculapaCallEvent;
import dk.dtu.imm.esculapauml.core.executors.coordination.EsculapaCallReturnControlEvent;
import dk.dtu.imm.esculapauml.core.executors.coordination.EsculapaReplyEvent;
import dk.dtu.imm.esculapauml.core.executors.guards.GuardEvaluator;
import dk.dtu.imm.esculapauml.core.executors.guards.GuardEvaluatorsFactory;
import dk.dtu.imm.esculapauml.core.utils.StateMachineUtils;

/**
 * @author Piotr J. Puczynski
 * 
 */
public class BehaviorExecutor extends AbstractInstanceExecutor {

	protected Set<Vertex> activeConfiguration = new HashSet<Vertex>();
	protected StateMachine checkee;
	protected BehaviorChecker checker;
	protected static Predicate<Transition> isCompletionTransition = new Predicate<Transition>() {
		public boolean apply(Transition item) {
			return item.getTriggers().isEmpty();
		}
	};

	/**
	 * @param checker
	 */
	public BehaviorExecutor(BehaviorChecker checker, String instanceName) {
		super(checker, instanceName, (Class) checker.getCheckedObject().getContext());
		this.checker = checker;
		checkee = checker.getCheckedObject();
		logger = Logger.getLogger(BehaviorExecutor.class);
		logger.debug(checkee.getLabel() + "[" + instanceName + "]: executor created");
	}

	/**
	 * @param checker2
	 * @param instanceSpecification
	 */
	public BehaviorExecutor(BehaviorChecker checker, InstanceSpecification instanceSpecification) {
		super(checker, instanceSpecification, (Class) checker.getCheckedObject().getContext());
		this.checker = checker;
		checkee = checker.getCheckedObject();
		logger = Logger.getLogger(BehaviorExecutor.class);
		logger.debug(checkee.getLabel() + "[" + instanceName + "]: executor created");
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
		TransitionReplyChecker trc = new TransitionReplyChecker(checker, null, null);
		trc.setAllowedToHaveReply(false);
		recalculateActiveState(trc);
	}

	/**
	 * This function calculates the enabled transitions. It also fires the
	 * completion transition. It should be called after every run-to-completion
	 * step.
	 * 
	 */
	protected void recalculateActiveState(TransitionReplyChecker trc) {
		// check for empty transitions and fire them
		boolean hasCompletionTransitions;
		// for loops detection
		ArrayList<Transition> completionTransitionTaken = new ArrayList<Transition>();

		do {
			hasCompletionTransitions = false;
			for (Vertex vertex : activeConfiguration) {
				GuardEvaluator ge = GuardEvaluatorsFactory.getGuardEvaluator(this, vertex);
				ge.setPreconditions(isCompletionTransition);
				EList<Transition> satisfiedCompletionTransitions = ge.getTransitionsWithEnabledGuards();
				Transition transitionToTake = TransitionChooser.choose(this, satisfiedCompletionTransitions);
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
					trc.setNextTransition(transitionToTake);
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
			// enabledTransitions.addAll(vertex.getOutgoings());
			logger.debug(checkee.getLabel() + "[" + instanceName + "]: " + vertex.getLabel());
		}
		logger.debug(checkee.getLabel() + "[" + instanceName + "]: active states end");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.executors.InstanceExecutor#callOperation(
	 * java.lang.Object, java.lang.String, org.eclipse.emf.common.util.EList,
	 * boolean, org.eclipse.uml2.uml.Element)
	 */
	public ValueSpecification callOperation(Object source, InstanceSpecification caller, String operationName, EList<ValueSpecification> arguments,
			boolean isSynchronous, Element errorContext) {
		Operation operation = getOperationByName(operationName);
		if (null == operation) {
			checker.addOtherProblem(Diagnostic.ERROR, "Instance '" + instanceSpecification.getName() + "' has no operation with name '" + operationName + "'.",
					errorContext);
			return null;
		}
		return callOperation(source, caller, operation, arguments, isSynchronous, errorContext);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.executors.InstanceExecutor#callOperation(
	 * java.lang.Object, org.eclipse.uml2.uml.Operation,
	 * org.eclipse.emf.common.util.EList, boolean, org.eclipse.uml2.uml.Element)
	 */
	public ValueSpecification callOperation(Object source, InstanceSpecification caller, Operation operation, EList<ValueSpecification> arguments,
			boolean isSynchronous, Element errorContext) {
		logger.debug(checkee.getLabel() + "[" + instanceName + "]: event arrived: " + operation.getLabel());
		checkOperationConformance(caller, operation, errorContext);
		if (checker.hasErrors()) {
			return null;
		}
		Transition goodTransition = getEnabledTransitionForOperation(operation, arguments, errorContext);
		if (null == goodTransition) {
			if (isSynchronous) {
				checker.addOtherProblem(Diagnostic.ERROR, "Instance '" + instanceSpecification.getName() + "' is not ready to respond to an event '"
						+ operation.getLabel() + "'.", errorContext);
			} else {
				// warning, the machine is not able to process an operation
				// event
				checker.addOtherProblem(Diagnostic.WARNING, "StateMachine instance \"" + instanceSpecification.getName() + "\" is not ready for an event \""
						+ operation.getLabel() + "\". Event is lost.", errorContext);
				// asynchronous call returns immediately
				EsculapaCallReturnControlEvent ecrce = new EsculapaCallReturnControlEvent(this, errorContext, operation);
				checker.getSystemState().getCoordinator().fireEvent(ecrce);
			}
		} else {
			// dispatch new execution event
			EsculapaCallEvent ece = new EsculapaCallEvent(source, errorContext, this, operation, arguments, isSynchronous);
			checker.getSystemState().getCoordinator().fireEvent(ece);
			if (checker.hasErrors()) {
				return null;
			}
			if (!isSynchronous) {
				// asynchronous call returns immediately
				EsculapaCallReturnControlEvent ecrce = new EsculapaCallReturnControlEvent(this, errorContext, operation);
				checker.getSystemState().getCoordinator().fireEvent(ecrce);
			}

			TransitionReplyChecker trc = new TransitionReplyChecker(checker, goodTransition, operation);
			// only synchronous calls can have a reply
			trc.setAcceptReplies(isSynchronous);
			fireTransition(trc);
			// dispatch completion event
			recalculateActiveState(trc);
			ValueSpecification result = trc.getReply();

			if (isSynchronous) {
				// dispatch new reply event
				EsculapaReplyEvent ere = new EsculapaReplyEvent(this, errorContext, operation, result, ece.getSequenceId());
				checker.getSystemState().getCoordinator().fireEvent(ere);
				// synchronous control flow returned here
				EsculapaCallReturnControlEvent ecrce = new EsculapaCallReturnControlEvent(this, errorContext, operation);
				checker.getSystemState().getCoordinator().fireEvent(ecrce);
				return result;
			}
		}
		return null;
	}

	/**
	 * Checks if the operation is in fact ours and if it is called correctly
	 * according to visibility of it.
	 * 
	 * @param caller
	 * @param operation
	 * @param errorContext
	 */
	private void checkOperationConformance(InstanceSpecification caller, Operation operation, Element errorContext) {
		if (!originalClass.getAllOperations().contains(operation)) {
			checker.addOtherProblem(Diagnostic.ERROR, "Instance '" + instanceSpecification.getName() + "' is called with operation '" + operation.getLabel()
					+ "' that is not declared for the instance type.", errorContext);
		} else {
			if (operation.getVisibility() == VisibilityKind.PRIVATE_LITERAL || operation.getVisibility() == VisibilityKind.PROTECTED_LITERAL) {
				// allow only private class access
				if (null != caller) {
					if (!caller.getClassifiers().equals(instanceSpecification.getClassifiers())) {
						checker.addOtherProblem(Diagnostic.ERROR, "Instance '" + instanceSpecification.getName()
								+ "' reported external access to private operation '" + operation.getLabel() + "' from '" + caller.getLabel() + "'.",
								errorContext);
					}
				} else {
					checker.addOtherProblem(Diagnostic.ERROR, "Instance '" + instanceSpecification.getName()
							+ "' reported external access to private operation '" + operation.getLabel() + "'.", errorContext);
				}
			}
		}

	}

	/**
	 * Prepares the event to be run on the instance. If guard is satisfied, the
	 * transition is returned, otherwise null is returned.
	 * 
	 * @param operation
	 * @param arguments
	 * @param errorContext
	 * 
	 * @return
	 */
	private Transition getEnabledTransitionForOperation(final Operation operation, EList<ValueSpecification> arguments, Element errorContext) {
		// if there are any argument to set, make a deep copy of current
		// instance (in case we need to restore it later)
		Collection<Slot> backupSlots = null;
		if (!arguments.isEmpty()) {
			backupSlots = getDeepCopyOfMySlots();
			// set the arguments as local values of state machine
			preprocessOperationArguments(operation, arguments, errorContext);
			if (checker.hasErrors()) {
				restoreCopyOfMySlots(backupSlots);
				return null;
			}
		}
		// for each state in active configuration check if we can fire
		// transition
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
		EList<Transition> results = new BasicEList<Transition>();
		for (Vertex vertex : activeConfiguration) {
			GuardEvaluator ge = GuardEvaluatorsFactory.getGuardEvaluator(this, vertex);
			ge.setPreconditions(hasTriggerForOperation);
			results.addAll(ge.getTransitionsWithEnabledGuards());
		}

		Transition result = TransitionChooser.choose(this, results);

		if (null != backupSlots) {
			// ups, there is no transition to fire
			// we need to restore instance
			if (null == result) {
				restoreCopyOfMySlots(backupSlots);
			} // else {
				// cleanup
				// for (Slot slot : backupSlots) {
			// EcoreUtil.delete(slot);
			// }
			// }

		}
		return result;
	}

	/**
	 * Prepares operation arguments to be used in state machine
	 * 
	 * @param operation
	 * @param arguments
	 * @param errorContext
	 */
	private void preprocessOperationArguments(Operation operation, EList<ValueSpecification> arguments, Element errorContext) {
		EList<Parameter> parameters = new UniqueEList.FastCompare<Parameter>(filter(
				having(on(Parameter.class).getDirection(), not(equalTo(ParameterDirectionKind.RETURN_LITERAL))), operation.getOwnedParameters()));
		List<ValueSpecification> argumentsIn = filter(having(on(ValueSpecification.class).getName(), not(equalTo("return"))), arguments);
		Iterator<ValueSpecification> a = argumentsIn.iterator();
		Iterator<Parameter> p = parameters.iterator();

		while (a.hasNext() && p.hasNext()) {
			ValueSpecification arg = a.next();
			Parameter param = p.next();
			if (param.getDirection() == ParameterDirectionKind.IN_LITERAL) {
				arg.setName(param.getName());
				if (!setVariable(arg.getName(), arg, errorContext)) {
					break;
				}
			}
		}
		if (a.hasNext() || p.hasNext()) {
			checker.addOtherProblem(Diagnostic.WARNING, "Wrong number of arguments for operation '" + operation.getLabel() + "'", errorContext);
		}

	}

	/**
	 * Fire transition in FTR.
	 * 
	 * @param transition
	 */
	protected void fireTransition(TransitionReplyChecker trc) {
		logger.info(checkee.getLabel() + "[" + instanceName + "]: firing transition: " + trc.getCheckedObject().getLabel());
		Vertex source = trc.getCheckedObject().getSource();
		Vertex target = trc.getCheckedObject().getTarget();
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
			Namespace lca = checkee.LCA(compositeSource, compositeTarget);
			// TODO: add removal of nested composite states, then lca will not
			// be null
			if (null == lca) {
				if (checkee.ancestor(compositeSource, compositeTarget)) {
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

		// run effect of transition
		runEffect(trc);
	}

	/**
	 * @param state
	 */
	private void exitCompositeState(State compositeState) {
		activeConfiguration.remove(compositeState);
		Iterator<Vertex> it = activeConfiguration.iterator();
		while (it.hasNext()) {
			Vertex vertex = it.next();
			if (vertex.getContainer().getState() != null && checkee.ancestor(compositeState, vertex.getContainer().getState())) {
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
	 * executes an effect
	 * 
	 * @param ftr
	 */
	protected void runEffect(TransitionReplyChecker ftr) {
		Behavior effect = ftr.getCheckedObject().getEffect();
		if (null != effect) {
			if (effect instanceof OpaqueBehavior) {
				OpaqueBehaviorExecutor obe = new OpaqueBehaviorExecutor(this, ftr);
				obe.prepare();
				obe.execute();
			} else {
				ftr.addProblem(Diagnostic.WARNING, "Unsupported effect type specified on transition '" + ftr.getCheckedObject().getLabel() + "'.");
			}
		}

	}

}
