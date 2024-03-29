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
package dk.dtu.imm.esculapauml.core.sal;

/**
 * Class utilized by visitors to pass extra information to nested SAL nodes.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class SALEvaluationHelper {

	private Object functionEvaluationContext;

	/**
	 * @param functionEvaluationContext
	 */
	public SALEvaluationHelper(Object functionEvaluationContext) {
		super();
		this.setFunctionEvaluationContext(functionEvaluationContext);
	}

	/**
	 * @return the functionEvaluationContext
	 */
	public Object getFunctionEvaluationContext() {
		return functionEvaluationContext;
	}

	/**
	 * @param functionEvaluationContext
	 * @return old function evaluation context
	 */
	public Object setFunctionEvaluationContext(Object functionEvaluationContext) {
		Object result = this.functionEvaluationContext;
		this.functionEvaluationContext = functionEvaluationContext;
		return result;
	}

}
