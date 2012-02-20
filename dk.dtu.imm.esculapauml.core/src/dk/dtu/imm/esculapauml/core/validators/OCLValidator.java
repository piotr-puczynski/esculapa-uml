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
package dk.dtu.imm.esculapauml.core.validators;

import org.eclipse.uml2.uml.Constraint;

import dk.dtu.imm.esculapauml.core.checkers.Checker;

/**
 * Guard validator is used to evaluate OCL guards constraints.
 * @author Piotr J. Puczynski
 *
 */
public class OCLValidator implements Validator {

	/**
	 * @param checker
	 * @param constraint
	 */
	public OCLValidator(Checker checker, Constraint constraint) {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see dk.dtu.imm.esculapauml.core.validators.Validator#validateConstraint()
	 */
	@Override
	public boolean validateConstraint() {
		// TODO Auto-generated method stub
		return false;
	}

}
