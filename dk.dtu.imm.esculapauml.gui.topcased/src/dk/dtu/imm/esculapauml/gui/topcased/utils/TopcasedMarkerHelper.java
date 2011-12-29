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
package dk.dtu.imm.esculapauml.gui.topcased.utils;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.topcased.validation.core.MarkerUtil;

/**
 * This class uses methods available in org.topcased.validation.core.MarkerUtil
 * to show the errors in diagrams
 * 
 * @author Piotr. J. Puczynski (piotr.puczynski)
 * 
 */
public class TopcasedMarkerHelper {

	/**
	 * This code is a modified code based on createMarker from MarkerUtil. I
	 * fixed some bugs with the original code and added proper recursion.
	 * 
	 * @param diagnostic
	 * @param res
	 */

	public static void createMarkers(Diagnostic diagnostic, Resource res) {
		IFile file = MarkerUtil.getFile(res);
		createMarkers(diagnostic, file, "");
	}

	public static void createMarkers(Diagnostic diagnostic, IFile file, String parentMessage) {
		if (!diagnostic.getChildren().isEmpty()) {
			for (Diagnostic childDiagnostic : diagnostic.getChildren()) {
				if (null != diagnostic.getMessage() && (!diagnostic.getMessage().isEmpty())) {
					createMarkers(childDiagnostic, file, parentMessage + ". " + diagnostic.getMessage());
				} else {
					createMarkers(childDiagnostic, file, parentMessage);
				}

			}
		}
		List<?> data = diagnostic.getData();
		if ((null != data) && !data.isEmpty()) {
			for (Object eObject : data) {
				if (eObject instanceof EObject) {
					try {
						String message = parentMessage.isEmpty() ? diagnostic.getMessage() : parentMessage + ". " + diagnostic.getMessage();
						createMarker(file, diagnostic, (EObject) eObject, EcoreUtil.getURI((EObject) eObject).toString(), message);
					} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	private static IMarker createMarker(IResource file, Diagnostic diagnostic, EObject eObject, String uri, String message)
			throws CoreException {
		IMarker marker = file.createMarker(EValidator.MARKER);
		int severity = diagnostic.getSeverity();
		if (severity < Diagnostic.WARNING) {
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
		} else if (severity < Diagnostic.ERROR) {
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
		} else {
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
		}
		marker.setAttribute(IMarker.MESSAGE, message);
		if (eObject != null) {
			marker.setAttribute(EValidator.URI_ATTRIBUTE, uri);
			marker.setAttribute(IMarker.LOCATION, eObject.toString());
		}
		return marker;

	}

	public static void deleteMarkers(Resource res) {
		try {
			MarkerUtil.deleteMarkers(res.getResourceSet());
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

}
