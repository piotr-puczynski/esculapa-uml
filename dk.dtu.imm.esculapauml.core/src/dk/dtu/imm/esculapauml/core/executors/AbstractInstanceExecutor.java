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
package dk.dtu.imm.esculapauml.core.executors;

import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static org.hamcrest.Matchers.equalTo;

import java.util.List;

import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.InstanceSpecification;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Slot;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.ValueSpecification;

import dk.dtu.imm.esculapauml.core.checkers.Checker;

/**
 * Executes instances.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public abstract class AbstractInstanceExecutor extends AbstractExecutor implements InstanceExecutor {

	protected InstanceSpecification instanceSpecification;
	protected String instanceName;

	/**
	 * @param checker
	 */
	public AbstractInstanceExecutor(Checker checker) {
		super(checker);
		instanceSpecification = UMLFactory.eINSTANCE.createInstanceSpecification();
	}

	/**
	 * @param checker
	 */
	public AbstractInstanceExecutor(Checker checker, InstanceSpecification instanceSpecification) {
		super(checker);
		this.instanceSpecification = instanceSpecification;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dk.dtu.imm.esculapauml.core.executors.InstanceExecutor#
	 * getInstanceSpecification()
	 */
	public InstanceSpecification getInstanceSpecification() {
		return instanceSpecification;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.executors.InstanceExecutor#getInstanceName()
	 */
	public String getInstanceName() {
		return instanceName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.executors.InstanceExecutor#setVariable(java
	 * .lang.String, org.eclipse.uml2.uml.ValueSpecification)
	 */
	public void setVariable(String name, ValueSpecification value) {
		Class myClass = (Class) instanceSpecification.getClassifiers().get(0);
		Property prop = null;
		List<Property> properties = filter(having(on(Property.class).getName(), equalTo(name)), myClass.getAllAttributes());
		if (!properties.isEmpty()) {
			// class variable
			prop = properties.get(0);
		} else {
			// TODO: create local slot
		}
		Slot slot = null;
		// do we have a slot that is needed?
		List<Slot> slots = filter(having(on(Slot.class).getDefiningFeature(), equalTo(prop)), instanceSpecification.getSlots());
		if (slots.isEmpty()) {
			slot = instanceSpecification.createSlot();
			slot.setDefiningFeature(prop);
		} else {
			slot = slots.get(0);
			// check for value name (for multiplicity many values)
			List<ValueSpecification> values = filter(having(on(ValueSpecification.class).getName(), equalTo(value.getName())), slot.getValues());
			if(!values.isEmpty()) {
				slot.getValues().removeAll(values);
			}
		}
		slot.getValues().add(value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.executors.InstanceExecutor#getVariable(java
	 * .lang.String)
	 */
	public ValueSpecification getVariable(String name) {
		return null;
	}

}
