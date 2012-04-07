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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

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

	/* (non-Javadoc)
	 * @see java.util.AbstractList#add(int, java.lang.Object)
	 */
	@Override
	public void add(int index, ValueSpecification element) {
		list.add(index, element);
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractList#add(java.lang.Object)
	 */
	@Override
	public boolean add(ValueSpecification e) {
		return list.add(e);
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractList#addAll(int, java.util.Collection)
	 */
	@Override
	public boolean addAll(int index, Collection<? extends ValueSpecification> c) {
		return list.addAll(index, c);
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractList#clear()
	 */
	@Override
	public void clear() {
		list.clear();
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractList#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		return list.equals(o);
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractList#hashCode()
	 */
	@Override
	public int hashCode() {
		return list.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractList#indexOf(java.lang.Object)
	 */
	@Override
	public int indexOf(Object o) {
		return list.indexOf(o);
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractList#iterator()
	 */
	@Override
	public Iterator<ValueSpecification> iterator() {
		return list.iterator();
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractList#lastIndexOf(java.lang.Object)
	 */
	@Override
	public int lastIndexOf(Object o) {
		return list.lastIndexOf(o);
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractList#listIterator()
	 */
	@Override
	public ListIterator<ValueSpecification> listIterator() {
		return list.listIterator();
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractList#listIterator(int)
	 */
	@Override
	public ListIterator<ValueSpecification> listIterator(int index) {
		return list.listIterator(index);
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractList#remove(int)
	 */
	@Override
	public ValueSpecification remove(int index) {
		return list.remove(index);
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractList#set(int, java.lang.Object)
	 */
	@Override
	public ValueSpecification set(int index, ValueSpecification element) {
		return list.set(index, element);
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractList#subList(int, int)
	 */
	@Override
	public List<ValueSpecification> subList(int fromIndex, int toIndex) {
		return list.subList(fromIndex, toIndex);
	}
	
	

}
