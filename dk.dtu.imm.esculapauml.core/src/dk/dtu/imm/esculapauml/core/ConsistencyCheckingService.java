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

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.common.util.UML2Util;

import dk.dtu.imm.esculapauml.core.checkers.UseCaseChecker;

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
	private Diagnostic diagnostics;

	/**
	 * @return the diagnostics
	 */
	public Diagnostic getDiagnostics() {
		return diagnostics;
	}

	/**
	 * Internal method used to validate arguments and to convert them into UML2
	 * elements
	 * 
	 * @param someObject
	 *            interaction, use case or collaboration
	 * @return uml2 interaction
	 */
	private Interaction getUMLInteractionArgument(EObject someObject) {
		// check for right EObject type argument
		if (!(someObject instanceof org.eclipse.uml2.uml.Element)) {
			throw new IllegalArgumentException("Passed argument is not UML2 Element");
		}
		Interaction umlInteraction = null;
		if (someObject.eClass().getName().equals("Interaction")) {
			umlInteraction = (Interaction) someObject;
		} else {
			// if collaboration or use case, be flexible
			if (someObject.eClass().getName().equals("Collaboration") || someObject.eClass().getName().equals("UseCase")) {
				umlInteraction = (Interaction) UML2Util.findEObject(someObject.eAllContents(), new UML2Util.EObjectMatcher() {
					public boolean matches(EObject eObject) {
						return eObject.eClass().getName().equals("Interaction");
					}
				});
			}
		}

		if (null == umlInteraction) {
			throw new IllegalArgumentException("Cannot find interaction to check");
		}

		return umlInteraction;
	}

	protected ConsistencyCheckingService() {
		super();
	}

	public static ConsistencyCheckingService getInstance() {
		if (instance == null) {
			instance = new ConsistencyCheckingService();
		}
		return instance;
	}

	/**
	 * Start consistency check of use case (as interaction diagram).
	 * 
	 * @param interaction
	 *            must be UML2 element containing interaction to check. It is
	 *            allowed to pass use case or collaboration if they contain an
	 *            interaction.
	 * @return checked and completed interaction.
	 */
	public Interaction checkUseCaseInteraction(EObject interaction) {
		UseCaseChecker checker = new UseCaseChecker(getUMLInteractionArgument(interaction));
		diagnostics = checker.getDiagnostics();
		checker.check();
		return checker.getInteraction();
	}

	/**
	 * Start consistency check of interaction diagram.
	 * 
	 * @param interaction
	 *            must be UML2 element containing interaction to check. It is
	 *            allowed to pass use case or collaboration if they contain an
	 *            interaction.
	 * @return checked interaction.
	 */
	public Interaction checkInteraction(EObject interaction) {
		Interaction umlInteraction = getUMLInteractionArgument(interaction);

		return umlInteraction;

	}
}
