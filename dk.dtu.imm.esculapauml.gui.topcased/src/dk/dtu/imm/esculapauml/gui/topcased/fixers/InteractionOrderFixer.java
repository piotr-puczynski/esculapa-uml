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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.uml2.uml.BehaviorExecutionSpecification;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageEnd;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.topcased.modeler.di.model.Diagram;
import org.topcased.modeler.di.model.GraphConnector;
import org.topcased.modeler.di.model.GraphEdge;
import org.topcased.modeler.di.model.GraphNode;
import org.topcased.modeler.diagrams.model.util.DiagramsUtils;
import org.topcased.modeler.editor.Modeler;
import org.topcased.modeler.utils.Utils;

import dk.dtu.imm.esculapauml.core.utils.InteractionUtils;
import dk.dtu.imm.esculapauml.gui.topcased.utils.DiagramElementIterable;
import dk.dtu.imm.esculapauml.gui.topcased.utils.DiagramElementIterator;
import dk.dtu.imm.esculapauml.gui.topcased.utils.PosUtils;

/**
 * Fixer is an object to fix order of messages occurrences on the lifeline in
 * the model based on graphical representation. It is a hack to avoid bug item
 * #4014:
 * http://gforge.enseeiht.fr/tracker/?func=detail&atid=109&aid=4014&group_id=52
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class InteractionOrderFixer {

	private Modeler modeler;
	private Interaction interaction;
	private boolean hadFixedErrors = false;

	public InteractionOrderFixer(Modeler modeler, Interaction interaction) {
		super();
		this.modeler = modeler;
		this.interaction = interaction;
	}

	/**
	 * Fixes the model.
	 */
	public void fix() {
		List<Diagram> diags = DiagramsUtils.findAllExistingDiagram(modeler.getDiagrams(), interaction);
		// fix only first diagram
		if (!diags.isEmpty()) {
			fixBasedOnDiagram(diags.get(0));
		}
	}

	/**
	 * Fixes interaction based on given diagram.
	 * 
	 * @param di
	 */
	private void fixBasedOnDiagram(Diagram di) {
		// we go through all the lifelines that are represented in the diagram
		for (Lifeline lifeline : interaction.getLifelines()) {
			GraphNode lifelineNode = (GraphNode) Utils.getGraphElement(di.getSemanticModel().getGraphElement(), lifeline, true);
			if (null != lifelineNode) {
				fixLifeline(di, lifeline, lifelineNode);
			}
		}
	}

	/**
	 * Fixes one Lifeline.
	 * 
	 * @param di
	 * @param lifeline
	 * @param lifelineNode
	 */
	private void fixLifeline(Diagram di, Lifeline lifeline, GraphNode lifelineNode) {
		// simulation of multimap
		SortedMap<Integer, EList<InteractionFragment>> lifelineMap = new TreeMap<Integer, EList<InteractionFragment>>();
		DiagramElementIterable iterLifeline = new DiagramElementIterable(lifelineNode);
		DiagramElementIterator dit = iterLifeline.iterator();
		while (dit.hasNext()) {
			GraphNode besNode = dit.nextNode();
			if (dit.getModel() instanceof BehaviorExecutionSpecification) {
				for (GraphConnector gc : besNode.getAnchorage()) {
					if (gc.getGraphEdge().size() > 0) {
						GraphEdge messageEdge = gc.getGraphEdge().get(0);
						EObject object = Utils.getElement(messageEdge);
						if (object instanceof Message) {
							Message message = (Message) object;
							if (Utils.getSource(messageEdge) == besNode) {
								// this is send occurrence
								MessageEnd messageEnd = message.getSendEvent();
								if (messageEnd instanceof MessageOccurrenceSpecification) {
									Point pos = PosUtils.getAbsolutePosition(gc);
									EList<InteractionFragment> l = lifelineMap.get(pos.y);
									if (null == l) {
										lifelineMap.put(pos.y, l = new BasicEList<InteractionFragment>());
									}
									l.add((InteractionFragment) messageEnd);
								}
							} else {
								if (Utils.getTarget(messageEdge) == besNode) {
									// this is receive occurrence
									MessageEnd messageEnd = message.getReceiveEvent();
									if (messageEnd instanceof MessageOccurrenceSpecification) {
										Point pos = PosUtils.getAbsolutePosition(gc);
										EList<InteractionFragment> l = lifelineMap.get(pos.y);
										if (null == l) {
											lifelineMap.put(pos.y, l = new BasicEList<InteractionFragment>());
										}
										l.add((InteractionFragment) messageEnd);
									}
								}
							}
						}
					}
				}
			}
		}
		// end while
		// consolidate a map
		EList<InteractionFragment> realOrder = new BasicEList<InteractionFragment>();
		for (EList<InteractionFragment> l : lifelineMap.values()) {
			realOrder.addAll(l);
		}

		// we are only interested in MessageOccurrences that are really in the
		// model
		List<InteractionFragment> modelOrder = InteractionUtils.filterSpecifications(lifeline, MessageOccurrenceSpecification.class);
		List<Integer> lifelinePositions = new ArrayList<Integer>();
		// we need positions of detected fragments so that we can later add only
		// in this positions (if there is something more in between them)
		for (Iterator<InteractionFragment> it = realOrder.iterator(); it.hasNext();) {
			InteractionFragment realFrag = it.next();
			if (modelOrder.contains(realFrag)) {
				lifelinePositions.add(lifeline.getCoveredBys().indexOf(realFrag));
			} else {
				it.remove();
			}
		}
		Collections.sort(lifelinePositions);
		// fill empty gaps in case the orders are different
		if (!realOrder.equals(modelOrder)) {
			hadFixedErrors = true;
			lifeline.getCoveredBys().removeAll(realOrder);
			for (int i = 0; i < realOrder.size(); ++i) {
				lifeline.getCoveredBys().add(lifelinePositions.get(i), realOrder.get(i));
			}
		}
	}

	/**
	 * @return the hadFixedErrors
	 */
	public boolean hadFixedErrors() {
		return hadFixedErrors;
	}


}
