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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.util.EcoreUtil;
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
	protected Class localClass = null;
	protected Class originalClass;
	protected String instanceName;

	/**
	 * @param checker
	 */
	public AbstractInstanceExecutor(Checker checker, String instanceName, Class originalClass) {
		super(checker);
		this.instanceName = instanceName;
		this.originalClass = originalClass;
		createInstanceSpecification();
	}

	/**
	 * Copy constructor.
	 * 
	 * @param checker
	 */
	public AbstractInstanceExecutor(InstanceExecutor executor) {
		super(executor.getChecker());
		this.instanceSpecification = executor.getInstanceSpecification();
		this.instanceName = executor.getInstanceName();
		this.originalClass = executor.getOriginalClass();
		this.localClass = executor.getLocalClass();
	}

	/**
	 * Creates the instance specification for this executor.
	 */
	protected void createInstanceSpecification() {
		// create instance specification
		instanceSpecification = UMLFactory.eINSTANCE.createInstanceSpecification();
		checker.getSystemState().getInstancePackage().getPackagedElements().add(instanceSpecification);
		checker.getSystemState().addGeneratedElement(instanceSpecification);
		instanceSpecification.getClassifiers().add(originalClass);
		instanceSpecification.setName(instanceName);
		evaluateDefaultValues();
	}

	/**
	 * Evaluates default values in the class (if any) for existing properties.
	 * 
	 */
	protected void evaluateDefaultValues() {
		EList<Property> properties = originalClass.getAllAttributes();
		for (Property property : properties) {
			if (null != property.getDefaultValue()) {
				if (property.getType().conformsTo(property.getDefaultValue().getType())) {
					setVariable(property.getName(), property.getDefaultValue());
				} else {
					checker.addProblem(Diagnostic.ERROR, "Default value for property '" + property.getLabel() + "' of class '" + originalClass.getLabel()
							+ "' is of the a wrong type.");
				}
			}
		}

	}

	/**
	 * Lazily creates class to hold local variables
	 */
	protected void swapInstanceSpecificationToLocal() {
		localClass = checker.getSystemState().getInstancePackage().createOwnedClass(instanceName + "Local", false);
		localClass.getSuperClasses().add(originalClass);
		checker.getSystemState().addGeneratedElement(localClass);
		instanceSpecification.getClassifiers().set(0, localClass);
	}

	/**
	 * This method is used to check if other executor didn't update our instance
	 * specification with swapInstanceSpecificationToLocal() function. In case
	 * it did, we also need an update to know about a local class.
	 * 
	 */
	protected void checkLocalClassUpdates() {
		if (null == localClass) {
			if (instanceSpecification.getClassifiers().get(0) != originalClass) {
				localClass = (Class) instanceSpecification.getClassifiers().get(0);
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.executors.InstanceExecutor#getLocalClass()
	 */
	public Class getLocalClass() {
		checkLocalClassUpdates();
		return localClass;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.executors.InstanceExecutor#getOriginalClass()
	 */
	public Class getOriginalClass() {
		return originalClass;
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
	public boolean setVariable(String name, ValueSpecification value) {
		ValueSpecification valueToSet = EcoreUtil.copy(value);
		checkLocalClassUpdates();
		Property prop = null;
		List<Property> properties = new ArrayList<Property>();
		if (null != localClass) {
			// include private attributes of the original class
			properties = filter(having(on(Property.class).getName(), equalTo(name)), localClass.getAttributes());
		}

		if (properties.isEmpty()) {
			// class variable or new local variable
			properties = filter(having(on(Property.class).getName(), equalTo(name)), originalClass.getAllAttributes());
			if (properties.isEmpty()) {
				// new local variable
				if (null == localClass) {
					swapInstanceSpecificationToLocal();
				}
				prop = localClass.createOwnedAttribute(name, valueToSet.getType());
			} else {
				// class variable
				prop = properties.get(0);
			}
		} else {
			// local variable
			prop = properties.get(0);
		}
		// type check
		if (!prop.getType().conformsTo(valueToSet.getType())) {
			EcoreUtil.delete(valueToSet);
			return false;
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
			if (!values.isEmpty()) {
				slot.getValues().removeAll(values);
			}
		}
		slot.getValues().add(valueToSet);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.executors.InstanceExecutor#getVariable(java
	 * .lang.String)
	 */
	public ValueSpecification getVariable(String name) {
		return getVariable(name, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.executors.InstanceExecutor#getVariable(java
	 * .lang.String, java.lang.String)
	 */
	public ValueSpecification getVariable(String name, String valueName) {
		List<Slot> slots = filter(having(on(Slot.class).getDefiningFeature().getName(), equalTo(name)), instanceSpecification.getSlots());
		if (slots.isEmpty()) {
			return null;
		} else {
			if (null == valueName) {
				// single multiplicity value
				if (slots.get(0).getValues().isEmpty()) {
					return null;
				} else {
					return slots.get(0).getValues().get(0);
				}
			} else {
				// multi multiplicity value
				List<ValueSpecification> values = filter(having(on(ValueSpecification.class).getName(), equalTo(valueName)), slots.get(0).getValues());
				if (values.isEmpty()) {
					return null;
				} else {
					return values.get(0);
				}
			}
		}
	}

	/**
	 * Creates a deep copy of instance values.
	 * 
	 * @return
	 */
	protected Collection<Slot> getDeepCopyOfMySlots() {
		checkLocalClassUpdates();
		return EcoreUtil.copyAll(instanceSpecification.getSlots());
	}

	/**
	 * Restores slots with other values.
	 * 
	 * @return
	 */
	protected void restoreCopyOfMySlots(Collection<Slot> slots) {
		instanceSpecification.getSlots().clear();
		instanceSpecification.getSlots().addAll(slots);
	}
}
