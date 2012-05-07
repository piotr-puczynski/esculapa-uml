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
package dk.dtu.imm.esculapauml.core.executors.scheduler;

import java.util.LinkedList;
import java.util.Queue;

import dk.dtu.imm.esculapauml.core.executors.coordination.EsculapaCallEvent;

/**
 * Schedules the execution of the asynchronous events in the system.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class Scheduler {

	protected Queue<EsculapaCallEvent> eventsQueue = new LinkedList<EsculapaCallEvent>();

	public void enqueue(EsculapaCallEvent event) {
		eventsQueue.offer(event);
	}

	/**
	 * Executes the first event from the queue. We do not need looping here, it
	 * is recursively looped when we call the callOperation.
	 */
	public void executeFromQueue() {
		if (!eventsQueue.isEmpty()) {
			EsculapaCallEvent event = eventsQueue.poll();
			event.getTarget().callOperation(event);
		}

	}

}
