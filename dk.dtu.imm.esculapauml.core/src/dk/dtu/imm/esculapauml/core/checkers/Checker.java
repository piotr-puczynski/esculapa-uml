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
import org.eclipse.emf.common.util.Diagnostic;

import dk.dtu.imm.esculapauml.core.states.SystemState;

/**
 * The interface for checkers.
 * @author Piotr. J. Puczynski (piotr.puczynski)
 *
 */
public interface Checker {
	public final static String ESCULAPA_NAMESPACE = "dk.dtu.imm.esculapauml";
	/**
	 * Start checking.
	 */
	void check();
	/**
	 * @return if the checked element has errors
	 */
	boolean hasErrors();
	
	/**
	 * Basic function to add problem to diagnostics for its own object to check.
	 * Mostly used internally but it is possible to call it from outside too.
	 * 
	 * @param severity
	 * @param message
	 */
	BasicDiagnostic addProblem(int severity, String message);
	
	/**
	 * Function to add problem to diagnostics containing other object(s).
	 * 
	 * @param severity
	 * @param message
	 */
	BasicDiagnostic addOtherProblem(int severity, String message, Object... objects);
	
	/**
	 * This data structure contains an overview of all occurred errors.
	 * 
	 * @return the diagnostics
	 */
	Diagnostic getDiagnostics();
	
	/**
	 * returns system state
	 * @return
	 */
	SystemState getSystemState();
}
