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
 * Interaction diagnostic check of common features for all objects
 * 
 * @author Piotr. J. Puczynski (piotr.puczynski)
 * 
 */
public abstract class AbstractChecker implements CheckerInterface {
	AbstractChecker() {
		diagnostics = new BasicDiagnostic();
	}

	protected BasicDiagnostic diagnostics;

	/**
	 * This data structure contains an overview of all occurred errors.
	 * 
	 * @return the diagnostics
	 */
	public Diagnostic getDiagnostics() {
		return diagnostics;
	}

	public boolean hasErrors() {
		return ((diagnostics.getSeverity() == Diagnostic.ERROR) || (diagnostics.getSeverity() == Diagnostic.CANCEL));
	}
}
