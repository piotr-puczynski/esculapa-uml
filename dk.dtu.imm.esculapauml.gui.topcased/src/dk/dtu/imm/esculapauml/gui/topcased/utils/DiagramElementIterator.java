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
package dk.dtu.imm.esculapauml.gui.topcased.utils;

import java.util.Iterator;
import java.util.Stack;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.topcased.modeler.di.model.DiagramElement;
import org.topcased.modeler.di.model.GraphEdge;
import org.topcased.modeler.di.model.GraphNode;
import org.topcased.modeler.di.model.internal.impl.EMFSemanticModelBridgeImpl;
import org.topcased.modeler.di.model.internal.impl.GraphElementImpl;

/**
 * Enhanced tree iterator to handle diagram nodes.
 * 
 * @author Piotr J. Puczynski
 * 
 */
@SuppressWarnings("restriction")
public class DiagramElementIterator implements TreeIterator<DiagramElement> {

	private DiagramElement current = null;
	private Stack<Iterator<DiagramElement>> stack = new Stack<Iterator<DiagramElement>>();
	private boolean isDeep;

	/**
	 * @param elements
	 */
	public DiagramElementIterator(EList<DiagramElement> elements, boolean isDeep) {
		this.isDeep = isDeep;
		if (!elements.isEmpty()) {
			stack.push(elements.iterator());
		}
	}

	/**
	 * Gets the model element associated to the current element.
	 * 
	 * @return model element.
	 */
	public EObject getModel() {
		if (current instanceof GraphElementImpl) {
			if (((GraphElementImpl) current).getSemanticModel() instanceof EMFSemanticModelBridgeImpl) {
				return ((EMFSemanticModelBridgeImpl) ((GraphElementImpl) current).getSemanticModel()).getElement();
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		return !stack.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#next()
	 */
	@Override
	public DiagramElement next() {
		current = stack.peek().next();
		// if iterator finished, remove it from stack
		if (!stack.peek().hasNext()) {
			stack.pop();
		}
		if (isDeep) {
			// if node is compound, add new iterator
			if (current instanceof GraphElementImpl) {
				if (!((GraphElementImpl) current).getContained().isEmpty()) {
					stack.push(((GraphElementImpl) current).getContained().iterator());
				}
			}
		}

		return current;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove() {
		// not implemented
		assert false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.emf.common.util.TreeIterator#prune()
	 */
	@Override
	public void prune() {
		if (!stack.isEmpty()) {
			stack.pop();
		}
	}

	/**
	 * @return
	 */
	public GraphNode nextNode() {
		next();
		if (current instanceof GraphNode) {
			return (GraphNode) current;
		}
		return null;
	}
	
	/**
	 * @return
	 */
	public GraphEdge nextEdge() {
		next();
		if (current instanceof GraphEdge) {
			return (GraphEdge) current;
		}
		return null;
	}

}
