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
import java.util.Iterator;
import dk.dtu.imm.esculapauml.gui.topcased.utils.GuiUtils;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.topcased.modeler.DuplicationAdapter;
import org.topcased.modeler.actions.DuplicateSubTreeAction;
import org.topcased.modeler.editor.Modeler;

/**
 * command to trigger use case consistency checking on selected interaction
 * 
 * @author Piotr. J. Puczynski (piotr.puczynski)
 *
 */
public class CheckUseCase extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		/*List<?> elements = GuiUtils.getSelectionModelSubtreeContents(event);
		System.out.println(elements.get(0).toString());
		Iterator<EObject> itChildren = ((EObject) elements.get(0)).eAllContents();
		while (itChildren.hasNext()) {
			EObject o = itChildren.next();
			System.out.println(o.toString());
		}*/
		List<?> elements = GuiUtils.getSelectionModelSubtreeContents(event);
		Modeler modeler = GuiUtils.getModeler(event);
		modeler.gotoEObject((EObject) elements.get(0));
		return null;
		
	}

}
