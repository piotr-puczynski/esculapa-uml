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

	AbstractChecker(T objectToCheck) {
		diagnostics = new BasicDiagnostic(Diagnostic.OK, ESCULAPA_NAMESPACE, 0, "", null);
		checkee = objectToCheck;
	}

	AbstractChecker(BasicDiagnostic existingDiagnostics, T objectToCheck) {
		diagnostics = existingDiagnostics;
		checkee = objectToCheck;
	}

	/**
	 * Basic function to add problem to diagnostics for its own object to check.
	 * Mostly used internally but it is possible to call it from outside too.
	 * 
	 * @param severity
	 * @param message
	 */
	public void addProblem(int severity, String message) {
		diagnostics.add(new BasicDiagnostic(severity, ESCULAPA_NAMESPACE, 0, message, new Object[] { checkee }));
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
}
