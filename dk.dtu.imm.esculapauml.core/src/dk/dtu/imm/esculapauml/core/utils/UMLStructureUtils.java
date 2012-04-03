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
package dk.dtu.imm.esculapauml.core.utils;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.uml2.uml.Component;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.util.UMLUtil;

/**
 * Structural UML handful functions.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public final class UMLStructureUtils {

	/**
	 * Gets owning component of the element.
	 * 
	 * @param element
	 * @return
	 */
	public static Component getOwningComponent(Element element) {
		return (Component) getOwningElement(element, UMLPackage.Literals.COMPONENT, true);
	}

	/**
	 * Finds owning element of given class.
	 * 
	 * @param element
	 * @param eClass
	 * @param resolve
	 * @return
	 */
	public static Element getOwningElement(Element element, EClass eClass, boolean resolve) {
		Element owningElement = null;

		for (Element owner = element; ((owningElement = (Element) owner.eGet(UMLPackage.Literals.ELEMENT__OWNER, resolve)) == null ? owner = owningElement = UMLUtil
				.getBaseElement(owner.eContainer()) : owningElement) != null
				&& !(eClass.isInstance(owningElement));) {

			owner = owner.getOwner();
		}

		return owningElement;
	}
}
