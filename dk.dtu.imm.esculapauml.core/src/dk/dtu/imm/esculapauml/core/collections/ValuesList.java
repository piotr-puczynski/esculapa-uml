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

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.InstanceValue;
import org.eclipse.uml2.uml.LiteralInteger;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.ValueSpecification;

import dk.dtu.imm.esculapauml.core.checkers.BehaviorChecker;
import dk.dtu.imm.esculapauml.core.checkers.Checker;
import dk.dtu.imm.esculapauml.core.utils.UMLTypesUtil;

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
	private Type type = null;

	/**
	 * 
	 */
	public ValuesList() {
		super();
	}

	/**
	 * Generates list from EList of values.
	 * 
	 * @param values
	 */
	public ValuesList(EList<ValueSpecification> values) {
		for (ValueSpecification val : values) {
			add(val);
			type = val.getType();
		}
	}

	/**
	 * Generates list from one value.
	 * 
	 * @param values
	 */
	public ValuesList(ValueSpecification value) {
		add(value);
		type = value.getType();
	}

	/**
	 * Create list from values of given name filtered from given collection.
	 * 
	 * @param string
	 * @param arguments
	 */
	public ValuesList(String name, EList<ValueSpecification> arguments) {
		for (ValueSpecification arg : arguments) {
			if (arg.getName().equals(name)) {
				add(arg);
			}
		}
	}

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.AbstractList#add(int, java.lang.Object)
	 */
	@Override
	public void add(int index, ValueSpecification element) {
		type = element.getType();
		list.add(index, element);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.AbstractList#add(java.lang.Object)
	 */
	@Override
	public boolean add(ValueSpecification e) {
		type = e.getType();
		return list.add(e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.AbstractCollection#addAll(java.util.Collection)
	 */
	@Override
	public boolean addAll(Collection<? extends ValueSpecification> c) {
		if (!c.isEmpty()) {
			type = ((ValueSpecification) c.toArray()[0]).getType();
		}
		return list.addAll(c);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.AbstractList#clear()
	 */
	@Override
	public void clear() {
		list.clear();
		type = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.AbstractList#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		return list.equals(o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.AbstractList#hashCode()
	 */
	@Override
	public int hashCode() {
		return list.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.AbstractList#indexOf(java.lang.Object)
	 */
	@Override
	public int indexOf(Object o) {
		return list.indexOf(o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.AbstractList#iterator()
	 */
	@Override
	public Iterator<ValueSpecification> iterator() {
		return list.iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.AbstractList#lastIndexOf(java.lang.Object)
	 */
	@Override
	public int lastIndexOf(Object o) {
		return list.lastIndexOf(o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.AbstractList#listIterator()
	 */
	@Override
	public ListIterator<ValueSpecification> listIterator() {
		return list.listIterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.AbstractList#listIterator(int)
	 */
	@Override
	public ListIterator<ValueSpecification> listIterator(int index) {
		return list.listIterator(index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.AbstractList#remove(int)
	 */
	@Override
	public ValueSpecification remove(int index) {
		return list.remove(index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.AbstractList#set(int, java.lang.Object)
	 */
	@Override
	public ValueSpecification set(int index, ValueSpecification element) {
		return list.set(index, element);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.AbstractList#subList(int, int)
	 */
	@Override
	public List<ValueSpecification> subList(int fromIndex, int toIndex) {
		return list.subList(fromIndex, toIndex);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dk.dtu.imm.esculapauml.core.collections.ValuesCollection#getType()
	 */
	@Override
	public Type getType() {
		if (list.isEmpty()) {
			return null;
		} else {
			return type;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.collections.ValuesCollection#setName(java
	 * .lang.String)
	 */
	@Override
	public void setName(String name) {
		for (ValueSpecification val : list) {
			val.setName(name);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.collections.ValuesCollection#addFromOCL(java
	 * .lang.Object, dk.dtu.imm.esculapauml.core.checkers.BehaviorChecker,
	 * org.eclipse.uml2.uml.Element)
	 */
	@Override
	public void addFromOCL(Object oclValue, BehaviorChecker checker, Element errorContext) throws OCLConversionException {
		if (oclValue instanceof ValueSpecification) {
			add((ValueSpecification) oclValue);
			return;
		}
		if (oclValue instanceof Collection) {
			@SuppressWarnings("rawtypes")
			Collection collection = (Collection) oclValue;
			for (Object obj : collection) {
				addFromOCL(obj, checker, errorContext);
			}
			return;
		} else {
			if (UMLTypesUtil.canBeConverted(oclValue)) {
				add(UMLTypesUtil.getObjectValue(oclValue, checker, errorContext));
				return;
			}
			if (null == oclValue) {
				add(UMLTypesUtil.getNullValue());
				return;
			}
		}
		throw new OCLConversionException(oclValue);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.collections.ValuesCollection#isSingleValued()
	 */
	@Override
	public boolean isSingleValued(Checker checker) {
		if (list.size() == 1) {
			return true;
		} else {
			checker.addProblem(Diagnostic.ERROR, "The value was expected to be single valued but it is: " + toString());
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[" + getValuesNames() + "]";
	}

	/**
	 * @return
	 */
	private String getValuesNames() {
		String result = "";
		for (ValueSpecification vs : list) {
			if (vs instanceof InstanceValue) {
				result += "instance: ";
				if (null != ((InstanceValue) vs).getInstance()) {
					result += ((InstanceValue) vs).getInstance().getLabel();
				}
				result += ", ";
			} else {
				result += vs.stringValue() + ", ";
			}
		}
		if (!result.isEmpty()) {
			result = result.substring(0, result.length() - 2);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.collections.ValuesCollection#select(dk.dtu
	 * .imm.esculapauml.core.collections.ValuesCollection)
	 */
	@Override
	public boolean select(ValuesCollection selector) {
		int index = ((LiteralInteger) selector.get(0)).getValue();
		return select(index);
	}

	/**
	 * @param index
	 * @return
	 */
	private boolean select(int index) {
		if (index < 0 || index >= list.size()) {
			return false;
		}
		ValueSpecification vs = list.get(0);
		list.clear();
		add(vs);
		return true;
	}

}
