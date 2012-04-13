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

import org.eclipse.uml2.uml.OpaqueExpression;
import org.eclipse.uml2.uml.Operation;

import dk.dtu.imm.esculapauml.core.checkers.Checker;

/**
 * Initializes OCL evaluation for given model. It searches for query operations
 * and defines them so that OCL will be able to see it.
 * 
 * This is the one to avoid https://bugs.eclipse.org/bugs/show_bug.cgi?id=286931
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class OCLInitializator {
	public static void initialize(OCLEvaluator evaluator, Checker checker, Operation operation) {
		OpaqueExpression oe = (OpaqueExpression) operation.getBodyCondition().getSpecification();
		evaluator.defineBody(checker, operation.getClass_(), operation, operation, oe.getBodies().get(0));
	}
}
