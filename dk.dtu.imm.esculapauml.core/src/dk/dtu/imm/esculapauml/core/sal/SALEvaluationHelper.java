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

import static ch.lambdaj.Lambda.join;

import java.util.Stack;

/**
 * Class utilized by visitors to pass extra information to nested SAL nodes.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class SALEvaluationHelper {

	private Stack<String> evaluationContext = new Stack<String>();

	/**
	 * Adds new evaluation context entry to existing context.
	 * 
	 * @param id
	 */
	public void pushEvaluationContext(String id) {
		evaluationContext.push(id);
	}

	/**
	 * Removes last entry in evaluation context.
	 * 
	 * @param id
	 */
	public String popEvaluationContext() {
		if (hasEvaluationContext()) {
			return evaluationContext.pop();
		}
		return null;
	}

	/**
	 * Checks if any evaluation context entry was set.
	 * 
	 * @return
	 */
	public boolean hasEvaluationContext() {
		return !evaluationContext.isEmpty();
	}

	/**
	 * Calculates OCL expression of evaluation context.
	 * 
	 * @return the evaluationContext
	 */
	public String getEvaluationContextExpression() {
		return join(evaluationContext, ".");
	}
	
	/**
	 * Swaps the evaluation context with the new one, returns the old one.
	 * 
	 * @param newContext
	 * @return
	 */
	public Stack<String> swapEvaluationContext(Stack<String> newContext) {
		Stack<String> result = evaluationContext;
		evaluationContext = newContext;
		return result;
	}

}
