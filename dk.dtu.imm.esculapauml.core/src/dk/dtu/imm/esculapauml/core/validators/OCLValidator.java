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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.ocl.EvaluationEnvironment;
import org.eclipse.ocl.OCLInput;
import org.eclipse.ocl.ParserException;
import org.eclipse.ocl.helper.ConstraintKind;
import org.eclipse.ocl.helper.OCLHelper;
import org.eclipse.ocl.options.EvaluationOptions;
import org.eclipse.ocl.uml.OCL.Query;
import org.eclipse.ocl.uml.OCLExpression;
import org.eclipse.ocl.uml.UMLEnvironment;
import org.eclipse.ocl.uml.UMLEnvironmentFactory;
import org.eclipse.ocl.uml.UMLEvaluationEnvironment;
import org.eclipse.ocl.uml.options.EvaluationMode;
import org.eclipse.ocl.uml.options.UMLEvaluationOptions;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.OpaqueExpression;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Property;

import dk.dtu.imm.esculapauml.core.executors.InstanceExecutor;

/**
 * Guard validator is used to evaluate OCL guards constraints.
 * @author Piotr J. Puczynski
 *
 */
public class OCLValidator extends AbstractValidator implements Validator {

	protected OpaqueExpression specification;

	/**
	 * @param executor
	 * @param constraint
	 */
	public OCLValidator(InstanceExecutor executor, Constraint constraint) {
		super(executor, constraint);
		specification = (OpaqueExpression) constraint.getSpecification();
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
		EvaluationOptions.setOption(myOCL.getEvaluationEnvironment(), UMLEvaluationOptions.EVALUATION_MODE, EvaluationMode.RUNTIME_OBJECTS);
		org.eclipse.ocl.expressions.OCLExpression<?> oclConstraint = null;
		myOCL.setEvaluationTracingEnabled(true);
		myOCL.setParseTracingEnabled(true);
		OCLHelper<?, ?, ?, ?> helper = myOCL.createOCLHelper();
		helper.setInstanceContext(executor.getInstanceSpecification());
		try {
			oclConstraint = helper.createQuery("false"); //specification.getBodies().get(0)
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//boolean check = myOCL.check(executor.getInstanceSpecification(), oclConstraint);
		return false;
	}

}
