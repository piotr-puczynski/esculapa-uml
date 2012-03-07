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
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.uml2.uml.Actor;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.BehavioredClassifier;
import org.eclipse.uml2.uml.ConnectableElement;
import org.eclipse.uml2.uml.InstanceSpecification;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.Type;

import dk.dtu.imm.esculapauml.core.states.SystemState;

/**
 * Checker for lifelines
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class LifelineChecker extends AbstractChecker<Lifeline> {
	/**
	 * @param existingDiagnostics
	 */
	public LifelineChecker(SystemState systemState, BasicDiagnostic existingDiagnostics, Lifeline lifeline) {
		super(systemState, existingDiagnostics, lifeline);
		logger = Logger.getLogger(LifelineChecker.class);
	}

	/**
	 * @param checker
	 * @param lifeline
	 */
	public LifelineChecker(Checker checker, Lifeline lifeline) {
		super(checker, lifeline);
		logger = Logger.getLogger(LifelineChecker.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dk.dtu.imm.esculapauml.core.checkers.CheckerInterface#check()
	 */
	@Override
	public void check() {
		logger.debug(checkee.getLabel() + ": start check");
		structuralExistenceCheck();
		fregmentsCheck();
	}

	/**
	 * Checks all interaction fragments (structural check).
	 * 
	 */
	protected void fregmentsCheck() {
		CollectionChecker<?> cc = new CollectionChecker<InteractionFragment>(this, checkee.getCoveredBys());
		cc.check();
	}

	/**
	 * Check if lifeline corresponds to some class in the model
	 */
	protected void structuralExistenceCheck() {
		ConnectableElement connection = checkee.getRepresents();
		// representant is not set at all
		if (null == connection) {
			addProblem(Diagnostic.ERROR, "The Lifeline \"" + checkee.getLabel() + "\" has no representant.");
		} else {
			Type type = connection.getType();
			// representant set to nothing
			if (null == type) {
				addProblem(Diagnostic.ERROR, "The Lifeline \"" + checkee.getLabel() + "\" has no representant set to any type.");
			} else {
				checkBehaviorExistence(type);
			}
		}
	}

	/**
	 * Check if lifelines representans has behavior
	 */
	protected void checkBehaviorExistence(Type type) {
		if (type instanceof BehavioredClassifier) {
			// we do not expect actor to have defined behavior
			if (!(type instanceof Actor)) {
				// if class is abstract we should not instantiate it
				if (((BehavioredClassifier) type).isAbstract()) {
					addOtherProblem(Diagnostic.ERROR, "The Lifeline '" + checkee.getLabel() + "' representant '" + type.getLabel()
							+ "' is abstract and cannot be instantiated.", checkee, type);
				} else {
					Behavior behavior = ((BehavioredClassifier) type).getClassifierBehavior();
					if (null == behavior) {
						addOtherProblem(Diagnostic.ERROR, "The Lifeline \"" + checkee.getLabel() + "\" representant \"" + type.getLabel()
								+ "\" has no behavior defined.", checkee, type);
					} else {
						if (behavior instanceof StateMachine) {
							prepareBehaviorCheckerForLifeline((BehavioredClassifier) type);
						} else {
							addOtherProblem(Diagnostic.ERROR, "The Lifeline \"" + checkee.getLabel() + "\" representant \"" + type.getLabel()
									+ "\" has no behavior defined not as StateMachine.", checkee, type);
						}

					}
				}
			}
		}
	}

	/**
	 * Initializes the checker for behavior of the lifeline representant
	 */
	protected void prepareBehaviorCheckerForLifeline(BehavioredClassifier type) {
		BehaviorChecker bc = systemState.getBehaviorChecker(type);
		if (null == bc) {
			bc = new BehaviorChecker(this, type);
			bc.check();
		}
		if (!bc.hasErrors()) {
			// check if instance was specified by the user
			InstanceSpecification is = systemState.getExistingInstanceSpecification(checkee.getName(), type);
			if (null == is) {
				addProblem(Diagnostic.OK, "The Lifeline '" + checkee.getLabel()
						+ "' has no provided instance specification. The default instance will be generated.");
				// register new instance for lifeline
				bc.registerInstance(checkee.getName());
			}
		}
	}

}
