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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Component;
import org.eclipse.uml2.uml.Dependency;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.InterfaceRealization;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.TypedElement;
import org.eclipse.uml2.uml.UMLPackage.Literals;

import dk.dtu.imm.esculapauml.core.utils.UMLStructureUtils;

/**
 * Checks UML component and its contents.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class ComponentChecker extends AbstractChecker<Component> {

	private EList<EObject> elements;

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
		elements = getElementsExcludingOtherComponents();
		checkAssociationsBetweenComponents();
		checkDependenciesBetweenComponents();
		checkGeneralizationsBetweenComponents();
		checkTypedElements();
		checkProvidedInterfacesRealizations();
	}

	/**
	 * Finds provided interfaces of component and checks if they are realized by
	 * classes.
	 */
	private void checkProvidedInterfacesRealizations() {
		Map<Interface, EList<InterfaceRealization>> interfacesMap = new HashMap<Interface, EList<InterfaceRealization>>();
		for (EObject element : elements) {
			if (element instanceof InterfaceRealization) {
				InterfaceRealization ir = (InterfaceRealization) element;
				if (null != ir.getContract()) {
					EList<InterfaceRealization> l = interfacesMap.get(ir.getContract());
					if (null == l) {
						interfacesMap.put(ir.getContract(), l = new BasicEList<InterfaceRealization>());
					}
					l.add(ir);
				}
			}
		}

		for (Entry<Interface, EList<InterfaceRealization>> entry : interfacesMap.entrySet()) {
			boolean isProvided = false;
			boolean isRealized = false;
			for (InterfaceRealization ir : entry.getValue()) {
				if (ir.getClients().contains(checkee)) {
					isProvided = true;
				}
				if (!isRealized) {
					for (NamedElement ne : ir.getClients()) {
						if (ne != checkee && ne.eClass() == Literals.CLASS && elements.contains(ne)) {
							isRealized = true;
							break;
						}
					}
				}
			}
			if (isProvided && !isRealized) {
				addOtherProblem(Diagnostic.ERROR, "Provided interface '" + entry.getKey().getLabel() + "' is not realized by any class in component '"
						+ checkee.getLabel() + "'.'", checkee, entry.getKey());
			}
		}

	}

	/**
	 * Check all typed elements if they refer not to other components.
	 */
	private void checkTypedElements() {
		for (EObject element : elements) {
			if (element instanceof TypedElement) {
				Type type = ((TypedElement) element).getType();
				if (null != type) {
					if (!elements.contains(type)) {
						Component comp = UMLStructureUtils.getOwningComponent(type);
						if (null != comp && comp != checkee) {
							addOtherProblem(Diagnostic.ERROR, "The component '" + checkee.getLabel() + "' has element '" + ((TypedElement) element).getLabel()
									+ "' of type '" + type.getLabel() + "' that is located in other component '" + comp.getLabel() + "'.", element);
						}
					}
				}
			}
		}

	}

	/**
	 * Checks dependencies between components.
	 */
	private void checkDependenciesBetweenComponents() {
		for (EObject element : elements) {
			if (element instanceof NamedElement) {
				for (Dependency dep : ((NamedElement) element).getClientDependencies()) {
					for (NamedElement type : dep.getSuppliers()) {
						if (type != element && null != type) {
							if (!elements.contains(type)) {
								Component comp = UMLStructureUtils.getOwningComponent(type);
								if (null != comp && comp != checkee) {
									addOtherProblem(
											Diagnostic.ERROR,
											"The component '" + checkee.getLabel() + "' has classifier '" + ((Classifier) element).getLabel()
													+ "' that is dependend from type '" + type.getLabel() + "' that is located in other component '"
													+ comp.getLabel() + "'.", dep);
								}
							}
						}
					}
				}
			}
		}

	}

	/**
	 * Check if no associations point outside of this component to another
	 * component.
	 * 
	 */
	private void checkAssociationsBetweenComponents() {
		for (EObject element : elements) {
			if (element instanceof Type) {
				for (Association assoc : ((Type) element).getAssociations()) {
					for (Property prop : assoc.getOwnedEnds()) {
						Type type = prop.getType();
						if (type != element && null != type) {
							if (!elements.contains(type)) {
								Component comp = UMLStructureUtils.getOwningComponent(type);
								if (null != comp && comp != checkee) {
									addOtherProblem(Diagnostic.ERROR, "The component '" + checkee.getLabel() + "' has type '" + ((Type) element).getLabel()
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
	 * Check if no generalizations point outside of this component to another
	 * component.
	 * 
	 */
	private void checkGeneralizationsBetweenComponents() {
		for (EObject element : elements) {
			if (element instanceof Classifier) {
				for (Classifier general : ((Classifier) element).getGenerals()) {
					if (!elements.contains(general)) {
						Component comp = UMLStructureUtils.getOwningComponent(general);
						if (null != comp && comp != checkee) {
							addOtherProblem(Diagnostic.ERROR, "The component '" + checkee.getLabel() + "' has classifier '" + ((Classifier) element).getLabel()
									+ "' that has general type '" + general.getLabel() + "' that is located in other component '" + comp.getLabel() + "'.",
									element);
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
				result.add(o);
				it.prune();
			} else {
				result.add(o);
			}
		}
		return result;
	}

}
