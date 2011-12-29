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
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.uml2.uml.resource.UMLResource;

/**
 * @author Piotr. J. Puczynski (piotr.puczynski)
 * 
 */
public class TestUtils {
	public static Resource getUMLResource(String modelFileName) {
		ResourceSet resourceSet = new ResourceSetImpl();
		//resourceSet.getPackageRegistry().put(UMLPackage.eNS_URI, UMLPackage.eINSTANCE);

		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(UMLResource.FILE_EXTENSION, UMLResource.Factory.INSTANCE);
		// Map uriMap = resourceSet.getURIConverter().getURIMap();
		URI uri = URI.createURI("file:/C:/Users/s091137/git/EsculapaUML/ExampleModels/Models/" + modelFileName);

		return resourceSet.getResource(uri, true);
	}
}
