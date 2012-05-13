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

/**
 * Object of completion event during execution.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class EsculapaCompletionEvent extends EsculapaEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param source
	 * @param arguments
	 */
	public EsculapaCompletionEvent(Object source, Element errorContext) {
		super(source, errorContext);
	}

}
