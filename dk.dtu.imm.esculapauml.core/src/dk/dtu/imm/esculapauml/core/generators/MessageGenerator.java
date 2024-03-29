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
import dk.dtu.imm.esculapauml.core.executors.InteractionSequencer;

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
	private InteractionSequencer sequencer;

	/**
	 * @param checker
	 * @param sourceLifeline2
	 * @param targetLifeline2
	 */
	public MessageGenerator(Checker checker, Lifeline sourceLifeline, Lifeline targetLifeline, InteractionSequencer sequencer) {
		super(checker);
		logger = Logger.getLogger(MessageGenerator.class);
		this.targetLifeline = targetLifeline;
		this.sourceLifeline = sourceLifeline;
		this.sequencer = sequencer;
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
		insertSpecificationAfter(sourceLifeline, eventSend, sentGenerateAfter, generateNewBESForSent);
		eventSend.getCovereds().add(sourceLifeline);
		systemState.addGeneratedElement(eventSend);

		MessageOccurrenceSpecification eventReceive = UMLFactory.eINSTANCE.createMessageOccurrenceSpecification();
		generated.setReceiveEvent(eventReceive);
		eventReceive.setEvent(event);
		eventReceive.setMessage(generated);
		eventReceive.setName("ReceiveMessageOccurrenceSpecificationOf" + generated.getName());
		eventReceive.setEnclosingInteraction(targetLifeline.getInteraction());
		if (setReciveAfterSent) {
			insertSpecificationAfter(targetLifeline, eventReceive, eventSend, generateNewBESForReceive);
		} else {
			insertSpecificationAfter(targetLifeline, eventReceive, receiveGenerateAfter, generateNewBESForReceive);
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

	protected void insertSpecificationAfter(Lifeline lifeline, MessageOccurrenceSpecification toInsert, MessageOccurrenceSpecification after,
			boolean generateNewBES) {
		List<InteractionFragment> allBes = filter(is(BehaviorExecutionSpecification.class), lifeline.getCoveredBys());
		if (null == after) {
			// generate at the end but always before BES
			if (allBes.isEmpty()) {
				// insert in the end
				lifeline.getCoveredBys().add(toInsert);
				if (generateNewBES) {
					// insert new and only bes
					BehaviorExecutionSpecificationGenerator besGenerator = new BehaviorExecutionSpecificationGenerator(systemState, diagnostic, lifeline);
					besGenerator.setPosition(BehaviorExecutionSpecificationGenerator.POSITION_END);
					besGenerator.setStartAndFinish(toInsert);
					besGenerator.generate();
				}
			} else {
				if (extendBehavorExecutionSpecificationsIfNecessary) {
					// we need to know where to generate bes, we should generate
					// bes after the last executed bes or; if no bes was
					// executed, at the beginning
					BehaviorExecutionSpecification bes = findLastExecutedBES(allBes);
					int insertIndex;
					if(null == bes) {
						insertIndex = 0;
					} else {
						insertIndex = lifeline.getCoveredBys().indexOf(bes.getFinish()) + 1;
					}
					lifeline.getCoveredBys().add(insertIndex, toInsert);
					if (generateNewBES) {
						BehaviorExecutionSpecificationGenerator besGenerator = new BehaviorExecutionSpecificationGenerator(systemState, diagnostic, lifeline);
						// insert after previous spec
						if(null == bes) {
							besGenerator.setPosition(lifeline.getCoveredBys().indexOf(allBes.get(0)));
						} else {
							besGenerator.setPosition(lifeline.getCoveredBys().indexOf(bes) + 1);
						}
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
							if (generateNewBES) {
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
	 * Finds last bes of which start message was executed.
	 * 
	 * @param allBes
	 * @return
	 */
	private BehaviorExecutionSpecification findLastExecutedBES(List<InteractionFragment> allBes) {
		BehaviorExecutionSpecification result = null;
		for (InteractionFragment intfrag : allBes) {
			if (intfrag instanceof BehaviorExecutionSpecification) {
				BehaviorExecutionSpecification bes = (BehaviorExecutionSpecification) intfrag;
				if (sequencer.wasExecuted(((MessageOccurrenceSpecification) bes.getStart()).getMessage())) {
					result = bes;
				} else {
					break;
				}
			}
		}
		return result;
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
