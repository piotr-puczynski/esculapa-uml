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
import org.eclipse.emf.common.util.TreeIterator;

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
		System.out.println(interaction.eClass().getName());
		// check for right EObject type
		if (!interaction.eClass().getName().equals("Interaction")) {
			boolean found = false;
			// if collaboration or use case, be flexible
			if (interaction.eClass().getName().equals("Collaboration") || interaction.eClass().getName().equals("UseCase")) {
				TreeIterator<EObject> contents = interaction.eAllContents();
				while (contents.hasNext()) {
					EObject o = contents.next();
					if (o.eClass().getName().equals("Interaction")) {
						interaction = o;
						found = true;
						break;
					}
				}
			}
			if (!found) {
				throw new IllegalArgumentException("Passed argument is not an interaction, use case or collaboration");
			}
		}
	}
}
