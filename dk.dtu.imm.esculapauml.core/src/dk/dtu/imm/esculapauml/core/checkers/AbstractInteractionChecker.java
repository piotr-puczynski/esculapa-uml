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

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.UMLPackage.Literals;

/**
 * Interaction diagnostic check of common features for all interactions
 * 
 * @author Piotr. J. Puczynski (piotr.puczynski)
 * 
 */
public abstract class AbstractInteractionChecker extends AbstractChecker<Interaction> {
	
	AbstractInteractionChecker(Interaction interaction) {
		super(interaction);
	}

	/**
	 * Check all lifelines
	 */
	protected void checkLifelines() {
		CollectionChecker<?> cc = new CollectionChecker<Lifeline>(diagnostics, checkee.getLifelines());
		cc.check();
	}

	/**
	 * Check all messages in an interaction
	 * 
	 * @param message
	 */
	protected void checkMessages() {
		CollectionChecker<?> cc = new CollectionChecker<Message>(diagnostics, checkee.getMessages());
		cc.check();
	}

	/**
	 * Gets the first message to send in interaction
	 * 
	 * @return the first message to send or null if interaction has no messages
	 */
	protected Message getFirstMessage() {
		// get all possible messages
		EList<Message> messages = checkee.getMessages();
		// now check which message is sent first on lifelines
		EList<Lifeline> lifelines = checkee.getLifelines();
		for (Lifeline l : lifelines) {
			// we are only interested in the first fragment of message
			// occurrence specification
			MessageOccurrenceSpecification spec = (MessageOccurrenceSpecification) EcoreUtil.getObjectByType(l.getCoveredBys(),
					Literals.MESSAGE_OCCURRENCE_SPECIFICATION);
			if (null != spec) {
				for (Message m : messages) {
					if (spec == m.getReceiveEvent()) {
						// m is the first message to consider
						return m;
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Debug method used to print out contents of interaction
	 */
	public void printOutInteraction() {
		TreeIterator<EObject> contents = ((EObject)checkee).eAllContents();
		while (contents.hasNext()) {
			EObject o = contents.next();
			System.out.println(o.toString());
		}
	}
}
