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

import org.eclipse.emf.common.util.EList;
import org.topcased.modeler.di.model.Diagram;
import org.topcased.modeler.di.model.DiagramElement;
import org.topcased.modeler.di.model.GraphElement;

/**
 * @author Piotr J. Puczynski
 *
 */
public class DiagramElementIterable implements Iterable<DiagramElement> {

	private EList<DiagramElement> elements;
	/**
	 * 
	 */
	public DiagramElementIterable(Diagram di) {
		super();
		elements = di.getContained();
	}
	
	public DiagramElementIterable(GraphElement element) {
		super();
		elements = element.getContained();
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public DiagramElementIterator iterator() {
		return new DiagramElementIterator(elements, true);
	}
	
	public DiagramElementIterator shallowIterator() {
		return new DiagramElementIterator(elements, false);
	}

}
