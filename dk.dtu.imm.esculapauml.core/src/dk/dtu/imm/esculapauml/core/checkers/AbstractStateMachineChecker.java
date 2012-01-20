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
package dk.dtu.imm.esculapauml.core.checkers;

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.uml2.uml.StateMachine;

/**
 * Base class for StateMachine checkers
 * @author Piotr J. Puczynski
 *
 */
public abstract class AbstractStateMachineChecker extends AbstractChecker<StateMachine>  {

	/**
	 * @param existingDiagnostics
	 * @param objectToCheck
	 */
	AbstractStateMachineChecker(BasicDiagnostic existingDiagnostics, StateMachine objectToCheck) {
		super(existingDiagnostics, objectToCheck);
	}

	/**
	 * @param objectToCheck
	 */
	public AbstractStateMachineChecker(StateMachine objectToCheck) {
		super(objectToCheck);
	}
	
	

}
