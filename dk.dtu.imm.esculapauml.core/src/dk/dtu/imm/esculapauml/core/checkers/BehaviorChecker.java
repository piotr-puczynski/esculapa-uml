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
package dk.dtu.imm.esculapauml.core.checkers;

import java.util.ArrayList;

import static ch.lambdaj.Lambda.forEach;

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.uml2.uml.BehavioredClassifier;
import org.eclipse.uml2.uml.StateMachine;

import dk.dtu.imm.esculapauml.core.executors.BehaviorExecutor;
import dk.dtu.imm.esculapauml.core.states.SystemState;

/**
 * Checker for behaviors
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class BehaviorChecker extends AbstractStateMachineChecker {

	protected ArrayList<BehaviorExecutor> executors = new ArrayList<BehaviorExecutor>();

	/**
	 * @param existingDiagnostics
	 * @param objectToCheck
	 */
	public BehaviorChecker(SystemState systemState, BasicDiagnostic existingDiagnostics, StateMachine objectToCheck, BehavioredClassifier type) {
		super(systemState, existingDiagnostics, objectToCheck);
		systemState.registerBehaviorChecker(type, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dk.dtu.imm.esculapauml.core.checkers.CheckerInterface#check()
	 */
	@Override
	public void check() {
		checkRegions();

	}
	
	public void registerInstance(String name) {
		BehaviorExecutor be = new BehaviorExecutor(this, name);
		executors.add(be);
		if (!hasErrors()) {
			be.prepare();
		}
	}

	/**
	 * Return default executor for a class
	 * 
	 * @return
	 */
	public BehaviorExecutor getDefaultExecutor() {
		return executors.get(0);
	}

}
