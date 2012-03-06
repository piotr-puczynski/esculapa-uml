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

import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.InstanceSpecification;
import org.eclipse.uml2.uml.Operation;
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
	 * @param errorContext
	 *            context of the possible type error to display.
	 * 
	 * @return true if set successfully or false if type check failed and
	 *         variable was not set.
	 */
	boolean setVariable(String name, ValueSpecification value, Element errorContext);

	/**
	 * Gets local variable.
	 * 
	 * @param name
	 * @return value if variable was found or null if it was not.
	 */
	ValueSpecification getVariable(String name);

	/**
	 * Operation used to call operation on instance.
	 * 
	 * @param source
	 * @param operation
	 * @param arguments
	 * @param isSynchronous
	 * @param errorContext
	 * @return
	 */
	ValueSpecification callOperation(Object source, Operation operation, EList<ValueSpecification> arguments, boolean isSynchronous, Element errorContext);

	/**
	 * Gets local variable. ValueName is null for single multiplicity variables.
	 * 
	 * @param name
	 * @param valueName
	 * @return
	 */
	ValueSpecification getVariable(String name, String valueName);

	/**
	 * Gets the original class of the instance type.
	 * 
	 * @return
	 */
	Class getOriginalClass();

	/**
	 * Gets the local variables class that inherits from original class.
	 * 
	 * @return
	 */
	Class getLocalClass();
	
}
