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
import java.util.Iterator;
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
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.topcased.modeler.ModelerPropertyConstants;
import org.topcased.modeler.di.model.Diagram;
import org.topcased.modeler.di.model.DiagramElement;
import org.topcased.modeler.di.model.GraphConnector;
import org.topcased.modeler.di.model.GraphEdge;
import org.topcased.modeler.di.model.GraphElement;
import org.topcased.modeler.di.model.GraphNode;
import org.topcased.modeler.di.model.util.DIUtils;
import org.topcased.modeler.diagrams.model.util.DiagramsUtils;
import org.topcased.modeler.editor.Modeler;
import org.topcased.modeler.uml.sequencediagram.util.SequenceUtils;
import org.topcased.modeler.utils.Utils;

import dk.dtu.imm.esculapauml.core.checkers.AbstractChecker;
import dk.dtu.imm.esculapauml.core.utils.InteractionUtils;
import dk.dtu.imm.esculapauml.gui.topcased.utils.DiagramElementIterable;
import dk.dtu.imm.esculapauml.gui.topcased.utils.DiagramElementIterator;
import dk.dtu.imm.esculapauml.gui.topcased.utils.PosUtils;

/**
 * Extends TOPCASED sequence diagrams by new generated elements.
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
	private String messageColor = "0,128,255", lifelineColor = "128,128,255", executionColor = "192,192,192";
	private boolean changeColors = true;
	private int distanceBetweenMessages = 30;
	private int lifelineWidth = 100;
	private boolean autoResize = true;

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
			for (final Diagram di : diags) {
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
		recalculateElementsToPlot(new BasicEList<EObject>(toAdd).iterator());
		for (Element element : toAdd) {
			if (element instanceof BehaviorExecutionSpecification) {
				createBehaviorExecutionSpecification(di, (BehaviorExecutionSpecification) element);
			}
		}
		recalculateElementsToPlot(new BasicEList<EObject>(toAdd).iterator());
		for (Element element : toAdd) {
			if (element instanceof Message) {
				createMessage(di, (Message) element);
			}
		}

		finalizeDiagram(di);

		if (autoResize) {
			autoResizeDiagram(di);
		}
	}

	/**
	 * @param di
	 */
	private void autoResizeDiagram(Diagram di) {
		// reuse an existing functions
		// Dimension dim = Utils.getDiagramOptimizedDimension(modeler);
		// Set Very big size
		// DIUtils.setProperty(di, ModelerPropertyConstants.PAGE_FORMAT_NAME,
		// "");
		// DIUtils.setProperty(di, ModelerPropertyConstants.PAGE_WIDTH,
		// String.valueOf(dim.width + 100));
		// DIUtils.setProperty(di, ModelerPropertyConstants.PAGE_HEIGHT,
		// String.valueOf(dim.height + 100));
		DIUtils.setProperty(di, ModelerPropertyConstants.PAGE_WIDTH, String.valueOf(10000));
		DIUtils.setProperty(di, ModelerPropertyConstants.PAGE_HEIGHT, String.valueOf(10000));
	}

	/**
	 * Function used to normalize and finalize all the graphics elements of the
	 * diagram.
	 * 
	 * @param di
	 */
	private void finalizeDiagram(Diagram di) {
		DiagramElementIterable iterDiagram = new DiagramElementIterable(di);
		DiagramElementIterator dit = iterDiagram.iterator();
		Point globalLowestPoint = new Point(0, 0);
		while (dit.hasNext()) {
			DiagramElement diElement = dit.next();
			if (dit.getModel() instanceof BehaviorExecutionSpecification) {
				// we normalize the size of BES so it finishes where is the last
				// message
				// or if there is only one message the minimum size is
				// distanceBetweenMessages/2
				GraphNode besNode = (GraphNode) diElement;
				if (besNode.getAnchorage().size() < 2) {
					// only one or no messsage
					besNode.getSize().setHeight(distanceBetweenMessages / 2);
				} else {
					Point lowestPoint = new Point(0, 0);
					for (GraphConnector gc : besNode.getAnchorage()) {
						if (lowestPoint.y < gc.getPosition().y) {
							lowestPoint.setLocation(gc.getPosition());
						}
					}
					besNode.getSize().setHeight(lowestPoint.y);
				}
				globalLowestPoint = Point.max(globalLowestPoint, PosUtils.getAbsolutePosition(besNode).translate(besNode.getSize()));
			}
		}
		// restart iteration
		dit = iterDiagram.shallowIterator();
		while (dit.hasNext()) {
			GraphNode diNode = dit.nextNode();
			if (dit.getModel() instanceof Lifeline) {
				// set all lifelines length to the maximal point + margin
				diNode.getSize().setHeight(globalLowestPoint.y + distanceBetweenMessages);
			}
		}

	}

	/**
	 * Creates new message's graphical representation in diagram.
	 * 
	 * @param di
	 *            Diagram to change.
	 * @param message
	 *            Message to plot.
	 */
	private void createMessage(Diagram di, Message message) {
		GraphEdge edge = (GraphEdge) modeler.getActiveConfiguration().getCreationUtils().createGraphElement((EObject) message, "default");
		if (changeColors) {
			DIUtils.setProperty(edge, ModelerPropertyConstants.FOREGROUND_COLOR, messageColor);
		}

		BehaviorExecutionSpecification sourceSpec = InteractionUtils.getMessageSourceExecutionSpecification(message);
		BehaviorExecutionSpecification targetSpec = InteractionUtils.getMessageTargetExecutionSpecification(message);
		// if(null == targetSpec || null == sourceSpec) {
		// return;
		// }
		GraphElement source = Utils.getGraphElement(di.getSemanticModel().getGraphElement(), sourceSpec, true);
		GraphElement target = Utils.getGraphElement(di.getSemanticModel().getGraphElement(), targetSpec, true);
		GraphConnector srcConnector = SequenceUtils.createGraphConnector(new Point(0, 0), source, edge);
		GraphConnector targetConnector = SequenceUtils.createGraphConnector(new Point(0, 0), target, edge);
		// we need to update the location of points according to previous
		// message
		MessageOccurrenceSpecification sourcePrev = InteractionUtils.getPreviousMessageOccurrence((MessageOccurrenceSpecification) message.getSendEvent());
		MessageOccurrenceSpecification targetPrev = InteractionUtils.getPreviousMessageOccurrence((MessageOccurrenceSpecification) message.getReceiveEvent());

		if (sourcePrev != null) {
			GraphEdge prevMessageEdge = (GraphEdge) Utils.getGraphElement(di.getSemanticModel().getGraphElement(), sourcePrev.getMessage(), true);
			// if we are not finishing an execution
			// shift down the interaction before we add message to diagram
			if (targetSpec.getFinish() != message.getReceiveEvent()) {
				shiftInteractionVerticallyFromY(di, PosUtils.getAbsolutePosition(prevMessageEdge.getAnchor().get(0)).y, distanceBetweenMessages);
			}
			if (prevMessageEdge != null) {
				if (!prevMessageEdge.getAnchor().isEmpty()) {
					// we have two cases. one when both occurrences are on
					// the same bes
					boolean found = false;
					for (GraphConnector gc : prevMessageEdge.getAnchor()) {
						if (gc.getGraphElement() == srcConnector.getGraphElement()) {
							srcConnector.setPosition(Point.max(srcConnector.getPosition(), gc.getPosition().getTranslated(0, distanceBetweenMessages)));
							found = true;
						}
					}
					// and second that they are on separate bes
					if (!found) {
						// here we need to set the correct position of bes in
						// case it was generated and we starting it
						if (wasGenerated(sourceSpec)) {
							if (sourceSpec.getStart() instanceof MessageOccurrenceSpecification) {
								if (((MessageOccurrenceSpecification) sourceSpec.getStart()).getMessage() == message) {
									// get target connector
									GraphConnector toConnector = prevMessageEdge.getAnchor().get(prevMessageEdge.getAnchor().size() - 1);
									Point expected = PosUtils.getAbsolutePosition(toConnector).translate(0, distanceBetweenMessages);
									Point current = PosUtils.getAbsolutePosition(source);
									// calculate a shift
									int verticalShift = expected.y - current.y;
									// and shift bes
									source.getPosition().translate(0, Math.max(verticalShift, 0));

								}
							}
						}
					}
				}

			}
		}

		if (targetPrev != null) {
			GraphEdge prevMessageEdge = (GraphEdge) Utils.getGraphElement(di.getSemanticModel().getGraphElement(), targetPrev.getMessage(), true);
			if (prevMessageEdge != null) {
				if (!prevMessageEdge.getAnchor().isEmpty()) {
					for (GraphConnector gc : prevMessageEdge.getAnchor()) {
						if (gc.getGraphElement() == targetConnector.getGraphElement()) {
							targetConnector.setPosition(Point.max(targetConnector.getPosition(), gc.getPosition().getTranslated(0, distanceBetweenMessages)));
						}
					}
				}

			}
		}

		// make sure generated bes is not forcing message to go "back in time"
		if (wasGenerated(targetSpec)) {
			// if it is a first message of spec
			if (targetSpec.getStart() instanceof MessageOccurrenceSpecification) {
				if (((MessageOccurrenceSpecification) targetSpec.getStart()).getMessage() == message) {
					// update size and location of bes
					Point srcPoint = PosUtils.getAbsolutePosition(srcConnector);
					Point targetPoint = PosUtils.getAbsolutePosition(targetConnector);
					Point deltaPoint = srcPoint.getCopy().translate(targetPoint.getNegated());
					// if target is higher or lower than source
					if (deltaPoint.y != 0) {
						// we reduce a size of spec and shift it
						((GraphNode) targetConnector.getGraphElement()).getSize().expand(0, -deltaPoint.y);
						((GraphNode) targetConnector.getGraphElement()).getPosition().translate(0, deltaPoint.y);
					}
				}
			}
		}

		// make sure for other cases
		Point srcPoint = PosUtils.getAbsolutePosition(srcConnector);
		Point targetPoint = PosUtils.getAbsolutePosition(targetConnector);
		Point deltaPoint = srcPoint.getCopy().translate(targetPoint.getNegated());
		// if target is higher than source
		if (deltaPoint.y > 0) {
			// we change the target point
			targetConnector.getPosition().translate(0, deltaPoint.y);
		}

		di.getContained().add(edge);
		setAsPlotted(message);
	}

	/**
	 * Creates new BehaviorExecutionSpecification's graphical representation in
	 * diagram.
	 * 
	 * This function is used only for BES on existing lifelines.
	 * 
	 * @param di
	 *            Diagram to change.
	 * @param bes
	 *            BehaviorExecutionSpecification to plot.
	 */
	private void createBehaviorExecutionSpecification(Diagram di, BehaviorExecutionSpecification bes) {
		Lifeline lifeline = InteractionUtils.getLifelineOfFragment((InteractionFragment) bes);
		GraphNode lifelineNode = (GraphNode) Utils.getGraphElement(di.getSemanticModel().getGraphElement(), lifeline, true);
		GraphNode besNode = (GraphNode) modeler.getActiveConfiguration().getCreationUtils().createGraphElement((EObject) bes, "default");
		if (changeColors) {
			DIUtils.setProperty(besNode, ModelerPropertyConstants.BACKGROUND_COLOR, executionColor);
		}
		besNode.setPosition(new Point(0, 0));
		besNode.setSize(new Dimension(20, distanceBetweenMessages / 2));
		lifelineNode.getContained().add(besNode);
		setAsPlotted(bes);
	}

	/**
	 * Creates new lifeline's graphical representation in diagram.
	 * 
	 * @param di
	 *            Diagram to change.
	 * @param lifeline
	 *            Lifeline to plot.
	 */
	private void createLifeline(Diagram di, Lifeline lifeline) {
		GraphNode node = (GraphNode) modeler.getActiveConfiguration().getCreationUtils().createGraphElement((EObject) lifeline, "default");
		node.setPosition(new Point(calculateXForNewLifeline(di), 40));
		node.setSize(new Dimension(lifelineWidth, calculateHeightForNewLifeline(di)));
		// CreateGraphNodeCommand com = new
		// CreateGraphNodeCommand((EditDomain)
		// modeler.getAdapter(EditDomain.class), childGraphNode,
		// parentGraphNode, loc,
		// dim, attachment);
		// modeler.getEditingDomain().getCommandStack().execute((Command)
		// com);

		if (changeColors) {
			DIUtils.setProperty(node, ModelerPropertyConstants.BACKGROUND_COLOR, lifelineColor);
		}
		di.getContained().add(node);
		setAsPlotted(lifeline);
		// create bes
		List<GraphNode> besNodes = new ArrayList<GraphNode>();
		for (InteractionFragment fragment : lifeline.getCoveredBys()) {
			if (fragment instanceof BehaviorExecutionSpecification) {
				GraphNode besNode = (GraphNode) modeler.getActiveConfiguration().getCreationUtils().createGraphElement((EObject) fragment, "default");
				if (changeColors) {
					DIUtils.setProperty(besNode, ModelerPropertyConstants.BACKGROUND_COLOR, executionColor);
				}
				besNodes.add(besNode);
				setAsPlotted(fragment);
			}
		}
		// calculate bes sizes
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
	 * Used to filter interaction for a first time.
	 * 
	 */
	private void calculateElementsToPlot() {
		TreeIterator<EObject> contents = interaction.eAllContents();
		recalculateElementsToPlot(contents);
	}

	/**
	 * Finds elements to be added to diagram based on annotations.
	 * "topcased-ploted" is used to avoid duplication of graphical nodes if user
	 * runs a checker subsequently.
	 * 
	 * @return
	 */
	private void recalculateElementsToPlot(Iterator<EObject> contents) {
		toAdd.clear();
		while (contents.hasNext()) {
			EObject object = contents.next();
			if (object instanceof EModelElement) {
				EAnnotation annotation = UML2Util.getEAnnotation((EModelElement) object, AbstractChecker.ESCULAPA_NAMESPACE, false);
				if (null != annotation) {
					if (annotation.getDetails().get("generated").equals("true")) {
						if (null == annotation.getDetails().get("topcased-ploted") || !annotation.getDetails().get("topcased-ploted").equals("true")) {
							// supported elements
							if (object instanceof Lifeline || object instanceof Message || object instanceof BehaviorExecutionSpecification) {
								toAdd.add((Element) object);
							}
						}

					}
				}
			}
		}
	}

	/**
	 * Checks whatever element was generated by EsculapaUML.
	 * 
	 * @param element
	 * @return
	 */
	private boolean wasGenerated(Object object) {
		if (object instanceof EModelElement) {
			EAnnotation annotation = UML2Util.getEAnnotation((EModelElement) object, AbstractChecker.ESCULAPA_NAMESPACE, false);
			if (null != annotation) {
				if (annotation.getDetails().get("generated").equals("true")) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Annotates element as ploted in TOPCASED.
	 * 
	 * @param element
	 *            UML element to annotate.
	 */
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

	/**
	 * Calculates place on X axis for new lifeline based on existing lifelines
	 * in diagram.
	 * 
	 * @param di
	 *            Diagram to check.
	 * @return
	 */
	private int calculateXForNewLifeline(Diagram di) {
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

	/**
	 * Calculates maximum length of existing lifelines elements.
	 * 
	 * @param di
	 *            Diagram to check.
	 * @return
	 */
	private int calculateHeightForNewLifeline(Diagram di) {
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

	/**
	 * Shifts elements of diagram below given height by delta vertically.
	 * 
	 * @param fromY
	 * @param deltaY
	 */
	private void shiftInteractionVerticallyFromY(Diagram di, int fromY, int deltaY) {
		DiagramElementIterable iterDiagram = new DiagramElementIterable(di);
		DiagramElementIterator dit = iterDiagram.iterator();
		while (dit.hasNext()) {
			GraphNode diNode = dit.nextNode();
			if (dit.getModel() instanceof BehaviorExecutionSpecification) {
				// check if current position is in the range shift it
				Point pos = PosUtils.getAbsolutePosition(diNode);
				if (pos.y > fromY) {
					diNode.getPosition().translate(0, deltaY);
				} else {
					// if its not starting below but its size is in the range
					// then expand it
					if (pos.getTranslated(diNode.getSize()).y > fromY) {
						diNode.getSize().expand(0, deltaY);
					}
					for (GraphConnector gc : diNode.getAnchorage()) {
						Point gcPos = PosUtils.getAbsolutePosition(gc);
						if (gcPos.y > fromY) {
							// move a connector down
							gc.getPosition().translate(0, deltaY);
						}
					}
				}
			}
		}
	}

	/**
	 * Sets the color of generated messages.
	 * 
	 * @param messageColor
	 *            the color in string format "R,G,B".
	 */
	public void setMessageColor(String messageColor) {
		this.messageColor = messageColor;
	}

	/**
	 * Stest the color of generated lifelines.
	 * 
	 * @param lifelineColor
	 *            the color in string format "R,G,B".
	 */
	public void setLifelineColor(String lifelineColor) {
		this.lifelineColor = lifelineColor;
	}

	/**
	 * Sets the color of generated execution specifications.
	 * 
	 * @param executionColor
	 *            the color in string format "R,G,B".
	 */
	public void setExecutionColor(String executionColor) {
		this.executionColor = executionColor;
	}

	/**
	 * @param changeColors
	 *            Set if extender should change colors of generated elements;
	 */
	public void setChangeColors(boolean changeColors) {
		this.changeColors = changeColors;
	}

	/**
	 * @param distanceBetweenMessages
	 *            the distanceBetweenMessages to set
	 */
	public void setDistanceBetweenMessages(int distanceBetweenMessages) {
		this.distanceBetweenMessages = distanceBetweenMessages;
	}

	/**
	 * @param autoResize
	 *            the autoResize to set
	 */
	public void setAutoResize(boolean autoResize) {
		this.autoResize = autoResize;
	}

	/**
	 * @param lifelineWidth
	 *            the lifelineWidth to set
	 */
	public void setLifelineWidth(int lifelineWidth) {
		this.lifelineWidth = lifelineWidth;
	}

}
