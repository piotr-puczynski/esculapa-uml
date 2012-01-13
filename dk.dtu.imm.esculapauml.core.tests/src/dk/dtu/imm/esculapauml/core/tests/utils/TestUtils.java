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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.common.util.UML2Util;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.resource.UMLResource;
import org.eclipse.emf.common.util.Diagnostic;

/**
 * Testing methods help
 * 
 * @author Piotr. J. Puczynski (piotr.puczynski)
 * 
 */
public class TestUtils {

	/**
	 * Loads resource file as UML2 model
	 * 
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
	 * 
	 * @param model
	 * @param interactionName
	 * @return Interaction element
	 */
	public static Interaction getInteraction(Resource model, final String interactionName) {
		Interaction umlInteraction = null;
		umlInteraction = (Interaction) UML2Util.findEObject(model.getAllContents(), new UML2Util.EObjectMatcher() {
			public boolean matches(EObject eObject) {
				if (eObject.eClass().getName().equals("Interaction")) {
					return ((Interaction) eObject).getName().equals(interactionName);
				}
				return false;
			}
		});
		return umlInteraction;
	}
	
	
	/**
	 * copies contents of a resource set into a new one
	 */
	public static Resource cloneResource(Resource source) {
		EcoreUtil.Copier copier = new EcoreUtil.Copier();
		ResourceSet target = new ResourceSetImpl();
		target.getPackageRegistry().put(UMLPackage.eNS_URI, UMLPackage.eINSTANCE);
		target.getResourceFactoryRegistry().getExtensionToFactoryMap().put(UMLResource.FILE_EXTENSION, UMLResource.Factory.INSTANCE);
		Resource resource2 = target.createResource(source.getURI());
		resource2.getContents().addAll(copier.copyAll(source.getContents()));
		copier.copyReferences();
		return resource2;
	}

	/**
	 * Gets the diagnostic leafs with errors or warnings as a list
	 * @param diagnostic
	 * @return
	 */
	public static List<Diagnostic> getDiagnosticErrorsAndWarnings(Diagnostic diagnostic) {
		ArrayList<Diagnostic> result = new ArrayList<Diagnostic>();

		if (!diagnostic.getChildren().isEmpty()) {
			for (Diagnostic childDiagnostic : diagnostic.getChildren()) {
				result.addAll(getDiagnosticErrorsAndWarnings(childDiagnostic));
			}
		} else {
			if (diagnostic.getSeverity() > Diagnostic.OK) {
				result.add(diagnostic);
			}
		}
		return result;
	}

	/**
	 * Checks if there is an error given in parameters in diagnostic
	 * @param diagnostic
	 * @param severity
	 * @param message
	 * @param objects
	 * @return
	 */
	public static boolean diagnosticExists(Diagnostic diagnostic, int severity, String message, Object... objects) {
		ArrayList<Object> diagobj = null;
		if (null != objects) {
			diagobj = new ArrayList<Object>();
		}
		for (Object obj : objects) {
			diagobj.add(obj);
		}
		List<Diagnostic> diagnostics = getDiagnosticErrorsAndWarnings(diagnostic);
		for (Diagnostic diag : diagnostics) {
			if ((diag.getSeverity() == severity) && (diag.getMessage().equals(message)) && (diag.getData().equals(diagobj))) {
				return true;
			}
		}
		return false;
	}
}
