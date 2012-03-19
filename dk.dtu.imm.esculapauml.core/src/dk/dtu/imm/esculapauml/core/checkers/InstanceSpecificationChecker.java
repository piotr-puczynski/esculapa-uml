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

import java.util.Iterator;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.BehavioredClassifier;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.InstanceSpecification;
import org.eclipse.uml2.uml.InstanceValue;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Slot;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.ValueSpecification;

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
				if (classifier.isAbstract()) {
					addProblem(Diagnostic.ERROR, "Instance specification '" + checkee.getLabel() + "' is of an abstract type.");
				} else {
					Behavior behavior = ((BehavioredClassifier) classifier).getClassifierBehavior();
					if (behavior instanceof StateMachine) {
						BehaviorChecker bc = systemState.getBehaviorChecker((BehavioredClassifier) classifier);
						if (null == bc) {
							bc = new BehaviorChecker(this, (BehavioredClassifier) classifier);
							bc.check();
						}
						if (!bc.hasErrors()) {
							bc.registerInstance(checkee);
						}
					} else {
						addProblem(Diagnostic.WARNING, "Instance specification '" + checkee.getLabel()
								+ "' has a classifier with no behavior as State Machine. Cannot create executor for this behavior.");
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
		} else {
			// cardinality check
			for (Association association : checkee.getClassifiers().get(0).getAssociations()) {
				// get all instances for this association
				EList<InstanceSpecification> links = systemState.getExistingInstanceSpecifications(association);
				// filter only these that are connected to us
				Iterator<InstanceSpecification> it = links.iterator();
				while (it.hasNext()) {
					InstanceSpecification is = it.next();
					boolean found = false;
					for (Slot slot : is.getSlots()) {
						for (ValueSpecification val : slot.getValues()) {
							if (val instanceof InstanceValue) {
								if (((InstanceValue) val).getInstance() == checkee) {
									found = true;
								}
							}
						}
					}
					if (!found) {
						it.remove();
					}
				}

				int min = -2;
				int max = -2;
				// check conformance to multiplicitiy
				for (Property prop : association.getAllAttributes()) {
					if (prop.getType() != checkee.getClassifiers().get(0)) {
						min = prop.getLower();
						max = prop.getUpper();
					}
				}
				// self association
				if (min == -2 || max == -2) {
					for (Property prop : association.getAllAttributes()) {
						min = prop.getLower();
						max = prop.getUpper();
					}
				}
				if (min > -2 && max > -2) {
					if (links.size() < min) {
						addProblem(Diagnostic.ERROR,
								"Instance specification '" + checkee.getLabel() + "' does not satisfy lower bound of links (" + String.valueOf(min)
										+ ") according to multiplicity of association '" + association.getLabel() + "'.");
					} else if (max >= 0) { // -1 represents infinity
						if (links.size() > max) {
							addProblem(Diagnostic.ERROR,
									"Instance specification '" + checkee.getLabel() + "' does not satisfy upper bound of links (" + String.valueOf(max)
											+ ") according to multiplicity of association '" + association.getLabel() + "'.");
						}
					}
				}

			}
		}

	}

}
