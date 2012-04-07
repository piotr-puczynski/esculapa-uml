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
package dk.dtu.imm.esculapauml.core.collections;

/**
 * Base class for exception of OCL 2 UML converter.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class OCLConversionException extends Exception {
	private Object oclValue;

	/**
	 * @param oclValue
	 */
	public OCLConversionException(Object oclValue) {
		this.oclValue = oclValue;
	}

	/**
	 * @return the oclValue
	 */
	public Object getOclValue() {
		return oclValue;
	}


	/**
	 * 
	 */
	private static final long serialVersionUID = 8149004296676438841L;

}
