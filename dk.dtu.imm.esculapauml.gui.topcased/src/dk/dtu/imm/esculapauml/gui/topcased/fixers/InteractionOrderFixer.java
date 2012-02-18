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
package dk.dtu.imm.esculapauml.gui.topcased.fixers;

import org.eclipse.uml2.uml.Interaction;
import org.topcased.modeler.editor.Modeler;

/**
 * Fixer is an object to fix order of messages occurrences on the lifeline in
 * the model based on graphical representation. It is a hack to avoid bug item
 * #4014:
 * http://gforge.enseeiht.fr/tracker/?func=detail&atid=109&aid=4014&group_id=52
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class InteractionOrderFixer {

	private Modeler modeler;
	private Interaction interaction;

	public InteractionOrderFixer(Modeler modeler, Interaction interaction) {
		super();
		this.modeler = modeler;
		this.interaction = interaction;
	}

	/**
	 * Fixes the model.
	 */
	public void fix() {

	}

}
