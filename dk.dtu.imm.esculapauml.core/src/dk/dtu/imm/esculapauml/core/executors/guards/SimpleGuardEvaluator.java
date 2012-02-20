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

import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.Vertex;

import dk.dtu.imm.esculapauml.core.executors.InstanceExecutor;

/**
 * Guard evaluator for vertices based on simple evaluation.
 * @author Piotr J. Puczynski
 *
 */
public class SimpleGuardEvaluator implements GuardEvaluator {

	/**
	 * @param executor
	 * @param vertex
	 */
	public SimpleGuardEvaluator(InstanceExecutor executor, Vertex vertex) {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see dk.dtu.imm.esculapauml.core.executors.guards.GuardEvaluator#getTransitionsWithEnabledGuards()
	 */
	@Override
	public EList<Transition> getTransitionsWithEnabledGuards() {
		// TODO Auto-generated method stub
		return null;
	}

}
