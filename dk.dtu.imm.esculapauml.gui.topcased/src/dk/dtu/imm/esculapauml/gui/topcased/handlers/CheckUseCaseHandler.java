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

import dk.dtu.imm.esculapauml.gui.topcased.commands.EsculapaCommand;
import dk.dtu.imm.esculapauml.gui.topcased.utils.GuiUtils;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Interaction;
import org.topcased.modeler.editor.Modeler;

/**
 * command to trigger use case consistency checking on selected interaction
 * 
 * @author Piotr. J. Puczynski (piotr.puczynski)
 * 
 */
public class CheckUseCaseHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		List<?> elements = GuiUtils.getSelectionModelSubtreeContents(event);
		Modeler modeler = GuiUtils.getModeler(event);
		EList<Interaction> interactions = GuiUtils.getUMLInteractionArgument(elements);
		if (interactions.isEmpty()) {
			GuiUtils.showError("Selection does not contain any UML2 Interaction elements.", modeler);
			return null;
		}
		Resource res = GuiUtils.getSelectedResource(event);
		EsculapaCommand esculapaCommand = new EsculapaCommand(res, modeler, interactions);
		
		GuiUtils.getCommandStack(event).execute(esculapaCommand);
		

		return null;

	}

}
