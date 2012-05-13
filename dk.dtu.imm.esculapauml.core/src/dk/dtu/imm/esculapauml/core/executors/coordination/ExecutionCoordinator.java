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

import java.util.EventListener;

import javax.swing.event.EventListenerList;

/**
 * The class is an observable for all listeners that track the execution process
 * of state machines, e.g. interactions, debuggers, etc.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class ExecutionCoordinator {
	private long currentSequenceId = 0;
	private EventListenerList listenerList = new EventListenerList();

	/**
	 * @param callEvent
	 */
	private void assignSequenceNumber(EsculapaEvent event) {
		event.setSequenceId(currentSequenceId++);
	}

	/**
	 * Adds new execution listener as observer of the execution.
	 * 
	 * @param listener
	 */
	public void addExecutionListener(EventListener listener) {
		if (listener instanceof ExecutionCallListener) {
			listenerList.add(ExecutionCallListener.class, (ExecutionCallListener) listener);
		}
		if (listener instanceof ExecutionReplyListener) {
			listenerList.add(ExecutionReplyListener.class, (ExecutionReplyListener) listener);
		}
		if (listener instanceof ExecutionCallReturnControlListener) {
			listenerList.add(ExecutionCallReturnControlListener.class, (ExecutionCallReturnControlListener) listener);
		}
		if (listener instanceof ExecutionCompletionListener) {
			listenerList.add(ExecutionCompletionListener.class, (ExecutionCompletionListener) listener);
		}
	}

	/**
	 * Removes the execution listener from the observers list.
	 * 
	 * @param listener
	 */
	public void removeExecutionListener(EventListener listener) {
		if (listener instanceof ExecutionCallListener) {
			listenerList.remove(ExecutionCallListener.class, (ExecutionCallListener) listener);
		}
		if (listener instanceof ExecutionReplyListener) {
			listenerList.remove(ExecutionReplyListener.class, (ExecutionReplyListener) listener);
		}
		if (listener instanceof ExecutionCallReturnControlListener) {
			listenerList.remove(ExecutionCallReturnControlListener.class, (ExecutionCallReturnControlListener) listener);
		}
		if (listener instanceof ExecutionCompletionListener) {
			listenerList.remove(ExecutionCompletionListener.class, (ExecutionCompletionListener) listener);
		}
	}

	/**
	 * Notify observers of new call event that occurred.
	 * 
	 * @param callEvent
	 */
	public void fireEvent(EsculapaCallEvent callEvent) {
		if (!callEvent.isSent()) {
			assignSequenceNumber(callEvent);
			callEvent.setSent(true);
			for (ExecutionCallListener listener : listenerList.getListeners(ExecutionCallListener.class)) {
				listener.callEventOccurred(callEvent);
			}
		}
	}

	/**
	 * Notify observers of new reply event that occurred.
	 * 
	 * @param replyEvent
	 */
	public void fireEvent(EsculapaReplyEvent replyEvent) {
		if (!replyEvent.isSent()) {
			assignSequenceNumber(replyEvent);
			replyEvent.setSent(true);
			for (ExecutionReplyListener listener : listenerList.getListeners(ExecutionReplyListener.class)) {
				listener.replyEventOccurred(replyEvent);
			}
		}
	}

	/**
	 * Notify observers of control flow event that occurred.
	 * 
	 * @param replyEvent
	 */
	public void fireEvent(EsculapaCallReturnControlEvent controlEvent) {
		if (!controlEvent.isSent()) {
			assignSequenceNumber(controlEvent);
			controlEvent.setSent(true);
			for (ExecutionCallReturnControlListener listener : listenerList.getListeners(ExecutionCallReturnControlListener.class)) {
				listener.callReturnControlEventOccurred(controlEvent);
			}
		}
	}
	
	/**
	 * Notify observers of completion event that occurred.
	 * 
	 * @param replyEvent
	 */
	public void fireEvent(EsculapaCompletionEvent completionEvent) {
		if (!completionEvent.isSent()) {
			assignSequenceNumber(completionEvent);
			completionEvent.setSent(true);
			for (ExecutionCompletionListener listener : listenerList.getListeners(ExecutionCompletionListener.class)) {
				listener.completionEventOccurred(completionEvent);
			}
		}
	}
}
