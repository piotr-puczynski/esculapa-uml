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

import static ch.lambdaj.Lambda.join;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.ocl.EvaluationEnvironment;
import org.eclipse.ocl.ParserException;
import org.eclipse.ocl.expressions.OCLExpression;
import org.eclipse.ocl.helper.OCLHelper;
import org.eclipse.ocl.options.EvaluationOptions;
import org.eclipse.ocl.uml.UMLEnvironment;
import org.eclipse.ocl.uml.UMLEnvironmentFactory;
import org.eclipse.ocl.uml.options.EvaluationMode;
import org.eclipse.ocl.uml.options.UMLEvaluationOptions;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.OpaqueExpression;

import dk.dtu.imm.esculapauml.core.executors.InstanceExecutor;

/**
 * Guard validator is used to evaluate OCL guards constraints.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class OCLValidator extends AbstractValidator implements Validator {

	protected OpaqueExpression specification;
	protected Logger logger = Logger.getLogger(OCLValidator.class);

	/**
	 * @param executor
	 * @param constraint
	 */
	public OCLValidator(InstanceExecutor executor, Constraint constraint) {
		super(executor, constraint);
		specification = (OpaqueExpression) constraint.getSpecification();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.validators.Validator#validateConstraint()
	 */
	@Override
	public boolean validateConstraint() {
		UMLEnvironmentFactory envFactory = new UMLEnvironmentFactory(constraint.getModel().eResource().getResourceSet());
		UMLEnvironment env = envFactory.createEnvironment();
		org.eclipse.ocl.uml.OCL myOCL = org.eclipse.ocl.uml.OCL.newInstance(env);
		EvaluationEnvironment<?, ?, ?, ?, ?> evalEnv = myOCL.getEvaluationEnvironment();
		EvaluationOptions.setOption(evalEnv, UMLEvaluationOptions.EVALUATION_MODE, EvaluationMode.RUNTIME_OBJECTS);
		// TODO: add custom variables with evalEnv.add if needed
		OCLExpression<?> oclConstraint = null;
		if (logger.getEffectiveLevel() == Level.DEBUG) {
			myOCL.setEvaluationTracingEnabled(true);
			myOCL.setParseTracingEnabled(true);
		}

		OCLHelper<?, ?, ?, ?> helper = myOCL.createOCLHelper();
		helper.setInstanceContext(executor.getInstanceSpecification());
		String body = calculateBody();
		try {
			oclConstraint = helper.createQuery(body);
		} catch (ParserException e) {
			executor.getChecker().addOtherProblem(Diagnostic.ERROR, "OCL parsing exception of '" + body + "': " + e.getMessage(), constraint.getOwner());
			return false;
		}
		@SuppressWarnings("unchecked")
		Object result = myOCL.evaluate(executor.getInstanceSpecification(), (OCLExpression<Classifier>) oclConstraint);

		if (result instanceof Boolean) {
			return (Boolean) result;
		} else {
			executor.getChecker().addOtherProblem(
					Diagnostic.ERROR,
					"OCL expression in guard must return Boolean value (current value is of type '" + ((null == result) ? "null" : result.getClass().getName())
							+ "'", constraint.getOwner());
			return false;
		}
	}

	/**
	 * Calculates an ocl body string.
	 * 
	 * @return
	 */
	protected String calculateBody() {
		int i = 0;
		List<String> bodies = new ArrayList<String>();
		for (String lang : specification.getLanguages()) {
			if (lang.equalsIgnoreCase("ocl")) {
				if (specification.getBodies().size() > i) {
					bodies.add(specification.getBodies().get(i));
				}
			}
			++i;
		}
		return join(bodies, " and ");
	}

}
