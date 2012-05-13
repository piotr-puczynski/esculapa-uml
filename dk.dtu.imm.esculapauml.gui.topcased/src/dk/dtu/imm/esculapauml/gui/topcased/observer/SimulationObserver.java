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
package dk.dtu.imm.esculapauml.gui.topcased.observer;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.uml2.uml.Transition;
import org.topcased.modeler.editor.Modeler;

import dk.dtu.imm.esculapauml.core.states.SimulationStateObserver;
import dk.dtu.imm.esculapauml.gui.topcased.labelProviders.TransitionLabelProvider;

/**
 * TOPCASED GUI for Esculapa UML choices.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class SimulationObserver implements SimulationStateObserver {

	private Modeler modeler;

	/**
	 * @param modeler
	 */
	public SimulationObserver(Modeler modeler) {
		this.modeler = modeler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.states.SimulationStateObserver#booleanChoice
	 * (int, boolean, java.lang.Object)
	 */
	@Override
	public boolean booleanChoice(int typeOfDecision, boolean defaultValue, Object data) {
		if (DECISION_TERMINATE_SIMULATION == typeOfDecision) {
			return MessageDialog.openQuestion(modeler.getSite().getShell(), "EsculapaUML simulation",
					"Number of events exceeded threshold of " + data.toString() + ". Do you want to stop the simulation?");
		}
		return defaultValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.states.SimulationStateObserver#multipleChoice
	 * (int, int, java.lang.Object)
	 */
	@Override
	public Object multipleChoice(int typeOfDecision, Object defaultValue, Object[] data) {
		if (DECISION_EXTERNAL_TRANSITION_CHOICE == typeOfDecision) {
			ElementListSelectionDialog dialog = new ElementListSelectionDialog(modeler.getSite().getShell(), new TransitionLabelProvider());
			dialog.setTitle("EsculapaUML: External choice");
			dialog.setMessage("Please, choose transition to take in '" + ((Transition) defaultValue).getContainer().getStateMachine().getLabel() + "':");
			dialog.setElements(data);
			dialog.setMultipleSelection(false);
			dialog.setBlockOnOpen(true);
			dialog.open();
			Object result = dialog.getFirstResult();
			if (null != result) {
				return result;
			}
		}
		return defaultValue;
	}

}
