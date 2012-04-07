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

import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.InstanceSpecification;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.ValueSpecification;

import dk.dtu.imm.esculapauml.core.collections.CallArguments;
import dk.dtu.imm.esculapauml.core.collections.ValuesCollection;

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
	boolean setVariable(String name, ValuesCollection value, Element errorContext);

	/**
	 * Sets local variable (for arrays).
	 * 
	 * @param name
	 * @param value
	 * @param errorContext
	 *            context of the possible type error to display.
	 * 
	 * @return true if set successfully or false if type check failed and
	 *         variable was not set.
	 */
	boolean setVariable(String name, int index, ValueSpecification value, Element errorContext);

	/**
	 * Gets local variable.
	 * 
	 * @param name
	 * @return value if variable was found or null if it was not.
	 */
	ValuesCollection getVariable(String name);

	/**
	 * Gets local variable with index (for arrays).
	 * 
	 * @param name
	 * @param index
	 * @return
	 */
	ValueSpecification getVariable(String name, int index);

	/**
	 * Removes local variable.
	 * 
	 * @param name
	 * @param removeDeclaringModelElement
	 *            if true will also remove declaration of corresponding property
	 *            from the class
	 * @return true if the variable was removed successfully
	 */
	boolean removeVariable(String name, boolean removeDeclaringModelElement);

	/**
	 * Returns the operation based on name or null if operation is not declared.
	 * 
	 * @param name
	 *            of an operation
	 * @return UML operation or null
	 */
	Operation getOperationByName(String name);

	/**
	 * Operation used to call operation on an instance.
	 * 
	 * @param source
	 *            any source object
	 * @param operation
	 *            object
	 * @param arguments
	 *            as list of ValueSpecifications
	 * @param isSynchronous
	 * @param errorContext
	 * @return
	 */
	ValuesCollection callOperation(Object source, InstanceSpecification caller, Operation operation, CallArguments arguments, boolean isSynchronous,
			Element errorContext);

	/**
	 * Operation used to call operation on an instance. Uses operation name to
	 * identify operation.
	 * 
	 * @param source
	 * @param operationName
	 * @param arguments
	 * @param isSynchronous
	 * @param errorContext
	 * @return
	 */
	ValuesCollection callOperation(Object source, InstanceSpecification caller, String operationName, CallArguments arguments, boolean isSynchronous,
			Element errorContext);

	/**
	 * Gets the original class of the instance type.
	 * 
	 * @return
	 */
	Class getOriginalClass();

}
