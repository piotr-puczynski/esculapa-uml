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
package dk.dtu.imm.esculapauml.gui.topcased.extenders;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.uml2.common.util.UML2Util;
import org.eclipse.uml2.uml.BehaviorExecutionSpecification;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.topcased.modeler.di.model.Diagram;
import org.topcased.modeler.di.model.GraphConnector;
import org.topcased.modeler.di.model.GraphEdge;
import org.topcased.modeler.di.model.GraphElement;
import org.topcased.modeler.di.model.GraphNode;
import org.topcased.modeler.diagrams.model.util.DiagramsUtils;
import org.topcased.modeler.editor.Modeler;
import org.topcased.modeler.uml.sequencediagram.util.SequenceUtils;
import org.topcased.modeler.utils.Utils;
//import org.topcased.modeler.exceptions.BoundsFormatException;
//import org.topcased.modeler.tools.Importer;
//import org.topcased.modeler.utils.Utils;
//import org.topcased.modeler.commands.CreateGraphNodeCommand;
//import org.eclipse.gef.EditPart;
//import org.eclipse.gef.GraphicalEditPart;

import dk.dtu.imm.esculapauml.core.checkers.AbstractChecker;
import dk.dtu.imm.esculapauml.core.utils.InteractionUtils;
import dk.dtu.imm.esculapauml.gui.topcased.utils.DiagramElementIterable;
import dk.dtu.imm.esculapauml.gui.topcased.utils.DiagramElementIterator;

/**
 * Extends sequence diagrams by new generated elements.
 * 
 * @author Piotr J. Puczynski
 * 
 */
@SuppressWarnings("restriction")
public class InteractionExtender implements ExtenderInterface {
	private static final String DIAGRAM_ID = "org.topcased.modeler.uml.sequencediagram";
	private Modeler modeler;
	private Interaction interaction;
	private EList<Element> toAdd = new BasicEList<Element>();

	/**
	 * @param modeler
	 * @param interaction
	 */
	public InteractionExtender(Modeler modeler, Interaction interaction) {
		super();
		this.modeler = modeler;
		this.interaction = interaction;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.gui.topcased.extenders.ExtenderInterface#extend()
	 */
	@Override
	public void extend() {
		calculateElementsToPlot();
		if (!toAdd.isEmpty()) {
			List<Diagram> diags = DiagramsUtils.findAllExistingDiagram(modeler.getDiagrams(), interaction);
			for (Diagram di : diags) {
				if (di.getSemanticModel().getPresentation().equals(DIAGRAM_ID)) {
					modeler.setActiveDiagram(di);
					extendSequenceDiagram(di);
				}
			}
		}
	}

	/**
	 * Extends sequence diagram.
	 * 
	 * @param diagram
	 */
	private void extendSequenceDiagram(Diagram di) {
		for (Element element : toAdd) {
			if (element instanceof Lifeline) {
				createLifeline(di, (Lifeline) element);
			}
		}
		for (Element element : toAdd) {
			if (element instanceof Message) {
				createMessage(di, (Message) element);
			}
		}
	}

	/**
	 * @param di
	 * @param element
	 */
	private void createMessage(Diagram di, Message message) {
		GraphEdge edge = (GraphEdge) modeler.getActiveConfiguration().getCreationUtils().createGraphElement((EObject) message, "default");
		GraphElement source = Utils.getGraphElement(di.getSemanticModel().getGraphElement(), InteractionUtils.getMessageSourceExecutionSpecification(message), true);
		GraphElement target = Utils.getGraphElement(di.getSemanticModel().getGraphElement(), InteractionUtils.getMessageTargetExecutionSpecification(message), true);
		GraphConnector srcConnector = SequenceUtils.createGraphConnector(new Point(10,10), source, edge);
		GraphConnector targetConnector = SequenceUtils.createGraphConnector(new Point(30,30), target, edge);
		
		di.getContained().add(edge);
	}

	private void createLifeline(Diagram di, Lifeline lifeline) {

		GraphNode node = (GraphNode) modeler.getActiveConfiguration().getCreationUtils().createGraphElement((EObject) lifeline, "default");
		node.setPosition(new Point(calculateXForNewLifeline(di), 40));
		node.setSize(new Dimension(50, calculateHeightForNewLifeline(di)));
		// CreateGraphNodeCommand com = new
		// CreateGraphNodeCommand((EditDomain)
		// modeler.getAdapter(EditDomain.class), childGraphNode,
		// parentGraphNode, loc,
		// dim, attachment);
		// modeler.getEditingDomain().getCommandStack().execute((Command)
		// com);
		di.getContained().add(node);
		setAsPlotted(lifeline);
		// create bes
		List<GraphNode> besNodes = new ArrayList<GraphNode>();
		for (InteractionFragment fragment : lifeline.getCoveredBys()) {
			if (fragment instanceof BehaviorExecutionSpecification) {
				besNodes.add((GraphNode) modeler.getActiveConfiguration().getCreationUtils().createGraphElement((EObject) fragment, "default"));
				setAsPlotted(fragment);
			}
		}
		//calculate bes sizes
		int currentHeight = 15;
		for (GraphNode besNode : besNodes) {
			besNode.setPosition(new Point(0, currentHeight));
			besNode.setSize(new Dimension(20, (node.getSize().height - 45) / besNodes.size()));
			currentHeight += 15 + besNode.getSize().height;
		}
		node.getContained().addAll(besNodes);
		
		
		// Importer importer = new Importer(modeler, lifeline);
		// importer.setDisplayDialogs(false);
		// importer.setTargetEditPart((GraphicalEditPart)
		// getEditPartForObjectInDiagram(di, interaction));
		// importer.setLocation(new Point(calculateXForNewLifeline(di), 0));
		// try {
		// importer.run(new NullProgressMonitor());
		// // now the element is created we can resize it
		// GraphElement lifelineElement =
		// Utils.getGraphElement(di.getSemanticModel().getGraphElement(),
		// lifeline);
		// if (lifelineElement instanceof GraphNode) {
		// ((GraphNode) lifelineElement).setSize(new Dimension(((GraphNode)
		// lifelineElement).getSize().width,
		// calculateHeightForNewLifeline(di)));
		// }
		// } catch (BoundsFormatException e) {
		// throw e;
		// } catch (IllegalArgumentException e) {
		// throw e;
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }

	}

	/**
	 * Finds elements to be added to diagram based on annotations.
	 * "topcased-ploted" is used to avoid duplication of graphical nodes if user
	 * runs a checker subsequently.
	 * 
	 * @return
	 */
	private void calculateElementsToPlot() {
		toAdd.clear();
		TreeIterator<EObject> contents = interaction.eAllContents();
		while (contents.hasNext()) {
			EObject object = contents.next();
			if (object instanceof EModelElement) {
				EAnnotation annotation = UML2Util.getEAnnotation((EModelElement) object, AbstractChecker.ESCULAPA_NAMESPACE, false);
				if (null != annotation) {
					if (annotation.getDetails().get("generated").equals("true")) {
						if (null == annotation.getDetails().get("topcased-ploted") || !annotation.getDetails().get("topcased-ploted").equals("true")) {
							toAdd.add((Element) object);
						}

					}
				}
			}
		}
	}

	private void setAsPlotted(Element element) {
		EAnnotation annotation = UML2Util.getEAnnotation((EModelElement) element, AbstractChecker.ESCULAPA_NAMESPACE, true);
		annotation.getDetails().put("topcased-ploted", "true");
	}

	// EditPart getEditPartForObjectInDiagram(Diagram di, Object object) {
	// EList<DiagramElement> elements = new BasicEList<DiagramElement>();
	// // collect diagram
	// elements.add(di.getSemanticModel().getGraphElement());
	// // collect all nodes in diagram (first level only)
	// elements.addAll(di.getContained());
	// for (DiagramElement element : elements) {
	// if (element instanceof GraphNode) {
	// GraphNode node = (GraphNode) element;
	// if (node.getSemanticModel() instanceof EMFSemanticModelBridgeImpl) {
	// if (((EMFSemanticModelBridgeImpl) node.getSemanticModel()).getElement()
	// == object) {
	// return (EditPart)
	// modeler.getGraphicalViewer().getEditPartRegistry().get(element);
	// }
	// }
	//
	// }
	// }
	// return null;
	// }

	int calculateXForNewLifeline(Diagram di) {
		int result = 0;
		DiagramElementIterable iterDiagram = new DiagramElementIterable(di);
		DiagramElementIterator dit = iterDiagram.shallowIterator();
		while (dit.hasNext()) {
			GraphNode diNode = dit.nextNode();
			if (dit.getModel() instanceof Lifeline) {
				result = Math.max(result, diNode.getPosition().x + diNode.getSize().width + 30);
			}
		}
		return result;
	}

	int calculateHeightForNewLifeline(Diagram di) {
		int result = 30;
		DiagramElementIterable iterDiagram = new DiagramElementIterable(di);
		DiagramElementIterator dit = iterDiagram.shallowIterator();
		while (dit.hasNext()) {
			GraphNode diNode = dit.nextNode();
			if (dit.getModel() instanceof Lifeline) {
				result = Math.max(result, diNode.getSize().height);
			}
		}
		return result;
	}

}
