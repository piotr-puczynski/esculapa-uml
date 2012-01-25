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
	private HashMap<BehavioredClassifier, BehaviorChecker> behaviorExecutors = new HashMap<BehavioredClassifier, BehaviorChecker>();

	/**
	 * Init state
	 */
	public SystemState() {
		super();
	}

	public BehaviorChecker getBehaviorExecutor(BehavioredClassifier type) {
		return behaviorExecutors.get(type);
	}

	public void registerBehaviorExecutor(BehavioredClassifier type, BehaviorChecker checker) {
		behaviorExecutors.put(type, checker);
	}

}
