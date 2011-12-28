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

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.ConnectableElement;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Type;

/**
 * Interaction diagnostic check of common features for all interactions
 * 
 * @author Piotr. J. Puczynski (piotr.puczynski)
 * 
 */
public abstract class AbstractInteractionChecker extends AbstractChecker {
	protected Interaction interaction;

	AbstractInteractionChecker(Interaction interaction) {
		this.interaction = interaction;
	}

	/**
	 * Start checking operaration
	 * 
	 * @return the interaction that is a result of checking
	 */
	public Interaction getInteraction() {
		return interaction;
	}

	/**
	 * Check if each lifeline corresponds to some class in the model
	 */
	protected void structuralLifelinesExistanceCheck() {
		EList<Lifeline> lifeLines = interaction.getLifelines();
		for (Lifeline l : lifeLines) {
			ConnectableElement connection = l.getRepresents();
			// representant is not set at all
			if (null == connection) {
				diagnostics.add(new BasicDiagnostic(Diagnostic.ERROR, "dk.dtu.imm.esculapauml", 0,
						"The Lifeline " + l.getLabel() + " has no representant.", new Object[] { l }));
			} else {
				Type type = connection.getType();
				if (null == type) {
					diagnostics.add(new BasicDiagnostic(Diagnostic.ERROR, "dk.dtu.imm.esculapauml", 0,
							"The Lifeline " + l.getLabel() + " has no representant set to any type.", new Object[] { l }));
				}
			}
		}
	}
}
