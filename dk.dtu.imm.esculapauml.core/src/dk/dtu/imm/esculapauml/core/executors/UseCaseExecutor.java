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

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.BehavioredClassifier;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageEnd;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.MessageSort;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.OccurrenceSpecification;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.UMLPackage.Literals;
import org.eclipse.uml2.uml.ValueSpecification;

import dk.dtu.imm.esculapauml.core.checkers.UseCaseChecker;
import dk.dtu.imm.esculapauml.core.executors.coordination.EsculapaCallEvent;
import dk.dtu.imm.esculapauml.core.executors.coordination.ExecutionListener;
import dk.dtu.imm.esculapauml.core.generators.BehaviorExecutionSpecificationGenerator;
import dk.dtu.imm.esculapauml.core.generators.LifelineGenerator;
import dk.dtu.imm.esculapauml.core.generators.MessageGenerator;
import dk.dtu.imm.esculapauml.core.states.SystemState;
import dk.dtu.imm.esculapauml.core.utils.InteractionUtils;

/**
 * executes the use case
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class UseCaseExecutor extends AbstractExecutor implements ExecutionListener {

	protected Message currentMessage;
	protected Interaction checkee;
	protected SystemState systemState;
	protected UseCaseChecker checker;

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
		Operation operation = event.getOperation();
		BehaviorExecutor executor = event.getSource();
		org.eclipse.uml2.uml.Class targetClass = operation.getClass_();
		Lifeline sourceLifeline = executor.getLifeline();
		Lifeline targetLifeline = InteractionUtils.findRepresentingLifeline(checkee, targetClass);
		if (null == targetLifeline) {
			// there is no lifeline now that could correspond to the object
			// we need to create it
			LifelineGenerator lifelineGenerator = new LifelineGenerator(checker, checkee, targetClass);
			targetLifeline = lifelineGenerator.generate();
			// we will need also to generate BehaviorExecutionSpecification and
			// a message (with call event)
			MessageGenerator messageGenerator = new MessageGenerator(checker, sourceLifeline, targetLifeline);
			messageGenerator.setOperation(operation);
			messageGenerator.setSentGenerateAfter((MessageOccurrenceSpecification) currentMessage.getReceiveEvent());
			Message message = messageGenerator.generate();
			BehaviorExecutionSpecificationGenerator besGenerator = new BehaviorExecutionSpecificationGenerator(systemState,
					(BasicDiagnostic) checker.getDiagnostics(), targetLifeline);
			besGenerator.setStartAndFinish((OccurrenceSpecification) message.getReceiveEvent());
			besGenerator.generate();
			executeMessage(message);
		} else {
			// check if operation and lifelines are the same
			Message message = getNextMessage(currentMessage);
			if (InteractionUtils.getMessageOperation(message) != operation || InteractionUtils.getMessageSourceLifeline(message) != sourceLifeline
					|| InteractionUtils.getMessageTargetLifeline(message) != targetLifeline) {
				// message not conform to given operation
				// we need to generate a new message
				MessageGenerator messageGenerator = new MessageGenerator(checker, sourceLifeline, targetLifeline);
				messageGenerator.setOperation(operation);
				messageGenerator.setSentGenerateAfter((MessageOccurrenceSpecification) currentMessage.getReceiveEvent());
				// TODO: add setting receive event in correct place
				message = messageGenerator.generate();
			}
			executeMessage(message);
		}

	}

	/**
	 * Executes all messages
	 */
	public void execute() {
		// execute all messages
		logger.info(checkee.getLabel() + ": executor start execution");
		while (null != currentMessage) {
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
		currentMessage = message;
		if ((message.getMessageSort() == MessageSort.SYNCH_CALL_LITERAL) || (message.getMessageSort() == MessageSort.ASYNCH_CALL_LITERAL)) {
			BehavioredClassifier target = (BehavioredClassifier) InteractionUtils.getMessageTargetType(message);
			BehaviorExecutor targetExecutor = systemState.getBehaviorChecker(target).getDefaultExecutor();
			NamedElement signature = message.getSignature();

			if (message.getMessageSort() == MessageSort.SYNCH_CALL_LITERAL) {
				if (signature instanceof Operation) {
					Operation operation = (Operation) signature;
					ValueSpecification result = targetExecutor.runOperation(message, operation);
					if (checker.hasErrors()) {
						return;
					}
					// if next message is not a reply after unwind, we should
					// generate and immediately execute reply message
					// we need to immediately execute to update currentMessage
					// in case there are other replies on stack to generate
					Message reply = getNextMessage(currentMessage);
					if (reply == null || reply.getMessageSort() != MessageSort.REPLY_LITERAL) {
						reply = generateReplyMessage(message);
					} else {
						fixReplyMessage(reply, message);
					}
					if (checker.hasErrors()) {
						return;
					}
					// check and set result of a message
					setMessageReturn(message, reply, result);
					executeMessage(reply);
				}
			} else if (message.getMessageSort() == MessageSort.ASYNCH_CALL_LITERAL) {
				if (signature instanceof Operation) {
					targetExecutor.runOperation(message, (Operation) signature);
				}
			}
		}
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
	private void setMessageReturn(Message message, Message reply, ValueSpecification result) {
		// clean old values
		reply.getArguments().clear();
		// set return
		if (null != result) {
			ValueSpecification ret = EcoreUtil.copy(result);
			ret.setName("return");
			reply.getArguments().add(ret);
		}

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
		MessageGenerator messageGenerator = new MessageGenerator(systemState, (BasicDiagnostic) checker.getDiagnostics(), sourceLifeline, targetLifeline);
		messageGenerator.setMessageSort(MessageSort.REPLY_LITERAL);
		messageGenerator.setCustomName("ReplyOf" + message.getName());
		messageGenerator.setOperation(InteractionUtils.getMessageOperation(message));
		// the reply must be inserted after currentMessage (not after message)
		if (sourceLifeline.getCoveredBys().contains(currentMessage.getReceiveEvent())) {
			messageGenerator.setSentGenerateAfter((MessageOccurrenceSpecification) currentMessage.getReceiveEvent());
		} else {
			messageGenerator.setSentGenerateAfter((MessageOccurrenceSpecification) currentMessage.getSendEvent());
		}
		messageGenerator.setReceiveGenerateAfter((MessageOccurrenceSpecification) message.getSendEvent());
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
				ArrayList<MessageOccurrenceSpecification> targetSpecs = new ArrayList<MessageOccurrenceSpecification>(targetSpecsCol);
				int sourceIndex = targetSpecs.indexOf(receiveEvent);
				if ((sourceIndex >= 0) && (sourceIndex + 1 != targetSpecs.size())) {
					MessageOccurrenceSpecification nextEnd = targetSpecs.get(sourceIndex + 1);
					return nextEnd.getMessage();
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
