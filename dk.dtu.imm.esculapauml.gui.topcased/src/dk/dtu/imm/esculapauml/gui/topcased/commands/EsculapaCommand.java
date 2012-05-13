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
package dk.dtu.imm.esculapauml.gui.topcased.commands;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.gef.commands.Command;
import org.eclipse.uml2.uml.Interaction;
import org.topcased.modeler.editor.Modeler;

import dk.dtu.imm.esculapauml.core.checkers.UseCaseChecker;
import dk.dtu.imm.esculapauml.gui.topcased.extenders.InteractionExtender;
import dk.dtu.imm.esculapauml.gui.topcased.fixers.InteractionOrderFixer;
import dk.dtu.imm.esculapauml.gui.topcased.observer.SimulationObserver;
import dk.dtu.imm.esculapauml.gui.topcased.utils.GuiUtils;
import dk.dtu.imm.esculapauml.gui.topcased.utils.TopcasedMarkerHelper;

/**
 * The main consistency checking command.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class EsculapaCommand extends Command {

	private EList<Interaction> interactions;
	private Resource res;
	private Modeler modeler;

	/**
	 * 
	 */
	public EsculapaCommand(Resource res, Modeler modeler, EList<Interaction> interactions) {
		super();
		this.interactions = interactions;
		this.res = res;
		this.modeler = modeler;
	}

	/**
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	@Override
	public void execute() {
		redo();
	}

	/**
	 * @see org.eclipse.gef.commands.Command#redo()
	 */
	@Override
	public void redo() {
		TopcasedMarkerHelper.deleteMarkers(res);
		for (Interaction interaction : interactions) {
			InteractionOrderFixer fixer = new InteractionOrderFixer(modeler, interaction);
			fixer.fix();
			if (fixer.hadFixedErrors()) {
				GuiUtils.showInfo("Interaction '" + interaction.getLabel()
						+ "' had order of messages in diagram inconsistent with the order in the model. The model was fixed before checking.", modeler);
			}
			UseCaseChecker checker = new UseCaseChecker(interaction);
			checker.getSystemState().getSimObservers().addListener(new SimulationObserver());
			checker.check();
			InteractionExtender ie = new InteractionExtender(modeler, checker.getCheckedObject());
			ie.extend();
			TopcasedMarkerHelper.createMarkers(checker.getDiagnostics(), res);
		}
		modeler.refreshOutline();
		modeler.refreshActiveDiagram();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#canUndo()
	 */
	@Override
	public boolean canUndo() {
		return false;
	}
}
