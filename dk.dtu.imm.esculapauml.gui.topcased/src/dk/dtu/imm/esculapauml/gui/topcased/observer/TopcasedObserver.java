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
package dk.dtu.imm.esculapauml.gui.topcased.observer;

import java.util.Observable;
import java.util.Observer;
import dk.dtu.imm.esculapauml.core.ConsistencyCheckingService;

/**
 * @author Piotr. J. Puczynski (piotr.puczynski)
 * 
 */
public class TopcasedObserver implements Observer {

	private static TopcasedObserver instance = null;

	protected TopcasedObserver() {
		super();
	}

	public static TopcasedObserver getInstance() {
		if (instance == null) {
			instance = new TopcasedObserver();
		}
		return instance;
	}
	
	public void register() {
		ConsistencyCheckingService.getInstance().addObserver(this);
	}
	
	public void unRegister() {
		ConsistencyCheckingService.getInstance().deleteObserver(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable arg0, Object arg1) {

	}

}
