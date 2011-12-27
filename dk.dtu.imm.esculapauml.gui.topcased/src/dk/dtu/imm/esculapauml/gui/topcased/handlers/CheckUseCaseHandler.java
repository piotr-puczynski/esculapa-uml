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


package dk.dtu.imm.esculapauml.gui.topcased.handlers;


import java.util.List;

import dk.dtu.imm.esculapauml.gui.topcased.utils.GuiUtils;
import dk.dtu.imm.esculapauml.core.ConsistencyCheckingService;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.ui.util.EditUIMarkerHelper;
import org.eclipse.emf.common.ui.MarkerHelper;
import org.eclipse.emf.common.util.Diagnostic;

/**
 * command to trigger use case consistency checking on selected interaction
 * 
 * @author Piotr. J. Puczynski (piotr.puczynski)
 *
 */
public class CheckUseCaseHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		List<?> elements = GuiUtils.getSelectionModelSubtreeContents(event);
		if((elements.size() > 0) && (elements.get(0) instanceof EObject)) {
			ConsistencyCheckingService.getInstance().checkUseCaseInteraction((EObject) elements.get(0));
		} else {
			throw new IllegalArgumentException("Passed argument is empty or of a wrong type (required EMF model element argument)");
		}

		MarkerHelper mh = new EditUIMarkerHelper();
		try {
			Diagnostic diag = ConsistencyCheckingService.getInstance().getDiagnostics();
			mh.deleteMarkers(GuiUtils.getSelectedResource(event));
			mh.createMarkers(diag);
			//org.topcased.validation.core.MarkerUtil.
		


			
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
		
	}

}
