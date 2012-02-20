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

import dk.dtu.imm.esculapauml.core.checkers.Checker;

/**
 * The interface for executors.
 * Executor is associated with checker for that object class.
 * @author Piotr J. Puczynski
 *
 */
public interface Executor {
	
	/**
	 * Prepare the execution.
	 */
	void prepare();
	
	/**
	 * get corresponding / owning checker
	 */
	Checker getChecker();
}
