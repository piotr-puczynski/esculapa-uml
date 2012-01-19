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
import org.eclipse.uml2.uml.ConnectableElement;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Type;

/**
 * Checker for lifelines
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class LifelineChecker extends AbstractChecker<Lifeline> {
	/**
	 * @param existingDiagnostics
	 */
	public LifelineChecker(BasicDiagnostic existingDiagnostics, Lifeline lifeline) {
		super(existingDiagnostics, lifeline);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dk.dtu.imm.esculapauml.core.checkers.CheckerInterface#check()
	 */
	@Override
	public void check() {
		structuralExistenceCheck();
	}

	/**
	 * Check if lifeline corresponds to some class in the model
	 */
	protected void structuralExistenceCheck() {
		ConnectableElement connection = checkee.getRepresents();
		// representant is not set at all
		if (null == connection) {
			addProblem(Diagnostic.ERROR, "The Lifeline " + checkee.getLabel() + " has no representant.");
		} else {
			Type type = connection.getType();
			// representant set to nothing
			if (null == type) {
				addProblem(Diagnostic.ERROR, "The Lifeline " + checkee.getLabel() + " has no representant set to any type.");
			}
		}
	}

}
