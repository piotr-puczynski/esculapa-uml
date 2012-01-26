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

import dk.dtu.imm.esculapauml.core.checkers.AbstractChecker;

/**
 * @author Piotr J. Puczynski
 *
 */
public abstract class AbstractExecutor<T> implements ExecutorInterface<T> {
	private T checker;
	public AbstractExecutor(T checker) {
		this.checker = checker;
	}
	
	/* (non-Javadoc)
	 * @see dk.dtu.imm.esculapauml.core.executors.ExecutorInterface#getChecker()
	 */
	@Override
	public T getChecker() {
		return checker;
	}
	
}
