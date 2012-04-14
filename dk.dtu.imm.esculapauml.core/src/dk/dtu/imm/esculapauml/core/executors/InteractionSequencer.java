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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;

import dk.dtu.imm.esculapauml.core.executors.coordination.EsculapaCallEvent;
import dk.dtu.imm.esculapauml.core.executors.coordination.EsculapaReplyEvent;
import dk.dtu.imm.esculapauml.core.utils.InteractionUtils;

/**
 * Tracks the interaction execution as a sequence of messages. Knows about the
 * mapping between calls and replies. Tracks the execution progress on separate
 * lifelines. This information is used in case of more complex interactions
 * where we need to know what is the last event on each lifeline that was
 * already generated or executed.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class InteractionSequencer {

	protected Map<Long, Message> sequencer = new HashMap<Long, Message>();
	// key is reply, call is value
	protected Map<Message, Message> replies = new HashMap<Message, Message>();
	protected Map<Lifeline, MessageOccurrenceSpecification> progress = new HashMap<Lifeline, MessageOccurrenceSpecification>();

	/**
	 * @param message
	 */
	private void trackProgress(Message message) {
		progress.put(InteractionUtils.getMessageSourceLifeline(message), (MessageOccurrenceSpecification) message.getSendEvent());
		progress.put(InteractionUtils.getMessageTargetLifeline(message), (MessageOccurrenceSpecification) message.getReceiveEvent());
	}

	MessageOccurrenceSpecification getLastOccurrenceOnLifeline(Lifeline lifeline) {
		return progress.get(lifeline);
	}

	Message getLastMessageOnLifeline(Lifeline lifeline) {
		MessageOccurrenceSpecification mos = progress.get(lifeline);
		if (null != mos) {
			mos.getMessage();
		}
		return null;
	}

	public void addEvent(EsculapaCallEvent event, Message message) {
		sequencer.put(event.getSequenceId(), message);
		trackProgress(message);
	}

	public void addEvent(EsculapaReplyEvent event, Message message) {
		sequencer.put(event.getSequenceId(), message);
		trackProgress(message);
		Message call = sequencer.get(event.getInitiatingCallSequenceNumber());
		if (null != call) {
			replies.put(message, call);
		}
	}

	public Message getReplyFor(Message call) {
		for (Entry<Message, Message> entry : replies.entrySet()) {
			if (entry.getValue() == call) {
				return entry.getKey();
			}
		}
		return null;
	}

	public Message getCallFor(Message reply) {
		return replies.get(reply);
	}

	public Message getMessageWithSequence(Long number) {
		return sequencer.get(number);
	}

	public boolean wasExecuted(Message message) {
		return sequencer.values().contains(message);
	}

	public void printSequence() {
		System.out.println("Sequence of events: ");
		long i = 1;
		for (Message message : sequencer.values()) {
			System.out.println(i++ + ": name: " + message.getLabel() + ", type: " + message.getMessageSort() + " (from "
					+ InteractionUtils.getMessageSourceLifeline(message).getLabel() + " to " + InteractionUtils.getMessageTargetLifeline(message).getLabel()
					+ ")");
		}
		System.out.println("Simulation finished.");
	}

}
