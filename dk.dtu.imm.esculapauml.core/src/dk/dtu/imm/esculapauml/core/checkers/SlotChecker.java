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
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.InstanceSpecification;
import org.eclipse.uml2.uml.Slot;
import org.eclipse.uml2.uml.StructuralFeature;

/**
 * Checks slots in instance specifications.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class SlotChecker extends AbstractChecker<Slot> {

	/**
	 * @param checker
	 * @param objectToCheck
	 */
	SlotChecker(Checker checker, Slot objectToCheck) {
		super(checker, objectToCheck);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dk.dtu.imm.esculapauml.core.checkers.Checker#check()
	 */
	@Override
	public void check() {
		checkDefiningFeature();
	}

	/**
	 * Checks if defining feature was set correctly.
	 */
	private void checkDefiningFeature() {
		StructuralFeature sf = checkee.getDefiningFeature();
		if (null == sf) {
			addProblem(Diagnostic.ERROR, "Slot has no defining feature set.");
		} else {
			InstanceSpecification instance = checkee.getOwningInstance();
			// check if there is a feature in classifier defining owning
			// instance
			if (!instance.getClassifiers().isEmpty()) {
				Classifier classifier = instance.getClassifiers().get(0);
				if (!classifier.getFeatures().contains(sf)) {
					addProblem(Diagnostic.ERROR,
							"Slot defining feature '" + sf.getLabel() + "' has no representation in instance classifier '" + classifier.getLabel() + "'.");
				}
			}

		}

	}
}
