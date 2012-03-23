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

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.BehavioredClassifier;
import org.eclipse.uml2.uml.InstanceSpecification;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.StateMachine;

import dk.dtu.imm.esculapauml.core.executors.BehaviorExecutor;

/**
 * Checker for behaviors
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class BehaviorChecker extends AbstractStateMachineChecker {

	private BehavioredClassifier type;
	private EList<Interface> interfaces = null;

	/**
	 * @param existingDiagnostics
	 * @param objectToCheck
	 */
	public BehaviorChecker(Checker checker, BehavioredClassifier type) {
		super(checker, ((type.getClassifierBehavior() == null) ? null : (StateMachine) type.getClassifierBehavior()));
		this.type = type;
		systemState.registerBehaviorChecker(type, this);
		logger = Logger.getLogger(BehaviorChecker.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dk.dtu.imm.esculapauml.core.checkers.CheckerInterface#check()
	 */
	@Override
	public void check() {
		logger.debug(checkee.getLabel() + ": start check");
		checkInterfaceRealizations();
		checkBehavior();
		if (!hasErrors()) {
			checkRegions();
		}

	}

	/**
	 * For behavioral type, check the interface realizations and if type
	 * conforms to the interface.
	 * 
	 */
	private void checkInterfaceRealizations() {
		interfaces = type.getAllImplementedInterfaces();
		for(Interface interface_: interfaces) {
			InterfaceRealizationChecker ic = new InterfaceRealizationChecker(this, type, interface_);
			ic.check();
		}

	}

	/**
	 * 
	 */
	private void checkBehavior() {
		if (!(checkee instanceof StateMachine)) {
			addOtherProblem(Diagnostic.ERROR, "Classifier '" + type.getLabel() + "' no State Machine behavior defined.", type);
		}
	}

	/**
	 * Creates new executor and instance specification with given name.
	 * 
	 * @param name
	 * @return
	 */
	public BehaviorExecutor registerInstance(String name) {
		BehaviorExecutor be = new BehaviorExecutor(this, name);
		if (!hasErrors()) {
			be.prepare();
		}

		return be;
	}

	/**
	 * Creates new executor from existing instance specification.
	 * 
	 * @param instanceSpecification
	 * @return
	 */
	public BehaviorExecutor registerInstance(InstanceSpecification instanceSpecification) {
		BehaviorExecutor be = new BehaviorExecutor(this, instanceSpecification);
		if (!hasErrors()) {
			be.prepare();
		}

		return be;
	}

}
