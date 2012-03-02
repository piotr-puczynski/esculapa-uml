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
package dk.dtu.imm.esculapauml.core.executors.behaviors;

import static ch.lambdaj.Lambda.joinFrom;

import java.util.HashSet;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Transition;

import dk.dtu.imm.esculapauml.core.executors.InstanceExecutor;

/**
 * The class used for deciding what transition to take also in case of
 * conflicting transitions.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class TransitionChooser {
	public static Transition choose(InstanceExecutor executor, EList<Transition> transitions) {
		// make sure we have unique transitions
		if (new HashSet<Transition>(transitions).size() > 1) {
			// set a warning on conflicting transitions
			String names = joinFrom(transitions, Transition.class).getName();
			executor.getChecker().addOtherProblem(Diagnostic.WARNING, "Conflicting transitions: [" + names + "]", transitions.toArray());
		}
		// make a choice of first transition if possible
		if (transitions.isEmpty()) {
			return null;
		} else {
			return transitions.get(0);
		}
	}
}
