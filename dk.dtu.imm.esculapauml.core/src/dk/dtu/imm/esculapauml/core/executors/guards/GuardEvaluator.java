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

import ch.lambdaj.function.matcher.Predicate;

/**
 * Interface for evaluating guards on any type of vertices.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public interface GuardEvaluator {
	/**
	 * Operation returns only the outgoing transitions from vertex that are
	 * enabled at the moment of evaluation.
	 * 
	 * @return
	 */
	EList<Transition> getTransitionsWithEnabledGuards();

	/**
	 * The function returns extra preconditions on which selection of
	 * transitions to evaluate is conducted. Null if no extra conditions exist.
	 * 
	 * @return the extraConditions
	 */
	Predicate<Transition> getPreconditions();

	/**
	 * Allows to set extra preconditions on which selection of transitions to
	 * evaluate is conducted. Only transitions that confirm to the condition are
	 * evaluated.
	 * 
	 * @param extraConditions
	 *            the extraConditions to set
	 */
	void setPreconditions(Predicate<Transition> preonditions);
}
