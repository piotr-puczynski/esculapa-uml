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

import org.eclipse.uml2.uml.ValueSpecification;

import dk.dtu.imm.esculapauml.core.executors.BehaviorExecutor;

/**
 * @author Piotr J. Puczynski
 *
 */
public class EsculapaReplyEvent extends EventObject {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private EsculapaCallEvent call;
	private ValueSpecification result;

	/**
	 * @param source
	 */
	public EsculapaReplyEvent(BehaviorExecutor source, EsculapaCallEvent call, ValueSpecification result) {
		super(source);
		this.call = call;
		this.result = result;
	}
	
	public BehaviorExecutor getSource() {
		return (BehaviorExecutor) source;
	}

	/**
	 * @return the call
	 */
	public EsculapaCallEvent getCall() {
		return call;
	}

	/**
	 * @return the result
	 */
	public ValueSpecification getResult() {
		return result;
	}
	
}
