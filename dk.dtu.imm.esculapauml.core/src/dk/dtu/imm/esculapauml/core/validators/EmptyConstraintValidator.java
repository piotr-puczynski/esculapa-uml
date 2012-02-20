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

/**
 * Empty constraint validator always evaluates to true.
 * @author Piotr J. Puczynski
 *
 */
public class EmptyConstraintValidator implements Validator {
	
	protected Constraint constraint;

	/**
	 * @param constraint
	 */
	public EmptyConstraintValidator(Constraint constraint) {
		super();
		this.constraint = constraint;
	}

	/* (non-Javadoc)
	 * @see dk.dtu.imm.esculapauml.core.validators.Validator#validateConstraint()
	 */
	@Override
	public boolean validateConstraint() {
		return true;
	}

	/* (non-Javadoc)
	 * @see dk.dtu.imm.esculapauml.core.validators.Validator#getConstraint()
	 */
	@Override
	public Constraint getConstraint() {
		return constraint;
	}

}
