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

import org.eclipse.uml2.uml.Operation;

import dk.dtu.imm.esculapauml.core.executors.InstanceExecutor;

/**
 * Class models return of a flow from any call.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class EsculapaCallReturnControlEvent extends EsculapaEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Operation operation;
	private long initiatingCallSequenceNumber;


	/**
	 * @param behaviorExecutor
	 * @param event
	 */
	public EsculapaCallReturnControlEvent(InstanceExecutor source, EsculapaCallEvent event) {
		super(source, event.getErrorContext());
		this.operation = event.getOperation();
		this.initiatingCallSequenceNumber = event.getSequenceId();
	}

	public InstanceExecutor getSource() {
		return (InstanceExecutor) source;
	}

	/**
	 * @return the operation
	 */
	public Operation getOperation() {
		return operation;
	}

	/**
	 * @return the initiatingCallSequenceNumber
	 */
	public long getInitiatingCallSequenceNumber() {
		return initiatingCallSequenceNumber;
	}
}
