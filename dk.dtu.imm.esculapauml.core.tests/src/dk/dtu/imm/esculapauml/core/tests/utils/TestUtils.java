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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.common.util.UML2Util;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.resource.UMLResource;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.compare.diff.metamodel.DiffElement;
import org.eclipse.emf.compare.diff.metamodel.DiffModel;
import org.eclipse.emf.compare.diff.service.DiffEngineRegistry;
import org.eclipse.emf.compare.diff.service.DiffService;
import org.eclipse.emf.compare.match.MatchOptions;
import org.eclipse.emf.compare.match.metamodel.MatchModel;
import org.eclipse.emf.compare.match.service.MatchEngineRegistry;
import org.eclipse.emf.compare.match.service.MatchService;
import org.eclipse.emf.compare.uml2.diff.UML2DiffEngine;
import org.eclipse.emf.compare.uml2.match.UML2MatchEngine;

/**
 * Testing methods help
 * 
 * @author Piotr. J. Puczynski (piotr.puczynski)
 * 
 */
public class TestUtils {

	private static boolean compareInit = false;
	private static boolean umlInit = false;
	private static final String repositoryPath = System.getProperty("user.home") + "/git/EsculapaUML/";

	/**
	 * Compares two UML2 models using EMF Compare and checks if there are any
	 * differences.
	 * 
	 * @param model1
	 * @param model2
	 * @return true if models are the same
	 * @throws InterruptedException
	 */
	public static boolean modelsHaveNoDifferences(Resource model1, Resource model2) throws InterruptedException {
		if (!compareInit) {
			DiffEngineRegistry.INSTANCE.putValue("uml", new UML2DiffEngine());
			MatchEngineRegistry.INSTANCE.putValue("uml", new UML2MatchEngine());
			compareInit = true;
		}
		Map<String, Object> options = new HashMap<String, Object>();
		// option to avoid diffs of generator's random ids
		options.put(MatchOptions.OPTION_IGNORE_XMI_ID, true);
		MatchModel match = MatchService.doMatch(model1.getContents().get(0), model2.getContents().get(0), options);
		DiffModel diff = DiffService.doDiff(match, false);
		List<DiffElement> differences = new ArrayList<DiffElement>(diff.getDifferences());
		if (!differences.isEmpty()) {
			System.out.println("Differences start:");
			for (DiffElement de : differences) {
				System.out.println(de.toString());
			}
			System.out.println("Differences end.");
		}
		return differences.isEmpty();
	}

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
		// prepare UML primitive types
		// if (!umlInit) {
		// URIConverter.URI_MAP.put(URI.createURI(UMLResource.UML_PRIMITIVE_TYPES_LIBRARY_URI),
		// URI.createFileURI(repositoryPath +
		// "ExampleModels/SystemLibs/libraries/UMLPrimitiveTypes.library.uml"));
		// }

		// Map uriMap = resourceSet.getURIConverter().getURIMap();
		if (!umlInit) {
			System.setProperty("org.eclipse.ocl.uml", repositoryPath + "dk.dtu.imm.esculapauml.core.tests/libs/ocl");
			System.setProperty("org.eclipse.uml2.uml.resources", repositoryPath + "dk.dtu.imm.esculapauml.core.tests/libs/uml");
			org.eclipse.ocl.uml.OCL.initialize(null); // null for global init
			org.eclipse.ocl.uml.OCL.initialize(resourceSet);
			umlInit = true;
		}

		URI uri = URI.createURI("file:/" + repositoryPath + "ExampleModels/Models/" + modelFileName);

		Resource result = resourceSet.getResource(uri, true);

		return result;
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
	 * Finds transition by name. Looks in all state machines.
	 * 
	 * @param model
	 * @param transitionName
	 * @return
	 */
	public static Transition getTransitionByName(Resource model, final String transitionName) {
		Transition result = (Transition) UML2Util.findEObject(model.getAllContents(), new UML2Util.EObjectMatcher() {
			public boolean matches(EObject eObject) {
				if (eObject instanceof Transition) {
					return ((Transition) eObject).getName().equals(transitionName);
				}
				return false;
			}
		});
		return result;
	}

	/**
	 * Copies contents of a resource into a new one
	 * 
	 * @param source
	 * @return copied resource
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
	 * 
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
	 * 
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

			for (Object obj : objects) {
				diagobj.add(obj);
			}
		}
		List<Diagnostic> diagnostics = getDiagnosticErrorsAndWarnings(diagnostic);
		for (Diagnostic diag : diagnostics) {
			if ((diag.getSeverity() == severity) && (diag.getMessage().equals(message)) && (diag.getData().equals(diagobj))) {
				return true;
			}
		}
		return false;
	}

	public static boolean diagnosticMessageExists(Diagnostic diagnostic, int severity, String message) {
		List<Diagnostic> diagnostics = getDiagnosticErrorsAndWarnings(diagnostic);
		for (Diagnostic diag : diagnostics) {
			if ((diag.getSeverity() == severity) && (diag.getMessage().equals(message))) {
				return true;
			}
		}
		return false;
	}

	public static void printDiagnostic(Diagnostic diagnostic) {
		printDiagnostic(diagnostic, "");
	}

	private static void printDiagnostic(Diagnostic diagnostic, String parentMessage) {
		if (!diagnostic.getChildren().isEmpty()) {
			for (Diagnostic childDiagnostic : diagnostic.getChildren()) {
				if (null != diagnostic.getMessage() && (!diagnostic.getMessage().isEmpty())) {
					printDiagnostic(childDiagnostic, parentMessage + ". " + diagnostic.getMessage());
				} else {
					printDiagnostic(childDiagnostic, parentMessage);
				}

			}
		}
		List<?> data = diagnostic.getData();
		if ((null != data) && !data.isEmpty()) {
			for (Object eObject : data) {
				if (eObject instanceof EObject) {
					String message = parentMessage.isEmpty() ? diagnostic.getMessage() : parentMessage + ". " + diagnostic.getMessage();
					String severityString = "";
					switch (diagnostic.getSeverity()) {
					case Diagnostic.OK:
						severityString = "OK";
						break;
					case Diagnostic.INFO:
						severityString = "INFO";
						break;
					case Diagnostic.WARNING:
						severityString = "WARNING";
						break;
					case Diagnostic.ERROR:
						severityString = "ERROR";
						break;
					case Diagnostic.CANCEL:
						severityString = "CANCEL";
						break;
					}
					System.out.println(severityString + ": " + message);
				}
			}
		}
	}
}
