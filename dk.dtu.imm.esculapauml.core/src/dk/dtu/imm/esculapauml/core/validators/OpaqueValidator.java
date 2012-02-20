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
import org.eclipse.uml2.uml.OpaqueExpression;

import dk.dtu.imm.esculapauml.core.executors.InstanceExecutor;

/**
 * Validator validates opaque expressions in all supported languages.
 * @author Piotr J. Puczynski
 *
 */
public class OpaqueValidator extends AbstractValidator implements Validator {

	
	protected OpaqueExpression specification;
	/**
	 * @param executor
	 * @param constraint
	 */
	public OpaqueValidator(InstanceExecutor executor, Constraint constraint) {
		super(executor, constraint);
		specification = (OpaqueExpression) constraint.getSpecification();
	}

	/* (non-Javadoc)
	 * @see dk.dtu.imm.esculapauml.core.validators.Validator#validateConstraint()
	 */
	@Override
	public boolean validateConstraint() {
		// EList<String> langs = new BasicEList<String>(specification.getLanguages());
		// TODO: process many languages
		OCLValidator ocl = new OCLValidator(executor, constraint);
		return ocl.validateConstraint();
	}

}
