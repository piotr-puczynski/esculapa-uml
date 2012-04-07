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

import org.eclipse.uml2.uml.Type;

/**
 * Class of incompatible type exception.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class TypesIncompatibleException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2693962535838861835L;

	private Type incompatibleType;

	/**
	 * @return the incompatibleType
	 */
	public Type getIncompatibleType() {
		return incompatibleType;
	}

	/**
	 * @param incompatibleType
	 */
	public TypesIncompatibleException(Type incompatibleType) {
		super();
		this.incompatibleType = incompatibleType;
	}

}
