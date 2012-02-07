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

import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.uml2.common.util.UML2Util;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.Lifeline;
import org.topcased.modeler.commands.CreateGraphNodeCommand;
import org.topcased.modeler.di.model.Diagram;
import org.topcased.modeler.di.model.DiagramElement;
import org.topcased.modeler.di.model.GraphElement;
import org.topcased.modeler.di.model.GraphNode;
import org.topcased.modeler.di.model.internal.impl.EMFSemanticModelBridgeImpl;
import org.topcased.modeler.diagrams.model.util.DiagramsUtils;
import org.topcased.modeler.editor.Modeler;
import org.topcased.modeler.exceptions.BoundsFormatException;
import org.topcased.modeler.tools.Importer;
import org.topcased.modeler.utils.Utils;

import dk.dtu.imm.esculapauml.core.checkers.AbstractChecker;

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
		calculateExtensionElements();
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
	}

	private void createLifeline(Diagram di, Lifeline lifeline) {

		GraphElement node = modeler.getActiveConfiguration().getCreationUtils().createGraphElement((EObject) lifeline, "default");
		if (node instanceof GraphNode) {
			node.setPosition(new Point(calculateXForNewLifeline(di), 40));
			((GraphNode) node).setSize(new Dimension(50, calculateHeightForNewLifeline(di)));
			// CreateGraphNodeCommand com = new
			// CreateGraphNodeCommand((EditDomain)
			// modeler.getAdapter(EditDomain.class), childGraphNode,
			// parentGraphNode, loc,
			// dim, attachment);
			// modeler.getEditingDomain().getCommandStack().execute((Command)
			// com);
			di.getContained().add(node);
		}

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
	 * 
	 * @return
	 */
	private void calculateExtensionElements() {
		toAdd.clear();
		TreeIterator<EObject> contents = interaction.eAllContents();
		while (contents.hasNext()) {
			EObject object = contents.next();
			if (object instanceof EModelElement) {
				EAnnotation annotation = UML2Util.getEAnnotation((EModelElement) object, AbstractChecker.ESCULAPA_NAMESPACE, false);
				if (null != annotation) {
					if (annotation.getDetails().get("generated").equals("true")) {
						toAdd.add((Element) object);
					}
				}
			}
		}
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
		EList<DiagramElement> elements = di.getContained();
		for (DiagramElement element : elements) {
			if (element instanceof GraphNode) {
				GraphNode node = (GraphNode) element;
				if (node.getSemanticModel() instanceof EMFSemanticModelBridgeImpl) {
					if (((EMFSemanticModelBridgeImpl) node.getSemanticModel()).getElement() instanceof Lifeline) {
						result = Math.max(result, node.getPosition().x + node.getSize().width + 30);
					}
				}
			}
		}

		return result;
	}

	int calculateHeightForNewLifeline(Diagram di) {
		int result = 30;
		EList<DiagramElement> elements = di.getContained();
		for (DiagramElement element : elements) {
			if (element instanceof GraphNode) {
				GraphNode node = (GraphNode) element;
				if (node.getSemanticModel() instanceof EMFSemanticModelBridgeImpl) {
					if (((EMFSemanticModelBridgeImpl) node.getSemanticModel()).getElement() instanceof Lifeline) {
						result = Math.max(result, node.getSize().height);
					}
				}
			}
		}

		return result;
	}

}
