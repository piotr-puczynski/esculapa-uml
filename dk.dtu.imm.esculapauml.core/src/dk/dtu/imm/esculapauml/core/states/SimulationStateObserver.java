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

import java.util.Collection;
import java.util.EventListener;

/**
 * Interface for observers of consistency checking. It can be implemented by GUI
 * or tests to react on important events during simulation.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public interface SimulationStateObserver extends EventListener {
	static final int DECISION_TERMINATE_SIMULATION = 0;
	static final int DECISION_EXTERNAL_TRANSITION_CHOICE = 1;
	
	boolean booleanChoice(int typeOfDecision, boolean defaultValue, Object data);
	
	Object multipleChoice(int typeOfDecision, Object defaultValue, @SuppressWarnings("rawtypes") Collection data, Object extra);
}
