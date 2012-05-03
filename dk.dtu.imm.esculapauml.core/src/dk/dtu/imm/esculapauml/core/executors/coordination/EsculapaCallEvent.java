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
package dk.dtu.imm.esculapauml.core.executors.coordination;

import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.InstanceSpecification;
import org.eclipse.uml2.uml.Operation;

import dk.dtu.imm.esculapauml.core.collections.CallArguments;
import dk.dtu.imm.esculapauml.core.executors.InstanceExecutor;

/**
 * Object of call event during execution.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class EsculapaCallEvent extends EsculapaEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Operation operation;
	private InstanceExecutor target;
	private boolean isSynchronousCall;
	private CallArguments arguments;
	private InstanceSpecification caller;

	/**
	 * @param source
	 * @param arguments
	 */
	public EsculapaCallEvent(Object source, Element errorContext, InstanceSpecification caller, InstanceExecutor target, Operation operation,
			CallArguments arguments, boolean isSynchronousCall) {
		super(source, errorContext);
		this.operation = operation;
		this.target = target;
		this.arguments = arguments;
		this.caller = caller;
		setSynchronousCall(isSynchronousCall);
	}

	/**
	 * @return the caller
	 */
	public InstanceSpecification getCaller() {
		return caller;
	}

	/**
	 * Returns operation called.
	 * 
	 * @return the operation
	 */
	public Operation getOperation() {
		return operation;
	}

	/**
	 * @return the isSynchronousCall
	 */
	public boolean isSynchronousCall() {
		return isSynchronousCall;
	}

	/**
	 * @param isSynchronousCall
	 *            the isSynchronousCall to set
	 */
	public void setSynchronousCall(boolean isSynchronousCall) {
		this.isSynchronousCall = isSynchronousCall;
	}

	/**
	 * @return the target
	 */
	public InstanceExecutor getTarget() {
		return target;
	}

	/**
	 * @return the arguments
	 */
	public CallArguments getArguments() {
		return arguments;
	}

}
