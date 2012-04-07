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
package dk.dtu.imm.esculapauml.core.collections;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.uml2.uml.ValueSpecification;

/**
 * List of value specification used during model execution to pass arguments.
 * This is to fulfill missing part of UML specification that does not have any
 * collection value type.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class ValuesList extends AbstractList<ValueSpecification> implements ValuesCollection {

	private List<ValueSpecification> list = new ArrayList<ValueSpecification>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.AbstractList#get(int)
	 */
	@Override
	public ValueSpecification get(int index) {
		return list.get(index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.AbstractCollection#size()
	 */
	@Override
	public int size() {
		return list.size();
	}

	@Override
	public Iterator<ValueSpecification> iterator() {
		return list.iterator();
	}

}
