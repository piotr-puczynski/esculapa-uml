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

import org.eclipse.uml2.uml.Interaction;

/**
 * Interface for all checkers classes
 * @author Piotr. J. Puczynski (piotr.puczynski)
 *
 */
public abstract class AbstractInteractionChecker implements CheckerInterface {
	protected Interaction interaction;
	/**
	 * Start checking operaration
	 * @return the interaction that is a result of checking
	 */
	public Interaction getInteraction() {
		return interaction;
	}
}
