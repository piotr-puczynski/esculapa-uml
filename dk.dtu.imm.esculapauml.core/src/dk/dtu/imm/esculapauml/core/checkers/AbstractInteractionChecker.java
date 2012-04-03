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

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.uml2.uml.Component;
import org.eclipse.uml2.uml.InstanceSpecification;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;

import dk.dtu.imm.esculapauml.core.states.SystemState;

/**
 * Interaction diagnostic check of common features for all interactions
 * 
 * @author Piotr. J. Puczynski (piotr.puczynski)
 * 
 */
public abstract class AbstractInteractionChecker extends AbstractChecker<Interaction> {

	AbstractInteractionChecker(SystemState systemState, Interaction interaction) {
		super(systemState, interaction);
	}

	/**
	 * Check all lifelines
	 */
	protected void checkLifelines() {
		CollectionChecker<?> cc = new CollectionChecker<Lifeline>(this, checkee.getLifelines());
		cc.check();
	}

	/**
	 * Check all messages in an interaction
	 * 
	 * @param message
	 */
	protected void checkMessages() {
		CollectionChecker<?> cc = new CollectionChecker<Message>(this, checkee.getMessages());
		cc.check();
	}

	/**
	 * Checks all existing instance specifications.
	 * 
	 */
	protected void checkExistingInstances() {
		CollectionChecker<?> cc = new CollectionChecker<InstanceSpecification>(this, systemState.getExistingInstances());
		cc.check();
	}

	/**
	 * Checks all components in the model.
	 */
	protected void checkComponents() {
		CollectionChecker<?> cc = new CollectionChecker<Component>(this, systemState.getExistingComponents());
		cc.check();
	}

	/**
	 * Debug method used to print out contents of interaction
	 */
	public void printOutInteraction() {
		TreeIterator<EObject> contents = ((EObject) checkee).eAllContents();
		logger.debug("Interaction contents:");
		while (contents.hasNext()) {
			EObject o = contents.next();
			logger.debug(o.toString());
		}
	}
}
