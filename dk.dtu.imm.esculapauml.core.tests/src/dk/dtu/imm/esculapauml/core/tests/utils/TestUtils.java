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
package dk.dtu.imm.esculapauml.core.tests.utils;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.uml2.common.util.UML2Util;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.resource.UMLResource;

/**
 * Testing methods help
 * @author Piotr. J. Puczynski (piotr.puczynski)
 * 
 */
public class TestUtils {
	
	/**
	 * Loads resource file as UML2 model
	 * @param modelFileName
	 * @return resource
	 */
	public static Resource getUMLResource(String modelFileName) {
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getPackageRegistry().put(UMLPackage.eNS_URI, UMLPackage.eINSTANCE);

		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(UMLResource.FILE_EXTENSION, UMLResource.Factory.INSTANCE);
		// Map uriMap = resourceSet.getURIConverter().getURIMap();
		URI uri = URI.createURI("file:/C:/Users/s091137/git/EsculapaUML/ExampleModels/Models/" + modelFileName);

		return resourceSet.getResource(uri, true);
	}
	
	/**
	 * Finds an interaction in UML2 model based on given name
	 * @param model
	 * @param interactionName
	 * @return Interaction element
	 */
	public static Interaction getInteraction(Resource model, final String interactionName) {
		Interaction umlInteraction = null;
		umlInteraction = (Interaction) UML2Util.findEObject(model.getAllContents(), new UML2Util.EObjectMatcher() {
			public boolean matches(EObject eObject) {
				if(eObject.eClass().getName().equals("Interaction")) {
					return ((Interaction) eObject).getName().equals(interactionName);
				}
				return false;
			}
		});
		return umlInteraction;
	}
}
