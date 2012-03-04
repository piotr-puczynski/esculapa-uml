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
package dk.dtu.imm.esculapauml.core.checkers;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.uml2.uml.Region;
import org.eclipse.uml2.uml.StateMachine;

/**
 * Base class for StateMachine checkers
 * 
 * @author Piotr J. Puczynski
 * 
 */
public abstract class AbstractStateMachineChecker extends AbstractChecker<StateMachine> {

	/**
	 * @param existingDiagnostics
	 * @param objectToCheck
	 */
	AbstractStateMachineChecker(Checker checker, StateMachine objectToCheck) {
		super(checker, objectToCheck);
	}

	/**
	 * Checks whatever any region element exists and then checks regions
	 * 
	 */
	protected void checkRegions() {
		if (checkee.getRegions().size() == 0) {
			addProblem(Diagnostic.ERROR, "The StateMachine \"" + checkee.getLabel() + "\" has no regions.");
		}
		CollectionChecker<?> cc = new CollectionChecker<Region>(systemState, diagnostics, checkee.getRegions());
		cc.check();
	}

}
