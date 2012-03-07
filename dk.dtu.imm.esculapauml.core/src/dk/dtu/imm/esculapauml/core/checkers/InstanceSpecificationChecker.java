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

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.BehavioredClassifier;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.InstanceSpecification;
import org.eclipse.uml2.uml.StateMachine;

/**
 * Checks existing instance specifications.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class InstanceSpecificationChecker extends AbstractChecker<InstanceSpecification> {

	/**
	 * @param checker
	 * @param objectToCheck
	 */
	InstanceSpecificationChecker(Checker checker, InstanceSpecification objectToCheck) {
		super(checker, objectToCheck);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dk.dtu.imm.esculapauml.core.checkers.Checker#check()
	 */
	@Override
	public void check() {
		structuralCheck();
		if (!hasErrors()) {
			generateExecutor();
		}
	}

	/**
	 * 
	 */
	private void generateExecutor() {
		if (!checkee.getClassifiers().isEmpty()) {
			Classifier classifier = checkee.getClassifiers().get(0);
			if (classifier instanceof BehavioredClassifier) {
				if(classifier.isAbstract()) {
					addProblem(Diagnostic.ERROR, "Instance specification '" + checkee.getLabel() + "' is of an abstract type.");
				} else {
					Behavior behavior = ((BehavioredClassifier) classifier).getClassifierBehavior();
					if(behavior instanceof StateMachine) {
						BehaviorChecker bc = systemState.getBehaviorChecker((BehavioredClassifier) classifier);
						if (null == bc) {
							bc = new BehaviorChecker(this, (BehavioredClassifier) classifier);
							bc.check();
						}
						if (!bc.hasErrors()) {
							bc.registerInstance(checkee);
						}
					} else {
						addProblem(Diagnostic.WARNING, "Instance specification '" + checkee.getLabel() + "' has a classifier with no behavior as State Machine. Cannot create executor for this behavior.");
					}
				}
			}
		}
	}

	/**
	 * 
	 */
	private void structuralCheck() {
		if (checkee.getClassifiers().isEmpty()) {
			addProblem(Diagnostic.ERROR, "Instance specification '" + checkee.getLabel() + "' does not have any classifier type set.");
		}

	}

}
