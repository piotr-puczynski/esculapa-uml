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
package dk.dtu.imm.esculapauml.core.executors.guards;

import org.eclipse.uml2.uml.Pseudostate;
import org.eclipse.uml2.uml.PseudostateKind;
import org.eclipse.uml2.uml.Vertex;

import dk.dtu.imm.esculapauml.core.executors.InstanceExecutor;

/**
 * @author Piotr J. Puczynski
 * 
 */
public class GuardEvaluatorsFactory {
	private static GuardEvaluatorsFactory instance = null;

	protected GuardEvaluatorsFactory() {
		super();
	}

	public static GuardEvaluatorsFactory getInstance() {
		if (instance == null) {
			instance = new GuardEvaluatorsFactory();
		}
		return instance;
	}

	/**
	 * Creates guard evaluator for given type of vertex.
	 * 
	 * @param executor
	 * @param vertex
	 * @return
	 */
	public GuardEvaluator getGuardEvaluator(InstanceExecutor executor, Vertex vertex) {
		if (vertex instanceof Pseudostate) {
			Pseudostate pseudoVertex = (Pseudostate) vertex;
			// if else is allowed
			if (pseudoVertex.getKind() == PseudostateKind.CHOICE_LITERAL || pseudoVertex.getKind() == PseudostateKind.JUNCTION_LITERAL) {
				return new GuardWithElseEvaluator(executor, vertex);
			}
		}
		// otherwise use general evaluator
		return new SimpleGuardEvaluator(executor, vertex);
	}
}
