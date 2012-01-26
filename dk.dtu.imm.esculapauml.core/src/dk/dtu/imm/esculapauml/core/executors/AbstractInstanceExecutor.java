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
package dk.dtu.imm.esculapauml.core.executors;

import org.eclipse.uml2.uml.InstanceSpecification;

/**
 * Executes instances.
 * @author Piotr J. Puczynski
 *
 */
public abstract class AbstractInstanceExecutor<T> extends AbstractExecutor<T> {
	
	private InstanceSpecification instance;

	/**
	 * @param checker
	 */
	public AbstractInstanceExecutor(T checker) {
		super(checker);
	}

	/**
	 * @return the instance
	 */
	public InstanceSpecification getInstance() {
		return instance;
	}
	

}
