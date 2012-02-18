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
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.Region;

import dk.dtu.imm.esculapauml.core.states.SystemState;

/**
 * Checker for collections of objects
 * @author Piotr J. Puczynski
 *
 */
public class CollectionChecker<T> extends AbstractChecker<Collection<T>> {

	/**
	 * @param objectToCheck
	 */
	public CollectionChecker(SystemState systemState, Collection<T> objectToCheck) {
		super(systemState, objectToCheck);
	}
	
	/**
	 * @param checker
	 * @param objectToCheck
	 */
	public CollectionChecker(Checker checker, Collection<T> objectToCheck) {
		super(checker, objectToCheck);
	}

	/**
	 * @param existingDiagnostics
	 * @param objectToCheck
	 */
	CollectionChecker(SystemState systemState, BasicDiagnostic existingDiagnostics, Collection<T> objectToCheck) {
		super(systemState, existingDiagnostics, objectToCheck);
	}

	/* (non-Javadoc)
	 * @see dk.dtu.imm.esculapauml.core.checkers.CheckerInterface#check()
	 */
	@Override
	public void check() {
		for(T elem : checkee) {
			Checker checker = null;
			if(elem instanceof Lifeline) {
				checker = new LifelineChecker(this, (Lifeline) elem);
			} else if(elem instanceof Message) {
				checker = new MessageChecker(this, (Message) elem);
			} else if(elem instanceof Region) {
				checker = new RegionChecker(this, (Region) elem);
			} else if(elem instanceof InteractionFragment) {
				checker = new InteractionFragmentChecker(this, (InteractionFragment) elem);
			}
			
			// add more classes here if needed
			assert checker != null;
			checker.check();
		}
		
	}

}
