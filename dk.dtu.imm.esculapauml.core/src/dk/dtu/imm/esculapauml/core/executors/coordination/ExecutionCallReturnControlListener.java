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

import java.util.EventListener;

/**
 * Interface for listeners of control execution events.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public interface ExecutionCallReturnControlListener extends EventListener {
	/**
	 * Occurs when control flow returns.
	 * 
	 * @param event
	 */
	void callReturnControlEventOccurred(EsculapaCallReturnControlEvent event);
}
