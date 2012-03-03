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
package dk.dtu.imm.esculapauml.core.executors.coordination;

import javax.swing.event.EventListenerList;

/**
 * The class is an observable for all listeners that track the execution process
 * of state machines, e.g. interactions, debuggers, etc.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class ExecutionCoordinator {
	private EventListenerList listenerList = new EventListenerList();

	/**
	 * Adds new execution listener as observer of the execution.
	 * 
	 * @param listener
	 */
	public void addExecutionListener(ExecutionListener listener) {
		listenerList.add(ExecutionListener.class, listener);
	}

	/**
	 * Notify observers of new call event that occurred.
	 * 
	 * @param callEvent
	 */
	public void fireCallEvent(EsculapaCallEvent callEvent) {
		for (ExecutionListener listener : listenerList.getListeners(ExecutionListener.class)) {
			listener.callEventOccurred(callEvent);
		}
	}
}
