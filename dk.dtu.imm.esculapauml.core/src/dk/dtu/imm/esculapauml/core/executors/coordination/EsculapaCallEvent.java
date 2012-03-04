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

import java.util.EventObject;

import org.eclipse.uml2.uml.Operation;

import dk.dtu.imm.esculapauml.core.executors.InstanceExecutor;

/**
 * Object of call event during execution.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class EsculapaCallEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Operation operation;
	private InstanceExecutor target;
	private boolean isSynchronousCall;

	/**
	 * @param source
	 */
	public EsculapaCallEvent(Object source, InstanceExecutor target, Operation operation, boolean isSynchronousCall) {
		super(source);
		this.operation = operation;
		this.target = target;
		setSynchronousCall(isSynchronousCall);
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

}
