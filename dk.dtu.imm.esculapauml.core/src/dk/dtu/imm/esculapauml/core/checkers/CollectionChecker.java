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

import org.eclipse.uml2.uml.InstanceSpecification;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.Region;
import org.eclipse.uml2.uml.Slot;
import org.eclipse.uml2.uml.Vertex;

/**
 * Checker for collections of objects
 * @author Piotr J. Puczynski
 *
 */
public class CollectionChecker<T> extends AbstractChecker<Collection<T>> {

	
	/**
	 * @param checker
	 * @param objectToCheck
	 */
	public CollectionChecker(Checker checker, Collection<T> objectToCheck) {
		super(checker, objectToCheck);
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
			} else if(elem instanceof InstanceSpecification) {
				checker = new InstanceSpecificationChecker(this, (InstanceSpecification) elem);
			} else if(elem instanceof Vertex) {
				checker = new VertexChecker(this, (Vertex) elem);
			} else if(elem instanceof Slot) {
				checker = new SlotChecker(this, (Slot) elem);
			}
			
			// add more classes here if needed
			assert checker != null;
			checker.check();
		}
		
	}

}
