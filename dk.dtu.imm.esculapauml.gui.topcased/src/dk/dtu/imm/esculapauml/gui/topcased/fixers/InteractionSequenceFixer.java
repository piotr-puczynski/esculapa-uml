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
package dk.dtu.imm.esculapauml.gui.topcased.fixers;

import java.util.List;

import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.Lifeline;
import org.topcased.modeler.di.model.Diagram;
import org.topcased.modeler.di.model.GraphNode;
import org.topcased.modeler.diagrams.model.util.DiagramsUtils;
import org.topcased.modeler.editor.Modeler;

import dk.dtu.imm.esculapauml.gui.topcased.utils.DiagramElementIterable;
import dk.dtu.imm.esculapauml.gui.topcased.utils.DiagramElementIterator;

/**
 * The class fixing the sequences on lifelines in UML model that can be wrongly
 * saved by Topcased. Class avoids risk of getting bug #4014 in the model by
 * updating model:
 * http://gforge.enseeiht.fr/tracker/?func=detail&atid=109&aid=4014&group_id=52
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class InteractionSequenceFixer {
	private Modeler modeler;
	private Interaction interaction;

	public InteractionSequenceFixer(Interaction interaction, Modeler modeler) {
		this.modeler = modeler;
		this.interaction = interaction;
	}

	public void fix() {
		List<Diagram> diags = DiagramsUtils.findAllExistingDiagram(modeler.getDiagrams(), interaction);
		// we fix only one diagram
		if (!diags.isEmpty()) {
			Diagram di = diags.get(0);
			// we need to run fix for all lifelines in the diagram
			DiagramElementIterable iterDiagram = new DiagramElementIterable(di);
			DiagramElementIterator dit = iterDiagram.shallowIterator();
			while (dit.hasNext()) {
				GraphNode diNode = dit.nextNode();
				if(dit.getModel() instanceof Lifeline) {
					fixLifeline(diNode, (Lifeline) dit.getModel());
				}
			}
		}
	}
	
	private void fixLifeline(GraphNode lifelineNode, Lifeline lifeline) {
		DiagramElementIterable iterDiagram = new DiagramElementIterable(lifelineNode);
		DiagramElementIterator dit = iterDiagram.iterator();
		while (dit.hasNext()) {
			GraphNode diNode = dit.nextNode();
		}
	}

}
