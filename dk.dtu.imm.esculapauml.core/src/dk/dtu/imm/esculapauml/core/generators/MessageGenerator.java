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
package dk.dtu.imm.esculapauml.core.generators;

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.uml2.uml.CallEvent;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.MessageSort;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.UMLFactory;

import dk.dtu.imm.esculapauml.core.states.SystemState;

/**
 * @author Piotr J. Puczynski
 * 
 */
public class MessageGenerator extends AbstractGenerator<Message> {

	private Lifeline sourceLifeline, targetLifeline;
	private MessageOccurrenceSpecification sentGenerateAfter = null, receiveGenerateAfter = null;
	private MessageSort messageSort = MessageSort.SYNCH_CALL_LITERAL;
	private Operation operation;

	/**
	 * @param systemState
	 * @param diagnostic
	 */
	public MessageGenerator(SystemState systemState, BasicDiagnostic diagnostic, Operation operation, Lifeline sourceLifeline, Lifeline targetLifeline) {
		super(systemState, diagnostic);
		this.targetLifeline = targetLifeline;
		this.sourceLifeline = sourceLifeline;
		this.operation = operation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dk.dtu.imm.esculapauml.core.generators.GeneratorInterface#generate()
	 */
	@Override
	public Message generate() {
		// first lets generate event for operation
		CallEvent event = UMLFactory.eINSTANCE.createCallEvent();
		event.setOperation(operation);
		event.setName("EventOf" + operation.getName());
		targetLifeline.getInteraction().getNearestPackage().getPackagedElements().add(event);
		systemState.addGeneratedElement(event);

		Message result = sourceLifeline.getInteraction().createMessage("MessageOf" + operation.getLabel());
		result.setMessageSort(messageSort);
		systemState.addGeneratedElement(result);

		MessageOccurrenceSpecification eventSend = UMLFactory.eINSTANCE.createMessageOccurrenceSpecification();
		result.setSendEvent(eventSend);
		eventSend.setEvent(event);
		eventSend.setMessage(result);
		eventSend.setName("SendMessageOccurrenceSpecificationOf" + operation.getName());
		eventSend.setEnclosingInteraction(targetLifeline.getInteraction());
		insertSpecificationAfter(sourceLifeline, eventSend, sentGenerateAfter);
		eventSend.getCovereds().add(sourceLifeline);
		systemState.addGeneratedElement(eventSend);

		MessageOccurrenceSpecification eventReceive = UMLFactory.eINSTANCE.createMessageOccurrenceSpecification();
		result.setReceiveEvent(eventReceive);
		eventReceive.setEvent(event);
		eventReceive.setMessage(result);
		eventReceive.setName("ReceiveMessageOccurrenceSpecificationOf" + operation.getName());
		eventReceive.setEnclosingInteraction(targetLifeline.getInteraction());
		insertSpecificationAfter(targetLifeline, eventReceive, receiveGenerateAfter);
		eventReceive.getCovereds().add(targetLifeline);
		systemState.addGeneratedElement(eventReceive);
		return result;
	}

	protected void insertSpecificationAfter(Lifeline lifeline, MessageOccurrenceSpecification toInsert, MessageOccurrenceSpecification after) {
		if (null == after) {
			// generate at the end
			lifeline.getCoveredBys().add(toInsert);
		} else {
			int insertIndex = lifeline.getCoveredBys().indexOf(after);
			if (-1 == insertIndex) {
				lifeline.getCoveredBys().add(toInsert);
			} else {
				lifeline.getCoveredBys().add(insertIndex + 1, toInsert);
			}
		}
	}

	/**
	 * @param sentGenerateAfter
	 *            the sentGenerateAfter to set
	 */
	public void setSentGenerateAfter(MessageOccurrenceSpecification sentGenerateAfter) {
		this.sentGenerateAfter = sentGenerateAfter;
	}

	/**
	 * @param receiveGenerateAfter
	 *            the receiveGenerateAfter to set
	 */
	public void setReceiveGenerateAfter(MessageOccurrenceSpecification receiveGenerateAfter) {
		this.receiveGenerateAfter = receiveGenerateAfter;
	}

	/**
	 * @param messageSort
	 *            the messageSort to set
	 */
	public void setMessageSort(MessageSort messageSort) {
		this.messageSort = messageSort;
	}

}
