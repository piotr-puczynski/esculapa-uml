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

import org.eclipse.ocl.OCLInput;
import org.eclipse.ocl.uml.UMLEnvironment;
import org.eclipse.ocl.uml.UMLEnvironmentFactory;
import org.eclipse.uml2.uml.Constraint;

import dk.dtu.imm.esculapauml.core.executors.InstanceExecutor;

/**
 * Guard validator is used to evaluate OCL guards constraints.
 * @author Piotr J. Puczynski
 *
 */
public class OCLValidator extends AbstractValidator implements Validator {

	/**
	 * @param executor
	 * @param constraint
	 */
	public OCLValidator(InstanceExecutor executor, Constraint constraint) {
		super(executor, constraint);
	}

	/* (non-Javadoc)
	 * @see dk.dtu.imm.esculapauml.core.validators.Validator#validateConstraint()
	 */
	@Override
	public boolean validateConstraint() {
		//OCLInput document = new OCLInput(in);
		UMLEnvironmentFactory envFactory = new UMLEnvironmentFactory(constraint.getModel().eResource().getResourceSet());
		UMLEnvironment env = envFactory.createEnvironment();
		org.eclipse.ocl.uml.OCL myOCL = org.eclipse.ocl.uml.OCL.newInstance(env);
		boolean check = myOCL.check(executor.getInstanceSpecification(), constraint);
		return false;
	}

}
