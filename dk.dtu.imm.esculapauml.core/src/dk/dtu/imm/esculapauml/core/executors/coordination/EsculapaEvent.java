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

import org.eclipse.uml2.uml.Element;

/**
 * Base class for all sequenced events.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public abstract class EsculapaEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long sequenceId;
	private Element errorContext;

	/**
	 * @param source
	 */
	public EsculapaEvent(Object source, Element errorContext) {
		super(source);
		this.errorContext = errorContext;
	}

	/**
	 * @return the sequenceId
	 */
	public long getSequenceId() {
		return sequenceId;
	}

	/**
	 * @param sequenceId
	 *            the sequenceId to set
	 */
	public void setSequenceId(long sequenceId) {
		this.sequenceId = sequenceId;
	}

	/**
	 * @return the errorContext
	 */
	public Element getErrorContext() {
		return errorContext;
	}

}
