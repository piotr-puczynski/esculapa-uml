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
package dk.dtu.imm.esculapauml.gui.topcased.observer;

import dk.dtu.imm.esculapauml.core.states.SimulationStateObserver;

/**
 * @author Piotr J. Puczynski
 * 
 */
public class SimulationObserver implements SimulationStateObserver {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.states.SimulationStateObserver#booleanChoice
	 * (int, boolean, java.lang.Object)
	 */
	@Override
	public boolean booleanChoice(int typeOfDecision, boolean defaultValue, Object data) {
		if (DECISION_TERMINATE_SIMULATION == typeOfDecision) {

		}
		return defaultValue;
	}

}
