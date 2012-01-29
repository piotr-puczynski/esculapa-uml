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

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;

import dk.dtu.imm.esculapauml.core.states.SystemState;

/**
 * Abstract diagnostic check of common features for any object
 * 
 * @author Piotr. J. Puczynski (piotr.puczynski)
 * 
 */
public abstract class AbstractChecker<T> implements CheckerInterface {
	public final static String ESCULAPA_NAMESPACE = "dk.dtu.imm.esculapauml";

	protected T checkee;
	protected BasicDiagnostic diagnostics;
	protected SystemState systemState;
	protected Logger logger = Logger.getLogger(AbstractChecker.class);

	AbstractChecker(SystemState systemState, T objectToCheck) {
		diagnostics = new BasicDiagnostic(Diagnostic.OK, ESCULAPA_NAMESPACE, 0, "", null);
		checkee = objectToCheck;
		this.systemState = systemState;
	}

	AbstractChecker(SystemState systemState, BasicDiagnostic existingDiagnostics, T objectToCheck) {
		diagnostics = existingDiagnostics;
		checkee = objectToCheck;
		this.systemState = systemState;
	}

	/**
	 * Basic function to add problem to diagnostics for its own object to check.
	 * Mostly used internally but it is possible to call it from outside too.
	 * 
	 * @param severity
	 * @param message
	 */
	public void addProblem(int severity, String message) {
		logger.warn("New diagnostic with severity: " + severity + " and message: " + message);
		diagnostics.add(new BasicDiagnostic(severity, ESCULAPA_NAMESPACE, 0, message, new Object[] { checkee }));
	}
	
	/**
	 * Function to add problem to diagnostics containing other object(s).
	 * 
	 * @param severity
	 * @param message
	 */
	public void addOtherProblem(int severity, String message, Object... objects) {
		logger.warn("New diagnostic with severity: " + severity + " and message: " + message);
		diagnostics.add(new BasicDiagnostic(severity, ESCULAPA_NAMESPACE, 0, message, objects));
	}

	/**
	 * This data structure contains an overview of all occurred errors.
	 * 
	 * @return the diagnostics
	 */
	public Diagnostic getDiagnostics() {
		return diagnostics;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dk.dtu.imm.esculapauml.core.checkers.CheckerInterface#hasErrors()
	 */
	public boolean hasErrors() {
		return ((diagnostics.getSeverity() == Diagnostic.ERROR) || (diagnostics.getSeverity() == Diagnostic.CANCEL));
	}

	/**
	 * Returns checkee (checked object)
	 * 
	 * @return
	 */
	public T getCheckedObject() {
		return checkee;
	}
	
	/**
	 * returns system state
	 * @return
	 */
	public SystemState getSystemState() {
		return systemState;
	}
}
