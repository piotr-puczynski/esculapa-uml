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
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.BehaviorExecutionSpecification;
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

import dk.dtu.imm.esculapauml.core.checkers.UseCaseChecker;
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
public class UseCaseExecutor extends AbstractExecutor<UseCaseChecker> {

	private Message currentMessage;
	private Interaction checkee;
	private SystemState systemState;

	/**
	 * @param checker
	 */
	public UseCaseExecutor(UseCaseChecker checker) {
		super(checker);
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
		currentMessage = getFirstMessage();

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
		currentMessage = message;
		BehavioredClassifier target = (BehavioredClassifier) InteractionUtils.getMessageTargetType(message);
		BehaviorExecutor targetExecutor = systemState.getBehaviorChecker(target).getDefaultExecutor();
		NamedElement signature = message.getSignature();

		if ((message.getMessageSort() == MessageSort.SYNCH_CALL_LITERAL) || (message.getMessageSort() == MessageSort.ASYNCH_CALL_LITERAL)) {
			if (signature instanceof Operation) {
				targetExecutor.runOperation(message, (Operation) signature);
			}
		}
	}

	/**
	 * Operation called each time some operation is executed from outside (e.g.
	 * in state machines). It enables extension of sequence diagrams or
	 * synchronization with current execution (depending on mode).
	 * 
	 * @param executor
	 * @param operation
	 */
	protected void behaviorExecution(BehaviorExecutor executor, Operation operation) {
		org.eclipse.uml2.uml.Class targetClass = operation.getClass_();
		Lifeline lifeline = InteractionUtils.findRepresentingLifeline(checkee, targetClass);
		if (null == lifeline) {
			// there is no lifeline now that could correspond to the object
			// we need to create it
			LifelineGenerator lifelineGenerator = new LifelineGenerator(systemState, (BasicDiagnostic) checker.getDiagnostics(), checkee, targetClass);
			lifeline = lifelineGenerator.generate();
			// we will need also to generate BehaviorExecutionSpecification and
			// a message (with call event)
			MessageGenerator messageGenerator = new MessageGenerator(systemState, (BasicDiagnostic) checker.getDiagnostics(), operation,
					InteractionUtils.getMessageTargetTLifeline(currentMessage), lifeline);
			messageGenerator.setSentGenerateAfter((MessageOccurrenceSpecification) currentMessage.getReceiveEvent());
			Message message = messageGenerator.generate();
			BehaviorExecutionSpecificationGenerator besGenerator = new BehaviorExecutionSpecificationGenerator(systemState,
					(BasicDiagnostic) checker.getDiagnostics(), lifeline);
			besGenerator.setStart((OccurrenceSpecification) message.getReceiveEvent());
			besGenerator.setFinish((OccurrenceSpecification) message.getReceiveEvent());
			besGenerator.generate();
		} else {
			// check if next message conforming with operation
			// TODO check
		}

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
	protected Message getNextMessage(Message message) {
		MessageEnd receiveEvent = message.getReceiveEvent();
		if (receiveEvent instanceof MessageOccurrenceSpecification) {
			Lifeline targetLifeline = (Lifeline) EcoreUtil.getObjectByType(((MessageOccurrenceSpecification) receiveEvent).getCovereds(), Literals.LIFELINE);
			if (null != targetLifeline) {
				Collection<MessageOccurrenceSpecification> targetSpecsCol = EcoreUtil.getObjectsByType(targetLifeline.getCoveredBys(),
						Literals.MESSAGE_OCCURRENCE_SPECIFICATION);
				ArrayList<MessageOccurrenceSpecification> targetSpecs = new ArrayList<MessageOccurrenceSpecification>(targetSpecsCol);
				int sourceIndex = targetSpecs.indexOf(receiveEvent);
				if ((sourceIndex >= 0) && (sourceIndex + 1 != targetSpecs.size())) {
					MessageOccurrenceSpecification previousEnd = targetSpecs.get(sourceIndex + 1);
					return previousEnd.getMessage();
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
	protected Message getPreviousMessage(Message message) {
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
