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

import org.eclipse.draw2d.geometry.Point;
import org.topcased.modeler.di.model.Diagram;
import org.topcased.modeler.di.model.GraphConnector;
import org.topcased.modeler.di.model.GraphElement;

/**
 * Functions to handle position calculation on diagrams.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class PosUtils {
	/**
	 * Translates the position of connector down to diagram coordinates.
	 * 
	 * @param srcConnector
	 * @return
	 */
	public static Point getAbsolutePosition(GraphConnector connector) {
		return getAbsolutePosition(connector.getGraphElement(), connector.getPosition());
	}

	/**
	 * Translates the position of element down to diagram coordinates.
	 * 
	 * @param element
	 * @return
	 */
	public static Point getAbsolutePosition(GraphElement element) {
		return getAbsolutePosition(element, new Point(0, 0));
	}

	/**
	 * Translates the position of point in context element down to diagram
	 * coordinates.
	 * 
	 * @param context
	 * @param position
	 * @return
	 */
	public static Point getAbsolutePosition(GraphElement context, Point position) {
		Point pos = position.getCopy();
		GraphElement graph = context;
		do {
			pos.translate(graph.getPosition());
			graph = graph.getContainer();
		} while (!(graph instanceof Diagram));
		return pos;
	}

}
