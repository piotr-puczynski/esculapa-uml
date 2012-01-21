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

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.uml2.uml.Actor;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.BehavioredClassifier;
import org.eclipse.uml2.uml.ConnectableElement;
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dk.dtu.imm.esculapauml.core.checkers.CheckerInterface#check()
	 */
	@Override
	public void check() {
		structuralExistenceCheck();
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
				Behavior behavior = ((BehavioredClassifier) type).getClassifierBehavior();
				if (null == behavior) {
					addOtherProblem(Diagnostic.ERROR, "The Lifeline \"" + checkee.getLabel() + "\" representant \"" + type.getLabel()
							+ "\" has no behavior defined.", checkee, type);
				} else {
					if (behavior instanceof StateMachine) {
						prepareBehaviorCheckerForLifeline((BehavioredClassifier) type, (StateMachine) behavior);
					} else {
						addOtherProblem(Diagnostic.ERROR, "The Lifeline \"" + checkee.getLabel() + "\" representant \"" + type.getLabel()
								+ "\" has no behavior defined not as StateMachine.", checkee, type);
					}

				}
			}
		}
	}

	/**
	 * Initializes the checker for behavior of the lifeline representant
	 */
	protected void prepareBehaviorCheckerForLifeline(BehavioredClassifier type, StateMachine sm) {
		BehaviorChecker bc = new BehaviorChecker(systemState, diagnostics, sm, type);
		bc.check();
	}

}