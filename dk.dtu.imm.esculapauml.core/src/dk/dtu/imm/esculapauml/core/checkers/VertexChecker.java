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

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.uml2.uml.FinalState;
import org.eclipse.uml2.uml.Region;
import org.eclipse.uml2.uml.State;
import org.eclipse.uml2.uml.Vertex;

/**
 * Checks all kinds of sub machine's states.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class VertexChecker extends AbstractChecker<Vertex> {

	/**
	 * @param checker
	 * @param objectToCheck
	 */
	VertexChecker(Checker checker, Vertex objectToCheck) {
		super(checker, objectToCheck);
		logger = Logger.getLogger(VertexChecker.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dk.dtu.imm.esculapauml.core.checkers.Checker#check()
	 */
	@Override
	public void check() {
		logger.info(checkee.getLabel() + ": starting check");
		if (checkee instanceof State) {
			// check regions (if composite)
			if (((State) checkee).getRegions().isEmpty()) {
				// miracle state test
				if (!checkee.getOutgoings().isEmpty() && checkee.getIncomings().isEmpty()) {
					addProblem(Diagnostic.ERROR, "Miracle state.");
				}
			} else {
				CollectionChecker<?> cc = new CollectionChecker<Region>(this, ((State) checkee).getRegions());
				cc.check();
			}

		} else if (checkee instanceof FinalState) {
			if (!checkee.getOutgoings().isEmpty()) {
				addProblem(Diagnostic.ERROR, "Final state must not have outgoing transitions.");
			}
		}

	}

}
