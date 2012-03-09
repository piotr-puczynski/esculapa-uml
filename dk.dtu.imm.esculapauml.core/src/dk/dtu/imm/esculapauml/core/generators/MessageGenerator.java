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

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.BehaviorExecutionSpecification;
import org.eclipse.uml2.uml.CallEvent;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.MessageSort;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.ValueSpecification;

import dk.dtu.imm.esculapauml.core.checkers.Checker;
import dk.dtu.imm.esculapauml.core.states.SystemState;

/**
 * @author Piotr J. Puczynski
 * 
 */
public class MessageGenerator extends AbstractGenerator<Message> {

	private Lifeline sourceLifeline, targetLifeline;
	private MessageOccurrenceSpecification sentGenerateAfter = null, receiveGenerateAfter = null;
	private boolean setReciveAfterSent = false;
	private MessageSort messageSort = MessageSort.SYNCH_CALL_LITERAL;
	private Operation operation = null;
	private boolean extendBehavorExecutionSpecificationsIfNecessary = true;
	private boolean generateNewBESForSent = true;
	private boolean generateNewBESForReceive = true;
	private String customName = null;
	private EList<ValueSpecification> arguments = null;

	/**
	 * @param systemState
	 * @param diagnostic
	 */
	public MessageGenerator(SystemState systemState, BasicDiagnostic diagnostic, Lifeline sourceLifeline, Lifeline targetLifeline) {
		super(systemState, diagnostic);
		logger = Logger.getLogger(MessageGenerator.class);
		this.targetLifeline = targetLifeline;
		this.sourceLifeline = sourceLifeline;
	}

	/**
	 * @param checker
	 * @param sourceLifeline2
	 * @param targetLifeline2
	 */
	public MessageGenerator(Checker checker, Lifeline sourceLifeline, Lifeline targetLifeline) {
		super(checker);
		logger = Logger.getLogger(MessageGenerator.class);
		this.targetLifeline = targetLifeline;
		this.sourceLifeline = sourceLifeline;
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

		targetLifeline.getInteraction().getNearestPackage().getPackagedElements().add(event);
		systemState.addGeneratedElement(event);

		if (null != customName) {
			generated = sourceLifeline.getInteraction().createMessage(customName);
			event.setName("EventOf" + customName);
		} else {
			generated = sourceLifeline.getInteraction().createMessage(getOperationName());
			event.setName("EventOf" + getOperationName());
		}
		generated.setMessageSort(messageSort);
		systemState.addGeneratedElement(generated);

		MessageOccurrenceSpecification eventSend = UMLFactory.eINSTANCE.createMessageOccurrenceSpecification();
		generated.setSendEvent(eventSend);
		eventSend.setEvent(event);
		eventSend.setMessage(generated);
		eventSend.setName("SendMessageOccurrenceSpecificationOf" + generated.getName());
		eventSend.setEnclosingInteraction(targetLifeline.getInteraction());
		insertSpecificationAfter(sourceLifeline, eventSend, sentGenerateAfter);
		eventSend.getCovereds().add(sourceLifeline);
		systemState.addGeneratedElement(eventSend);

		MessageOccurrenceSpecification eventReceive = UMLFactory.eINSTANCE.createMessageOccurrenceSpecification();
		generated.setReceiveEvent(eventReceive);
		eventReceive.setEvent(event);
		eventReceive.setMessage(generated);
		eventReceive.setName("ReceiveMessageOccurrenceSpecificationOf" + generated.getName());
		eventReceive.setEnclosingInteraction(targetLifeline.getInteraction());
		if (setReciveAfterSent) {
			insertSpecificationAfter(targetLifeline, eventReceive, eventSend);
		} else {
			insertSpecificationAfter(targetLifeline, eventReceive, receiveGenerateAfter);
		}
		eventReceive.getCovereds().add(targetLifeline);

		// add arguments if needed
		if (null != arguments) {
			for (ValueSpecification argument : arguments) {
				generated.getArguments().add(EcoreUtil.copy(argument));
			}
		}
		systemState.addGeneratedElement(eventReceive);
		logger.info("Generated new element: " + generated.getLabel());
		return generated;
	}

	protected void insertSpecificationAfter(Lifeline lifeline, MessageOccurrenceSpecification toInsert, MessageOccurrenceSpecification after) {
		List<InteractionFragment> allBes = filter(is(BehaviorExecutionSpecification.class), lifeline.getCoveredBys());
		if (null == after) {
			// generate at the end but always before BES
			if (allBes.isEmpty()) {
				lifeline.getCoveredBys().add(toInsert);
				// somebody else will generate BES in this case
			} else {
				if (extendBehavorExecutionSpecificationsIfNecessary) {
					// get the last available bes
					BehaviorExecutionSpecification bes = (BehaviorExecutionSpecification) ((BehaviorExecutionSpecification) allBes.get(allBes.size() - 1));
					after = (MessageOccurrenceSpecification) bes.getFinish();
					if (generated.getMessageSort() != MessageSort.REPLY_LITERAL && after.getMessage().getMessageSort() == MessageSort.REPLY_LITERAL) {
						BehaviorExecutionSpecificationGenerator besGenerator = new BehaviorExecutionSpecificationGenerator(systemState, diagnostic, lifeline);
						// insert after previous spec
						besGenerator.setPosition(lifeline.getCoveredBys().indexOf(bes) + 1);
						besGenerator.setStartAndFinish(toInsert);
						besGenerator.generate();
					} else {
						// extend existing bes
						bes.setFinish(toInsert);
					}
				} else {
					lifeline.getCoveredBys().add(lifeline.getCoveredBys().indexOf(allBes.get(0)), toInsert);
				}
			}
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
					for (InteractionFragment ifbes : allBes) {
						BehaviorExecutionSpecification bes = (BehaviorExecutionSpecification) ifbes;
						if (bes.getFinish() == after) {
							// if we do not generate reply and "after" is a
							// reply we need to generate new execution for this
							// message on existing lifeline
							if (generated.getMessageSort() != MessageSort.REPLY_LITERAL && after.getMessage().getMessageSort() == MessageSort.REPLY_LITERAL) {
								BehaviorExecutionSpecificationGenerator besGenerator = new BehaviorExecutionSpecificationGenerator(systemState, diagnostic,
										lifeline);
								// insert after previous spec
								besGenerator.setPosition(lifeline.getCoveredBys().indexOf(bes) + 1);
								besGenerator.setStartAndFinish(toInsert);
								besGenerator.generate();
							} else {
								// extend existing bes
								bes.setFinish(toInsert);
							}
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
	 * @param extendBehavorExecutionSpecificationsIfNecessary
	 *            the extendBehavorExecutionSpecificationsIfNecessary to set
	 */
	public void setExtendBehavorExecutionSpecificationsIfNecessary(boolean extendBehavorExecutionSpecificationsIfNecessary) {
		this.extendBehavorExecutionSpecificationsIfNecessary = extendBehavorExecutionSpecificationsIfNecessary;
	}

	/**
	 * @param operation
	 *            the operation to set
	 */
	public void setOperation(Operation operation) {
		this.operation = operation;
	}

	private String getOperationName() {
		if (null != operation) {
			return operation.getName();
		} else {
			return "";
		}
	}

	/**
	 * @param customName
	 *            the customName to set
	 */
	public void setCustomName(String customName) {
		this.customName = customName;
	}

	/**
	 * @param b
	 */
	public void setReceiveAfterSent(boolean b) {
		setReciveAfterSent = b;
	}

	/**
	 * @return the arguments
	 */
	public EList<ValueSpecification> getArguments() {
		return arguments;
	}

	/**
	 * @param arguments
	 *            the arguments to set
	 */
	public void setArguments(EList<ValueSpecification> arguments) {
		this.arguments = arguments;
	}

	/**
	 * @return the generateNewBESForSent
	 */
	public boolean isGenerateNewBESForSent() {
		return generateNewBESForSent;
	}

	/**
	 * @param generateNewBESForSent
	 *            the generateNewBESForSent to set
	 */
	public void setGenerateNewBESForSent(boolean generateNewBESForSent) {
		this.generateNewBESForSent = generateNewBESForSent;
	}

	/**
	 * @return the generateNewBESForReceive
	 */
	public boolean isGenerateNewBESForReceive() {
		return generateNewBESForReceive;
	}

	/**
	 * @param generateNewBESForReceive
	 *            the generateNewBESForReceive to set
	 */
	public void setGenerateNewBESForReceive(boolean generateNewBESForReceive) {
		this.generateNewBESForReceive = generateNewBESForReceive;
	}

}
