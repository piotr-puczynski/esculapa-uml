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
import org.eclipse.uml2.uml.Model;

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
	private org.eclipse.uml2.uml.Package instancePackage = null;
	private int stateId = -1;

	/**
	 * Init state
	 */
	public SystemState() {
		super();
	}
	
	/**
	 * Prepares the state in case of subsequent execution.
	 * @param model 
	 */
	public void prepare(String name, Model model) {
		instancePackage = model.createNestedPackage(name + " Instance(" + ++stateId + ")");
	}

	public BehaviorChecker getBehaviorChecker(BehavioredClassifier type) {
		return behaviorCheckers.get(type);
	}

	public void registerBehaviorChecker(BehavioredClassifier type, BehaviorChecker checker) {
		behaviorCheckers.put(type, checker);
	}

	/**
	 * @return the instancePackage
	 */
	public org.eclipse.uml2.uml.Package getInstancePackage() {
		return instancePackage;
	}
	
	

}
