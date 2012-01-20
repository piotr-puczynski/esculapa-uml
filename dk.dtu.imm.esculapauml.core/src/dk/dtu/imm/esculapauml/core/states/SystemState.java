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
package dk.dtu.imm.esculapauml.core.states;

import java.util.HashMap;

import org.eclipse.uml2.uml.BehavioredClassifier;
import dk.dtu.imm.esculapauml.core.checkers.BehaviorChecker;

/**
 * State of the whole system. Stores the checkers responsible for stateful
 * checks.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class SystemState {
	private HashMap<BehavioredClassifier, BehaviorChecker> behaviorCheckers = new HashMap<BehavioredClassifier, BehaviorChecker>();

	/**
	 * Init state
	 */
	public SystemState() {
		super();
	}

	public BehaviorChecker getBehaviorChecker(BehavioredClassifier type) {
		return behaviorCheckers.get(type);
	}

	public void registerChecker(BehavioredClassifier type, BehaviorChecker checker) {
		behaviorCheckers.put(type, checker);
	}

}
