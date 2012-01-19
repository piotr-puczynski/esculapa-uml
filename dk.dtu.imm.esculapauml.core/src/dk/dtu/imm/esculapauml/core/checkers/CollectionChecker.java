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

import java.util.Collection;

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;

/**
 * Checker for collections of objects
 * @author Piotr J. Puczynski
 *
 */
public class CollectionChecker<T> extends AbstractChecker<Collection<T>> {

	/**
	 * @param objectToCheck
	 */
	public CollectionChecker(Collection<T> objectToCheck) {
		super(objectToCheck);
	}

	/**
	 * @param existingDiagnostics
	 * @param objectToCheck
	 */
	CollectionChecker(BasicDiagnostic existingDiagnostics, Collection<T> objectToCheck) {
		super(existingDiagnostics, objectToCheck);
	}

	/* (non-Javadoc)
	 * @see dk.dtu.imm.esculapauml.core.checkers.CheckerInterface#check()
	 */
	@Override
	public void check() {
		for(T elem : checkee) {
			CheckerInterface checker = null;
			if(elem instanceof Lifeline) {
				checker = new LifelineChecker(diagnostics, (Lifeline) elem);
			} else if(elem instanceof Message) {
				checker = new MessageChecker(diagnostics, (Message) elem);
			}
			// add more classes here if needed
			assert checker != null;
			checker.check();
		}
		
	}

}
