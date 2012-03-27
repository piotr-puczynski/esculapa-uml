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
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.OpaqueExpression;
import dk.dtu.imm.esculapauml.core.executors.InstanceExecutor;
import dk.dtu.imm.esculapauml.core.ocl.OCLEvaluator;

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
		OCLEvaluator eval = new OCLEvaluator(executor.getChecker(), executor.getInstanceSpecification(), errorContext);
		eval.setDebug(logger.getEffectiveLevel() == Level.DEBUG);
		Object result = eval.evaluate(calculateBody());
		if (eval.hasErrors()) {
			return false;
		} else {
			if (result instanceof Boolean) {
				return (Boolean) result;
			} else {
				executor.getChecker().addOtherProblem(
						Diagnostic.ERROR,
						"OCL expression in guard must return 'Boolean' value (current value is of type '"
								+ ((null == result) ? "null" : result.getClass().getSimpleName()) + "')", errorContext);
				return false;
			}
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
