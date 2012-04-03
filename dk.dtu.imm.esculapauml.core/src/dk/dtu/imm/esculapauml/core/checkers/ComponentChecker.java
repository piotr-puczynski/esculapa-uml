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

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Component;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLPackage.Literals;

import dk.dtu.imm.esculapauml.core.utils.UMLStructureUtils;

/**
 * Checks UML component and its contents.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class ComponentChecker extends AbstractChecker<Component> {

	/**
	 * @param checker
	 * @param objectToCheck
	 */
	ComponentChecker(Checker checker, Component objectToCheck) {
		super(checker, objectToCheck);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dk.dtu.imm.esculapauml.core.checkers.Checker#check()
	 */
	@Override
	public void check() {
		EList<EObject> elements = getElementsExcludingOtherComponents();
		checkAssociationsBetweenComponents(elements);
	}

	/**
	 * Check if no associations point outside of this component to another
	 * component.
	 * 
	 * @param elements
	 */
	private void checkAssociationsBetweenComponents(EList<EObject> elements) {
		for (EObject element : elements) {
			if (element instanceof Type) {
				for (Association assoc : ((Type) element).getAssociations()) {
					for (Property prop : assoc.getOwnedEnds()) {
						Type type = prop.getType();
						if (type != element && null != type) {
							if (!elements.contains(type)) {
								Component comp = UMLStructureUtils.getOwningComponent(type);
								if (null != comp && comp != checkee) {
									addOtherProblem(Diagnostic.ERROR, "The component \"" + checkee.getLabel() + "\" has type '" + ((Type) element).getLabel()
											+ "' that has association to other type '" + type.getLabel() + "' in other component '" + comp.getLabel() + "'.",
											assoc);
								}
							}
						}
					}
				}
			}
		}

	}

	/**
	 * Finds contents of components excluding other components contents.
	 * 
	 * @return
	 * 
	 */
	private BasicEList<EObject> getElementsExcludingOtherComponents() {
		BasicEList<EObject> result = new BasicEList<EObject>();
		TreeIterator<EObject> it = checkee.eAllContents();
		while (it.hasNext()) {
			EObject o = it.next();
			if (o.eClass() == Literals.COMPONENT) {
				it.prune();
			} else {
				result.add(o);
			}
		}
		return result;
	}

}
