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
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Actor;
import org.eclipse.uml2.uml.BehaviorExecutionSpecification;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.InstanceSpecification;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageEnd;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.MessageSort;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.UMLPackage.Literals;
import org.eclipse.uml2.uml.ValueSpecification;

import dk.dtu.imm.esculapauml.core.checkers.UseCaseChecker;
import dk.dtu.imm.esculapauml.core.collections.CallArguments;
import dk.dtu.imm.esculapauml.core.collections.ValuesCollection;
import dk.dtu.imm.esculapauml.core.collections.ValuesList;
import dk.dtu.imm.esculapauml.core.executors.coordination.EsculapaCallEvent;
import dk.dtu.imm.esculapauml.core.executors.coordination.EsculapaCallReturnControlEvent;
import dk.dtu.imm.esculapauml.core.executors.coordination.EsculapaReplyEvent;
import dk.dtu.imm.esculapauml.core.executors.coordination.ExecutionCallListener;
import dk.dtu.imm.esculapauml.core.executors.coordination.ExecutionCallReturnControlListener;
import dk.dtu.imm.esculapauml.core.executors.coordination.ExecutionReplyListener;
import dk.dtu.imm.esculapauml.core.executors.termination.ExecutionTerminator;
import dk.dtu.imm.esculapauml.core.generators.LifelineGenerator;
import dk.dtu.imm.esculapauml.core.generators.MessageGenerator;
import dk.dtu.imm.esculapauml.core.states.SystemState;
import dk.dtu.imm.esculapauml.core.utils.InteractionUtils;
import dk.dtu.imm.esculapauml.core.utils.UMLTypesUtil;

/**
 * executes the use case
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class UseCaseExecutor extends AbstractExecutor implements ExecutionCallListener, ExecutionReplyListener, ExecutionCallReturnControlListener {

	protected Message currentMessage;
	protected Interaction checkee;
	protected SystemState systemState;
	protected UseCaseChecker checker;
	protected Stack<Message> callStack = new Stack<Message>();
	protected List<Message> executedAssynchMessages = new ArrayList<Message>();
	protected InteractionSequencer sequencer = new InteractionSequencer();

	/**
	 * @return the sequencer
	 */
	public InteractionSequencer getSequencer() {
		return sequencer;
	}

	/**
	 * @param checker
	 */
	public UseCaseExecutor(UseCaseChecker checker) {
		super(checker);
		this.checker = checker;
		checkee = checker.getCheckedObject();
		systemState = checker.getSystemState();
		logger = Logger.getLogger(UseCaseExecutor.class);
		logger.debug(checkee.getLabel() + ": executor created");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dk.dtu.imm.esculapauml.core.executors.ExecutorInterface#prepare()
	 */
	@Override
	public void prepare() {
		logger.debug(checkee.getLabel() + ": executor preparation");
		// register listener
		systemState.getCoordinator().addExecutionListener(this);
		// prepare terminator
		new ExecutionTerminator(this);

		currentMessage = getFirstMessage();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.executors.coordination.ExecutionListener#
	 * callEventOccurred
	 * (dk.dtu.imm.esculapauml.core.executors.coordination.EsculapaCallEvent)
	 * 
	 * Operation called each time some operation is executed from outside (e.g.
	 * in state machines). It enables extension of sequence diagrams or
	 * synchronization with current execution (depending on mode).
	 */

	@Override
	public void callEventOccurred(EsculapaCallEvent event) {
		if (!callStack.isEmpty() && callStack.peek() == event.getSource()) {
			// we should skip this message generation since its us who called it
			// (so we already have it)
			sequencer.addEvent(event, callStack.peek());
			return;
		}
		Operation operation = event.getOperation();
		InstanceExecutor targetExecutor = event.getTarget();
		org.eclipse.uml2.uml.Class targetClass = targetExecutor.getOriginalClass();
		Lifeline sourceLifeline = findSourceLifeline(event);
		// TODO: generate source if needed
		Lifeline targetLifeline = findLifelineForInstanceExecutor(targetExecutor);
		Message message;
		if (null == targetLifeline) {
			// there is no lifeline now that could correspond to the object
			// we need to create it
			LifelineGenerator lifelineGenerator = new LifelineGenerator(checker, checkee, targetClass, targetExecutor.getInstanceName());
			targetLifeline = lifelineGenerator.generate();
			// we will need also to generate BehaviorExecutionSpecification and
			// a message (with call event)
			MessageGenerator messageGenerator = new MessageGenerator(checker, sourceLifeline, targetLifeline, sequencer);
			messageGenerator.setOperation(operation);
			if (!event.isSynchronousCall()) {
				messageGenerator.setMessageSort(MessageSort.ASYNCH_CALL_LITERAL);
			}
			MessageOccurrenceSpecification mos = InteractionUtils.getLastMessageEventOnLifeline(currentMessage, sourceLifeline);
			messageGenerator.setSentGenerateAfter(mos);
			// generate new bes only when the last reply message was for the
			// call initiating current bes
			messageGenerator.setGenerateNewBESForSent(hasToGenerateNewBES(currentMessage, sourceLifeline));
			// always generate new bes for target
			messageGenerator.setGenerateNewBESForReceive(true);
			messageGenerator.setArguments(event.getArguments().getFlattened());
			message = messageGenerator.generate();

		} else {
			// we need to check if the next message conforms to the event
			// check if operation and lifelines are the same
			message = getNextMessage(currentMessage);
			if (null == message || InteractionUtils.getMessageOperation(message) != operation
					|| (message.getMessageSort() == MessageSort.SYNCH_CALL_LITERAL) != event.isSynchronousCall()
					|| InteractionUtils.getMessageSourceLifeline(message) != sourceLifeline
					|| InteractionUtils.getMessageTargetLifeline(message) != targetLifeline
					|| !areArgumentsEqual(message.getArguments(), event.getArguments().getFlattened())) {
				// message not conform to given operation
				// we need to generate a new message
				MessageGenerator messageGenerator = new MessageGenerator(checker, sourceLifeline, targetLifeline, sequencer);
				messageGenerator.setOperation(operation);
				if (!event.isSynchronousCall()) {
					messageGenerator.setMessageSort(MessageSort.ASYNCH_CALL_LITERAL);
				}
				messageGenerator.setGenerateNewBESForSent(hasToGenerateNewBES(currentMessage, sourceLifeline));
				messageGenerator.setSentGenerateAfter(InteractionUtils.getLastMessageEventOnLifeline(currentMessage, sourceLifeline));
				if (sourceLifeline == targetLifeline) {
					// for a self message
					messageGenerator.setReceiveAfterSent(true);
					messageGenerator.setGenerateNewBESForReceive(false);
				} else {
					messageGenerator.setReceiveGenerateAfter(sequencer.getLastOccurrenceOnLifeline(targetLifeline));
					messageGenerator.setGenerateNewBESForReceive(hasToGenerateNewBES(sequencer.getLastMessageOnLifeline(targetLifeline), targetLifeline));
				}
				messageGenerator.setArguments(event.getArguments().getFlattened());
				message = messageGenerator.generate();
			}
		}
		currentMessage = message;
		callStack.add(message);
		sequencer.addEvent(event, message);
	}

	/**
	 * @param mos
	 * @param currentMessage2
	 * @param sourceLifeline
	 * @return
	 */
	private boolean hasToGenerateNewBES(Message previousMessage, Lifeline lifeline) {
		if (null == previousMessage) {
			return true;
		}
		BehaviorExecutionSpecification currentBES = InteractionUtils.getMessageExecutionSpecificationOnLifeline(previousMessage, lifeline);
		Message firstMessageOnBES = ((MessageOccurrenceSpecification) currentBES.getStart()).getMessage();
		return previousMessage.getMessageSort() == MessageSort.REPLY_LITERAL && sequencer.getCallFor(previousMessage) == firstMessageOnBES;
	}

	/**
	 * @return the checkee
	 */
	public Interaction getInteraction() {
		return checkee;
	}

	/**
	 * Checks if the arguments are the same.
	 * 
	 * @param arguments
	 * @param arguments2
	 * @return
	 */
	private boolean areArgumentsEqual(EList<ValueSpecification> arguments, EList<ValueSpecification> arguments2) {
		List<ValueSpecification> argumentsIn = filter(having(on(ValueSpecification.class).getName(), not(equalTo("return"))), arguments);
		List<ValueSpecification> arguments2In = filter(having(on(ValueSpecification.class).getName(), not(equalTo("return"))), arguments2);
		Iterator<ValueSpecification> it = argumentsIn.iterator();
		Iterator<ValueSpecification> it2 = arguments2In.iterator();
		while (it.hasNext() && it2.hasNext()) {
			ValueSpecification val = it.next();
			ValueSpecification val2 = it2.next();
			if (!UMLTypesUtil.areEqualTypeConformance(val, val2)) {
				return false;
			}
		}

		return (!it.hasNext() && !it2.hasNext());
	}

	/**
	 * Finds source lifeline for an event.
	 * 
	 * @param event
	 * @return
	 */
	private Lifeline findSourceLifeline(EsculapaCallEvent event) {
		if (event.getSource() instanceof InstanceExecutor) {
			// sent by another instance
			return findLifelineForInstanceExecutor((InstanceExecutor) event.getSource());
		} else if (event.getSource() instanceof Message) {
			// sent by some interaction
			Lifeline result = InteractionUtils.getMessageSourceLifeline((Message) event.getSource());
			if (checkee.getLifelines().contains(result)) {
				// sent by us
				return result;
			}
		}
		return null;
	}

	/**
	 * Finds a lifeline that the instance executor represents in the diagram (if
	 * there is any).
	 * 
	 * @param executor
	 * @return
	 */
	private Lifeline findLifelineForInstanceExecutor(InstanceExecutor executor) {
		// search based on name and class
		for (Lifeline lifeline : checkee.getLifelines()) {
			if (lifeline.getName().equals(executor.getInstanceName()) && lifeline.getRepresents().getType() == executor.getOriginalClass()) {
				return lifeline;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.executors.coordination.ExecutionListener#
	 * replyEventOccurred
	 * (dk.dtu.imm.esculapauml.core.executors.coordination.EsculapaReplyEvent)
	 */
	@Override
	public void replyEventOccurred(EsculapaReplyEvent event) {
		// if next message is not a reply after unwind, we should
		// generate and immediately execute reply message
		// we need to immediately execute to update currentMessage
		// in case there are other replies on stack to generate
		if (!callStack.isEmpty()) {
			Message callMessage = callStack.peek();
			// wa assume we retrieved the reply for last call on stack, if not
			// its an error
			if (callMessage == sequencer.getMessageWithSequence(event.getInitiatingCallSequenceNumber())) {
				Message reply = getNextMessage(currentMessage);
				if (reply == null) {
					reply = generateReplyMessage(callMessage);
				} else if (reply.getMessageSort() != MessageSort.REPLY_LITERAL) {
					// if there is a call message on the same BES that was not
					// called, it is an error
					Lifeline myLifeline = InteractionUtils.getMessageTargetLifeline(callMessage);
					if (InteractionUtils.getMessageExecutionSpecificationOnLifeline(callMessage, myLifeline) == InteractionUtils
							.getMessageExecutionSpecificationOnLifeline(reply, myLifeline)) {
						checker.addOtherProblem(Diagnostic.ERROR, "The message was never realized in SM before reply of '" + callMessage.getLabel()
								+ "' for operation '" + event.getOperation().getLabel() + "' arrived.", reply);
					} else {
						reply = generateReplyMessage(callMessage);
					}
				} else {
					fixReplyMessage(reply, callMessage);
				}
				if (checker.hasErrors()) {
					return;
				}
				// check and set result of a message
				setMessageReturn(callMessage, reply, event.getResult());
				currentMessage = reply;
				sequencer.addEvent(event, reply);
			} else {
				checker.addOtherProblem(Diagnostic.ERROR, "Reply for message " + callMessage.getLabel()
						+ " did not arrive before a different reply for operation '" + event.getOperation().getLabel() + "' arrived.", callMessage);
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.executors.coordination.ExecutionListener#
	 * callReturnControlEventOccurred
	 * (dk.dtu.imm.esculapauml.core.executors.coordination
	 * .EsculapaCallReturnControlEvent)
	 */
	@Override
	public void callReturnControlEventOccurred(EsculapaCallReturnControlEvent event) {
		if (!callStack.isEmpty()) {
			Message callMessage = sequencer.getMessageWithSequence(event.getInitiatingCallSequenceNumber());
			if (callMessage.getMessageSort() == MessageSort.ASYNCH_CALL_LITERAL) {
				executedAssynchMessages.add(0, callMessage);
			}
			callStack.remove(callMessage);
		}

	}

	/**
	 * Executes all messages
	 */
	public void execute() {
		// execute all messages
		logger.info(checkee.getLabel() + ": executor start execution");
		while (null != currentMessage) {
			if ((currentMessage.getMessageSort() == MessageSort.REPLY_LITERAL)) {
				checker.addOtherProblem(Diagnostic.ERROR, "Cannot execute reply message '" + currentMessage.getLabel() + "'.", currentMessage);
				break;
			} else {
				Lifeline lifeline = InteractionUtils.getMessageSourceLifeline(currentMessage);
				if (null != lifeline) {
					if (!(lifeline.getRepresents().getType() instanceof Actor)) {
						checker.addOtherProblem(Diagnostic.ERROR, "Cannot execute message not from actor in interaction: '" + currentMessage.getLabel() + "'.",
								currentMessage);
						break;
					}
				}
			}
			executeMessage(currentMessage);
			if (checker.hasErrors()) {
				logger.warn(checkee.getLabel() + ": executor stopped due to errors detected");
				break;
			}
			currentMessage = getNextMessage(currentMessage);
		}

	}

	/**
	 * Executes one message.
	 * 
	 * @param message
	 * @return
	 */
	protected void executeMessage(Message message) {
		logger.info("Executing message " + message.getLabel());
		if ((message.getMessageSort() == MessageSort.SYNCH_CALL_LITERAL) || (message.getMessageSort() == MessageSort.ASYNCH_CALL_LITERAL)) {
			Lifeline targetLifeline = InteractionUtils.getMessageTargetLifeline(message);
			BehaviorExecutor targetExecutor = (BehaviorExecutor) findInstanceExecutorForLifeline(targetLifeline);
			NamedElement signature = message.getSignature();

			if (signature instanceof Operation) {
				InstanceSpecification caller = null;
				InstanceExecutor sourceExecutor = findInstanceExecutorForLifeline(InteractionUtils.getMessageSourceLifeline(message));
				if (null != sourceExecutor) {
					caller = sourceExecutor.getInstanceSpecification();
				}
				Operation operation = (Operation) signature;
				callStack.add(message);
				targetExecutor.callOperation(message, caller, operation, new CallArguments(operation, message.getArguments()),
						message.getMessageSort() == MessageSort.SYNCH_CALL_LITERAL, message);
				checker.getSystemState().getScheduler().executeFromQueue();
			}
		}
	}

	/**
	 * Uses systemState to find corresponding executor.
	 * 
	 * @param targetLifeline
	 * @return
	 */
	private InstanceExecutor findInstanceExecutorForLifeline(Lifeline lifeline) {
		if (lifeline.getRepresents().getType() instanceof Class) {
			return systemState.getInstanceExecutor(lifeline.getName(), (Class) lifeline.getRepresents().getType());
		}
		return null;
	}

	/**
	 * This method assures that the reply message points to the same operation
	 * than original message. If not, it's a warning, if there is no event then
	 * it should set the correct one.
	 * 
	 * @param reply
	 * @param message
	 */
	private void fixReplyMessage(Message reply, Message message) {
		Operation mOperation = InteractionUtils.getMessageOperation(message);
		Operation rOperation = InteractionUtils.getMessageOperation(reply);
		if (null == rOperation) {
			// fix it
			InteractionUtils.setMessageOperation(reply, mOperation);
		} else {
			if (rOperation != mOperation) {
				checker.addOtherProblem(Diagnostic.ERROR, "Reply " + reply.getLabel() + " does not correspond to message " + message.getLabel() + " operation",
						reply);
			}
		}
	}

	/**
	 * Copies original arguments to the reply, places the return value on reply
	 * message.
	 * 
	 * @param message
	 * @param reply
	 * @param result
	 */
	private void setMessageReturn(Message message, Message reply, ValuesCollection results) {
		if (replyShouldBeChecked(reply)) {
			// check reply
			ValuesCollection expectedResults = new ValuesList("return", reply.getArguments());
			if (!expectedResults.isEmpty()) {
				if (expectedResults.size() != results.size()) {
					checker.addOtherProblem(Diagnostic.ERROR, "Reply " + reply.getLabel() + " result size ('" + results.size()
							+ "') is not equal to expected result size ('" + expectedResults.size() + "').", reply);
				}
				Iterator<ValueSpecification> itResult = results.iterator();
				Iterator<ValueSpecification> itExpectedResult = expectedResults.iterator();
				while (itResult.hasNext() && itExpectedResult.hasNext()) {
					ValueSpecification result = itResult.next();
					ValueSpecification expectedResult = itExpectedResult.next();
					if (!UMLTypesUtil.areIdentical(expectedResult, result)) {
						checker.addOtherProblem(Diagnostic.ERROR, "Reply " + reply.getLabel() + " result ('" + result.stringValue()
								+ "') is not equal to expected result ('" + expectedResult.stringValue() + "').", reply);
						break;
					}
				}
			}
		} else {
			// clean old values
			reply.getArguments().clear();
			// set return
			if (null != results) {
				for (ValueSpecification result : results) {
					ValueSpecification ret = EcoreUtil.copy(result);
					ret.setName("return");
					reply.getArguments().add(ret);
				}
			}
		}

	}

	/**
	 * Decides when the reply result should conform to specified by the user.
	 * 
	 * @param reply
	 * @return
	 */
	private boolean replyShouldBeChecked(Message reply) {
		// it must not be generated
		if (!systemState.wasGenerated(reply)) {
			// it must be pointing to the actor
			Lifeline lifeline = InteractionUtils.getMessageTargetLifeline(reply);
			if (null != lifeline) {
				if (lifeline.getRepresents().getType() instanceof Actor) {
					// it must have some return specified
					if (null != reply.getArgument("return", null)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Generates reply message for an initiating message
	 * 
	 * @param message
	 * @return
	 */
	private Message generateReplyMessage(Message message) {
		// lifelines intentionally switched
		Lifeline sourceLifeline = InteractionUtils.getMessageTargetLifeline(message);
		Lifeline targetLifeline = InteractionUtils.getMessageSourceLifeline(message);
		MessageGenerator messageGenerator = new MessageGenerator(checker, sourceLifeline, targetLifeline, sequencer);
		messageGenerator.setMessageSort(MessageSort.REPLY_LITERAL);
		messageGenerator.setCustomName("ReplyOf" + message.getName());
		messageGenerator.setOperation(InteractionUtils.getMessageOperation(message));
		// the reply must be inserted after currentMessage (not after message)
		if (sourceLifeline.getCoveredBys().contains(currentMessage.getReceiveEvent())) {
			messageGenerator.setSentGenerateAfter((MessageOccurrenceSpecification) currentMessage.getReceiveEvent());
		} else {
			messageGenerator.setSentGenerateAfter((MessageOccurrenceSpecification) currentMessage.getSendEvent());
		}
		if (sourceLifeline == targetLifeline) {
			// self reply
			messageGenerator.setReceiveAfterSent(true);
		} else {
			messageGenerator.setReceiveGenerateAfter((MessageOccurrenceSpecification) message.getSendEvent());
		}
		messageGenerator.setGenerateNewBESForReceive(false);
		messageGenerator.setGenerateNewBESForSent(false);
		return messageGenerator.generate();
	}

	/**
	 * Gets the first message to send in interaction
	 * 
	 * @return the first message to send or null if interaction has no messages
	 */
	protected Message getFirstMessage() {
		// get all possible messages
		EList<Message> messages = checkee.getMessages();
		// now check which message is sent first on lifelines
		EList<Lifeline> lifelines = checkee.getLifelines();
		for (Lifeline l : lifelines) {
			// we are only interested in the first fragment of message
			// occurrence specification
			MessageOccurrenceSpecification spec = (MessageOccurrenceSpecification) EcoreUtil.getObjectByType(l.getCoveredBys(),
					Literals.MESSAGE_OCCURRENCE_SPECIFICATION);
			if (null != spec) {
				for (Message m : messages) {
					if (spec == m.getReceiveEvent()) {
						// m is the first message to consider
						Message result, helper = m;
						do {
							result = helper;
							helper = getPreviousMessage(result);
						} while (null != helper);
						return result;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Returns next message to execute or null if end of interaction
	 * 
	 * @param msg
	 * @return
	 */
	private Message getNextMessage(Message message) {
		MessageEnd receiveEvent = message.getReceiveEvent();
		if (receiveEvent instanceof MessageOccurrenceSpecification) {
			Lifeline targetLifeline = (Lifeline) EcoreUtil.getObjectByType(((MessageOccurrenceSpecification) receiveEvent).getCovereds(), Literals.LIFELINE);
			if (null != targetLifeline) {
				Collection<MessageOccurrenceSpecification> targetSpecsCol = EcoreUtil.getObjectsByType(targetLifeline.getCoveredBys(),
						Literals.MESSAGE_OCCURRENCE_SPECIFICATION);
				List<MessageOccurrenceSpecification> targetSpecs = new ArrayList<MessageOccurrenceSpecification>(targetSpecsCol);
				int sourceIndex = targetSpecs.indexOf(receiveEvent);
				if ((sourceIndex >= 0) && (sourceIndex + 1 != targetSpecs.size())) {
					MessageOccurrenceSpecification nextEnd = targetSpecs.get(sourceIndex + 1);
					if (!sequencer.wasExecuted(nextEnd.getMessage())) {
						return nextEnd.getMessage();
					}
				}
			}

			// because asynchronous calls do not have reply messages
			// (usually)
			// we cannot track them normally
			// before returning here check if there is any asynchronous call
			// on
			// the stack
			Iterator<Message> it = executedAssynchMessages.iterator();
			while (it.hasNext()) {
				// try to find next message for assynch
				Message assynchMessage = it.next();
				MessageEnd sentEvent = assynchMessage.getSendEvent();
				if (sentEvent instanceof MessageOccurrenceSpecification) {
					Lifeline sourceLifeline = (Lifeline) EcoreUtil.getObjectByType(((MessageOccurrenceSpecification) sentEvent).getCovereds(),
							Literals.LIFELINE);
					if (null != sourceLifeline) {
						Collection<MessageOccurrenceSpecification> sourceSpecsCol = EcoreUtil.getObjectsByType(sourceLifeline.getCoveredBys(),
								Literals.MESSAGE_OCCURRENCE_SPECIFICATION);
						ArrayList<MessageOccurrenceSpecification> sourceSpecs = new ArrayList<MessageOccurrenceSpecification>(sourceSpecsCol);
						int sourceIndex = sourceSpecs.indexOf(sentEvent);
						if ((sourceIndex >= 0) && (sourceIndex + 1 != sourceSpecs.size())) {
							MessageOccurrenceSpecification nextEnd = sourceSpecs.get(sourceIndex + 1);
							if (!sequencer.wasExecuted(nextEnd.getMessage())) {
								return nextEnd.getMessage();
							}
						}
					}
				}

			}

		}
		return null;
	}

	/**
	 * Returns previous message to execute or null if beginning of interaction
	 * 
	 * @param msg
	 * @return
	 */
	private Message getPreviousMessage(Message message) {
		MessageEnd sendEvent = message.getSendEvent();
		if (sendEvent instanceof MessageOccurrenceSpecification) {
			Lifeline sourceLifeline = (Lifeline) EcoreUtil.getObjectByType(((MessageOccurrenceSpecification) sendEvent).getCovereds(), Literals.LIFELINE);
			if (null != sourceLifeline) {
				Collection<MessageOccurrenceSpecification> sourceSpecsCol = EcoreUtil.getObjectsByType(sourceLifeline.getCoveredBys(),
						Literals.MESSAGE_OCCURRENCE_SPECIFICATION);
				ArrayList<MessageOccurrenceSpecification> sourceSpecs = new ArrayList<MessageOccurrenceSpecification>(sourceSpecsCol);
				int sourceIndex = sourceSpecs.indexOf(sendEvent);
				if (sourceIndex > 0) {
					MessageOccurrenceSpecification previousEnd = sourceSpecs.get(sourceIndex - 1);
					return previousEnd.getMessage();
				}
			}

		}
		return null;
	}

	/**
	 * @return the currentMessage
	 */
	public Message getCurrentMessage() {
		return currentMessage;
	}

}
