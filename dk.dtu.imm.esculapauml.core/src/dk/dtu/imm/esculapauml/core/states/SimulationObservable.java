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
package dk.dtu.imm.esculapauml.core.states;

import javax.swing.event.EventListenerList;

/**
 * The class is an observable for all listeners that track the simulation, i.e.
 * GUI or tests.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class SimulationObservable {
	private EventListenerList listenerList = new EventListenerList();

	/**
	 * Adds new simulation listener as observer of the execution.
	 * 
	 * @param listener
	 */
	public void addListener(SimulationStateObserver listener) {
		listenerList.add(SimulationStateObserver.class, listener);
	}

	/**
	 * Removes the simulation listener from the observers list.
	 * 
	 * @param listener
	 */
	public void removeListener(SimulationStateObserver listener) {
		listenerList.remove(SimulationStateObserver.class, listener);
	}

	/**
	 * Notify observers of booleanChoice needed and calculate result.
	 * 
	 * @param callEvent
	 */
	public boolean booleanChoice(int typeOfDecision, boolean defaultValue, Object data) {
		for (SimulationStateObserver listener : listenerList.getListeners(SimulationStateObserver.class)) {
			if(defaultValue != listener.booleanChoice(typeOfDecision, defaultValue, data)) {
				return !defaultValue;
			}
		}
		return defaultValue;
	}
	
	/**
	 * Notify observers of booleanChoice needed and calculate result.
	 * 
	 * @param callEvent
	 */
	public Object multipleChoice(int typeOfDecision, Object defaultValue, Object[] data) {
		for (SimulationStateObserver listener : listenerList.getListeners(SimulationStateObserver.class)) {
			Object result = listener.multipleChoice(typeOfDecision, defaultValue, data);
			if(defaultValue != result) {
				return result;
			}
		}
		return defaultValue;
	}
}
