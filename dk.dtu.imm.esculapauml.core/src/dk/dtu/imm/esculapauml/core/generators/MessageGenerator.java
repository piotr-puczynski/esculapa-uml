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

import static ch.lambdaj.Lambda.filter;
import static org.hamcrest.Matchers.is;

import java.util.List;

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.uml2.uml.BehaviorExecutionSpecification;
import org.eclipse.uml2.uml.CallEvent;
import org.eclipse.uml2.uml.InteractionFragment;
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
	private boolean extendBehavorExecutionSpecificationsIfNecessary = true;

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

		generated = sourceLifeline.getInteraction().createMessage("MessageOf" + operation.getLabel());
		generated.setMessageSort(messageSort);
		systemState.addGeneratedElement(generated);

		MessageOccurrenceSpecification eventSend = UMLFactory.eINSTANCE.createMessageOccurrenceSpecification();
		generated.setSendEvent(eventSend);
		eventSend.setEvent(event);
		eventSend.setMessage(generated);
		eventSend.setName("SendMessageOccurrenceSpecificationOf" + operation.getName());
		eventSend.setEnclosingInteraction(targetLifeline.getInteraction());
		insertSpecificationAfter(sourceLifeline, eventSend, sentGenerateAfter);
		eventSend.getCovereds().add(sourceLifeline);
		systemState.addGeneratedElement(eventSend);

		MessageOccurrenceSpecification eventReceive = UMLFactory.eINSTANCE.createMessageOccurrenceSpecification();
		generated.setReceiveEvent(eventReceive);
		eventReceive.setEvent(event);
		eventReceive.setMessage(generated);
		eventReceive.setName("ReceiveMessageOccurrenceSpecificationOf" + operation.getName());
		eventReceive.setEnclosingInteraction(targetLifeline.getInteraction());
		insertSpecificationAfter(targetLifeline, eventReceive, receiveGenerateAfter);
		eventReceive.getCovereds().add(targetLifeline);
		systemState.addGeneratedElement(eventReceive);
		logger.info("Generated new element: " + generated.getLabel());
		return generated;
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
				// we need to make sure that any execution specification do not
				// point with finish to after
				// if yes, we need to update it with toInsert
				if (extendBehavorExecutionSpecificationsIfNecessary) {
					List<InteractionFragment> allBes = filter(is(BehaviorExecutionSpecification.class), lifeline.getCoveredBys());
					for (InteractionFragment ifbes : allBes) {
						BehaviorExecutionSpecification bes = (BehaviorExecutionSpecification) ifbes;
						if (bes.getFinish() == after) {
							bes.setFinish(toInsert);
							break;
						}
					}
				}
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

	/**
	 * @param extendBehavorExecutionSpecificationsIfNecessary the extendBehavorExecutionSpecificationsIfNecessary to set
	 */
	public void setExtendBehavorExecutionSpecificationsIfNecessary(boolean extendBehavorExecutionSpecificationsIfNecessary) {
		this.extendBehavorExecutionSpecificationsIfNecessary = extendBehavorExecutionSpecificationsIfNecessary;
	}
	
	

}
