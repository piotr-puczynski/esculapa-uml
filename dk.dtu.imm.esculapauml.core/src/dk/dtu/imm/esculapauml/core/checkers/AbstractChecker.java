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
/**
 * @author Piotr J. Puczynski
 *
 * @param <T>
 */
public abstract class AbstractChecker<T> implements Checker {

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
	
	AbstractChecker(Checker checker, T objectToCheck) {
		diagnostics = (BasicDiagnostic) checker.getDiagnostics();
		checkee = objectToCheck;
		this.systemState = checker.getSystemState();
	}


	/* (non-Javadoc)
	 * @see dk.dtu.imm.esculapauml.core.checkers.CheckerInterface#addProblem(int, java.lang.String)
	 */
	public BasicDiagnostic addProblem(int severity, String message) {
		logger.warn("New diagnostic with severity: " + severity + " and message: " + message);
		BasicDiagnostic result = new BasicDiagnostic(severity, ESCULAPA_NAMESPACE, 0, message, new Object[] { checkee });
		diagnostics.add(result);
		return result;
	}

	/* (non-Javadoc)
	 * @see dk.dtu.imm.esculapauml.core.checkers.CheckerInterface#addOtherProblem(int, java.lang.String, java.lang.Object[])
	 */
	public BasicDiagnostic addOtherProblem(int severity, String message, Object... objects) {
		logger.warn("New diagnostic with severity: " + severity + " and message: " + message);
		BasicDiagnostic result = new BasicDiagnostic(severity, ESCULAPA_NAMESPACE, 0, message, objects);
		diagnostics.add(result);
		return result;
	}

	/* (non-Javadoc)
	 * @see dk.dtu.imm.esculapauml.core.checkers.CheckerInterface#getDiagnostics()
	 */
	public Diagnostic getDiagnostics() {
		return diagnostics;
	}

	/* (non-Javadoc)
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
	
	/* (non-Javadoc)
	 * @see dk.dtu.imm.esculapauml.core.checkers.CheckerInterface#getSystemState()
	 */
	public SystemState getSystemState() {
		return systemState;
	}
}
