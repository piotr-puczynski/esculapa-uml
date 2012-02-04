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
package dk.dtu.imm.esculapauml.gui.topcased.extenders;

/**
 * Extender extends some diagram adding to it new elements that has been generated in the model.
 * @author Piotr J. Puczynski
 *
 */
public interface ExtenderInterface {

	/**
	 * Extends the diagram for new generated elements.
	 */
	void extend();
	
}
