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
package dk.dtu.imm.esculapauml.core.ocl;

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
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.InstanceSpecification;

import dk.dtu.imm.esculapauml.core.checkers.Checker;

/**
 * Evaluator for OCL expressions.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class OCLEvaluator {
	private EvaluationMode mode = EvaluationMode.INSTANCE_MODEL;
	private InstanceSpecification context;
	private Checker checker;
	private boolean debug = false;
	private boolean hasErrors = false;
	private Element errorContext;

	/**
	 * 
	 */
	public OCLEvaluator(Checker checker, InstanceSpecification context, Element errorContext) {
		this.context = context;
		this.checker = checker;
		setErrorContext(errorContext);
	}

	public Object evaluate(String expression) {
		hasErrors = false;
		UMLEnvironmentFactory envFactory = new UMLEnvironmentFactory(context.getModel().eResource().getResourceSet());
		UMLEnvironment env = envFactory.createEnvironment();
		org.eclipse.ocl.uml.OCL myOCL = org.eclipse.ocl.uml.OCL.newInstance(env);
		EvaluationEnvironment<?, ?, ?, ?, ?> evalEnv = myOCL.getEvaluationEnvironment();
		EvaluationOptions.setOption(evalEnv, UMLEvaluationOptions.EVALUATION_MODE, mode);
		OCLExpression<?> oclConstraint = null;
		if (debug) {
			myOCL.setEvaluationTracingEnabled(true);
			myOCL.setParseTracingEnabled(true);
		}

		OCLHelper<?, ?, ?, ?> helper = myOCL.createOCLHelper();
		helper.setInstanceContext(context);
		try {
			oclConstraint = helper.createQuery(expression);
		} catch (ParserException e) {
			checker.addOtherProblem(Diagnostic.ERROR, "OCL parsing exception of '" + expression + "': " + e.getMessage(), errorContext);
			hasErrors = true;
			return null;
		}
		@SuppressWarnings("unchecked")
		Object result = myOCL.evaluate(context, (OCLExpression<Classifier>) oclConstraint);
		if (null == result || myOCL.isInvalid(result)) {
			checker.addOtherProblem(Diagnostic.ERROR, "Evaluation of '" + expression + "' in context of '" + context.getLabel() + "' returned invalid value.",
					errorContext);
			hasErrors = true;
			return null;
		}
		return result;
	}

	/**
	 * @return the mode
	 */
	public EvaluationMode getMode() {
		return mode;
	}

	/**
	 * @param mode
	 *            the mode to set
	 */
	public void setMode(EvaluationMode mode) {
		this.mode = mode;
	}

	/**
	 * @param errorContext
	 *            the errorContext to set
	 */
	public void setErrorContext(Element errorContext) {
		this.errorContext = errorContext;
	}

	/**
	 * @param debug
	 *            the debug to set
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	/**
	 * @return the hasErrors
	 */
	public boolean hasErrors() {
		return hasErrors;
	}
}
