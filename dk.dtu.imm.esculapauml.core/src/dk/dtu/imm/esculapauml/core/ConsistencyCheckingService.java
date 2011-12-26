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
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.common.util.UML2Util;

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
		super();
	}

	public static ConsistencyCheckingService getInstance() {
		if (instance == null) {
			instance = new ConsistencyCheckingService();
		}
		return instance;
	}

	public void checkUseCaseInteraction(EObject interaction) {
		// check for right EObject type argument
		if (!(interaction instanceof org.eclipse.uml2.uml.Element)) {
			throw new IllegalArgumentException("Passed argument is not UML2 Element");
		}
		Interaction umlInteraction = null;
		if (interaction.eClass().getName().equals("Interaction")) {
			umlInteraction = (Interaction) interaction;
		} else {
			// if collaboration or use case, be flexible
			if (interaction.eClass().getName().equals("Collaboration") || interaction.eClass().getName().equals("UseCase")) {
				umlInteraction = (Interaction) UML2Util.findEObject(interaction.eAllContents(), new UML2Util.EObjectMatcher() {
					public boolean matches(EObject eObject) {
						return eObject.eClass().getName().equals("Interaction");
					}
				});
			}
		}

		if (null == umlInteraction) {
			throw new IllegalArgumentException("Cannot find interaction to check");
		}
		
		
	}
}
