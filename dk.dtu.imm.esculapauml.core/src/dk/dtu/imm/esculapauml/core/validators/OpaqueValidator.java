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

import static ch.lambdaj.Lambda.filter;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.not;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.OpaqueExpression;

import dk.dtu.imm.esculapauml.core.executors.InstanceExecutor;

/**
 * Validator validates opaque expressions in all supported languages.
 * 
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.validators.Validator#validateConstraint()
	 */
	/**
	 * Checks that the specified guards are ocl guards and that the language is
	 * specified for all of them.
	 */
	@Override
	public boolean validateConstraint() {
		if (specification.getBodies().isEmpty()) {
			executor.getChecker().addOtherProblem(Diagnostic.WARNING, "OpaqueExpression specified as a constraint has no bodies.", errorContext);
			return true;
		}
		if (specification.getBodies().size() != specification.getLanguages().size()) {
			executor.getChecker().addOtherProblem(Diagnostic.ERROR, "OpaqueExpression specified as a constraint has different number of bodies than languages.", errorContext);
			return false;
		}
		if(!filter(not(equalToIgnoringCase("ocl")), specification.getLanguages()).isEmpty()) {
			executor.getChecker().addOtherProblem(Diagnostic.ERROR, "OpaqueExpression specified as a constraint has unrecognized languages.", errorContext);
			return false;
		}
		OCLValidator ocl = new OCLValidator(executor, constraint);
		ocl.setErrorContext(errorContext);
		return ocl.validateConstraint();
	}

}
