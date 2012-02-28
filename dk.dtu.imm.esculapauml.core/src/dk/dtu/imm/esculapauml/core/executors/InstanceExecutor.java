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
import org.eclipse.uml2.uml.ValueSpecification;

/**
 * @author Piotr J. Puczynski
 * 
 */
public interface InstanceExecutor extends Executor {
	/**
	 * Returns the executed instance.
	 * 
	 * @return
	 */
	InstanceSpecification getInstanceSpecification();

	/**
	 * Returns the name of the executed instance.
	 * 
	 * @return
	 */
	String getInstanceName();

	/**
	 * Sets local variable.
	 * 
	 * @param name
	 * @param value
	 * @return
	 */
	void setVariable(String name, ValueSpecification value);

	/**
	 * Gets local variable.
	 * 
	 * @param name
	 * @return
	 */
	ValueSpecification getVariable(String name);
}
