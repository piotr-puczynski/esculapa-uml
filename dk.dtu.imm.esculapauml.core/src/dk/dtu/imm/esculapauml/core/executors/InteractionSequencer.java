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

import org.eclipse.uml2.uml.Message;

import dk.dtu.imm.esculapauml.core.executors.coordination.EsculapaCallEvent;
import dk.dtu.imm.esculapauml.core.executors.coordination.EsculapaReplyEvent;

/**
 * Tracks the interaction execution as a sequence of messages. Knows about the
 * mapping between calls and replies.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class InteractionSequencer {

	protected Map<Long, Message> sequencer = new HashMap<Long, Message>();
	// key is reply, call is value
	protected Map<Message, Message> replies = new HashMap<Message, Message>();

	public void addEvent(EsculapaCallEvent event, Message message) {
		sequencer.put(event.getSequenceId(), message);
	}

	public void addEvent(EsculapaReplyEvent event, Message message) {
		sequencer.put(event.getSequenceId(), message);
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

	public void printSequence() {
		System.out.println("Sequence of events: ");
		long i = 1;
		for (Message message : sequencer.values()) {
			System.out.println(i++ + ": " + message.toString());
		}
		System.out.println("Simulation finished.");
	}

}
