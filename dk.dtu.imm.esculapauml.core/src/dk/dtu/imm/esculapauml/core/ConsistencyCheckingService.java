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

package dk.dtu.imm.esculapauml.core;

import java.util.Observable;
import org.eclipse.emf.ecore.EObject;

/**
 * 
 * The main interface that should be used by clients (i.e. other plug-ins) to
 * start consistency checking. It also acts as an observable for decision making
 * UIs. If your UI wants to be notified of need of decision it needs to add
 * itself as observer.
 * 
 * @author Piotr. J. Puczynski (piotr.puczynski)
 * 
 */
public class ConsistencyCheckingService extends Observable {
	private static ConsistencyCheckingService instance = null;
	
	protected ConsistencyCheckingService() {
	}
	
	
	public static ConsistencyCheckingService getInstance() {
		if (instance == null) {
			instance = new ConsistencyCheckingService();
		}
		return instance;
	}
	
	public void checkUseCaseInteraction(EObject interaction) {
		System.out.println("fajnie mi haha");
		setChanged();
		notifyObservers();
	}
}
