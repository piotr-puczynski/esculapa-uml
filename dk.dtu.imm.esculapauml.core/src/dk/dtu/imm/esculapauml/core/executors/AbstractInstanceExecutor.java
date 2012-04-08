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
import static org.hamcrest.Matchers.not;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.InstanceSpecification;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Slot;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.ValueSpecification;
import org.eclipse.uml2.uml.VisibilityKind;

import dk.dtu.imm.esculapauml.core.checkers.Checker;
import dk.dtu.imm.esculapauml.core.collections.ValuesCollection;
import dk.dtu.imm.esculapauml.core.collections.ValuesList;
import dk.dtu.imm.esculapauml.core.utils.Pair;

/**
 * Executes instances.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public abstract class AbstractInstanceExecutor extends AbstractExecutor implements InstanceExecutor {

	protected InstanceSpecification instanceSpecification;
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
		checker.getSystemState().registerInstanceExecutor(this);
	}

	/**
	 * @param checker
	 * @param instanceSpecification2
	 * @param context
	 */
	public AbstractInstanceExecutor(Checker checker, InstanceSpecification instanceSpecification, Class originalClass) {
		super(checker);
		this.instanceSpecification = instanceSpecification;
		this.instanceName = instanceSpecification.getName();
		this.originalClass = originalClass;
		evaluateDefaultValues();
		checker.getSystemState().registerInstanceExecutor(this);
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
		// do not register itself in the state registry, it's enough that our
		// parent is registered
	}

	/**
	 * Creates the instance specification for this executor.
	 */
	protected void createInstanceSpecification() {
		// create instance specification only if it does not already exist
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
				if (null == getVariable(property.getName())) {
					// variable is not set
					if (!setVariable(property.getName(), 0, property.getDefaultValue(), null)) {
						checker.addProblem(Diagnostic.ERROR, "Default value for property '" + property.getLabel() + "' of class '" + originalClass.getLabel()
								+ "' is of a wrong type.");
						break;
					}
				}
			}
		}

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
	 * .lang.String, org.eclipse.uml2.uml.ValueSpecification,
	 * org.eclipse.uml2.uml.Element)
	 */
	public boolean setVariable(String name, ValuesCollection value, Element errorContext) {
		return setVariable(name, value, errorContext, false, null, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.executors.InstanceExecutor#setVariable(java
	 * .lang.String, dk.dtu.imm.esculapauml.core.collections.ValuesCollection,
	 * org.eclipse.uml2.uml.Element, boolean,
	 * org.eclipse.uml2.uml.ValueSpecification,
	 * org.eclipse.uml2.uml.ValueSpecification)
	 */
	@Override
	public boolean setVariable(String name, ValuesCollection value, Element errorContext, boolean setMultiplicities, ValueSpecification lowerValue,
			ValueSpecification upperValue) {
		Property prop = null;
		boolean isLinkMode = false;
		List<InstanceSpecification> variableContext = new ArrayList<InstanceSpecification>();
		for (int i = 0; i < value.size(); ++i) {
			variableContext.add(instanceSpecification);
		}
		Pair<Association, Property> assoc = findPropertyInAssociationFor(name);
		if (null != assoc) {
			// this is association
			// check if a link is already existing
			EList<InstanceSpecification> instances = checker.getSystemState().getExistingLinksForInstance(assoc.getLeft(), instanceSpecification);
			if (instances.size() > value.size()) {
				// we need to remove additional links (if necessary)
				for (int i = instances.size() - 1; i >= value.size(); --i) {
					checker.getSystemState().removeInstanceSpecification(instances.get(i));
					instances.remove(i);
				}
			}
			isLinkMode = true;
			if (instances.size() == value.size()) {
				// great :) we have exact number of required links
				variableContext = new ArrayList<InstanceSpecification>(instances);
				prop = assoc.getRight();
			} else {
				// TODO add creation of new links
				checker.addOtherProblem(Diagnostic.ERROR, "Link out of bounds error when trying to assign '" + name + ".", errorContext);
				return false;
			}

		} else {
			// this is attribute
			prop = findAttributeFor(name);
		}
		if (null == prop) {
			// create local variable
			Type infered = value.inferType();
			if (null == infered) {
				checker.addOtherProblem(Diagnostic.ERROR,
						"Type couldn't be inferred from given value to any significant type when tried to create local variable '" + name + "'.", errorContext);
				return false;
			} else {
				prop = originalClass.createOwnedAttribute(name, infered);
				prop.setVisibility(VisibilityKind.PRIVATE_LITERAL);
				if (setMultiplicities) {
					if (null != lowerValue) {
						prop.setLowerValue(EcoreUtil.copy(lowerValue));
					}
					if (null != upperValue) {
						prop.setUpperValue(EcoreUtil.copy(upperValue));
					}
				} else if (!value.isSingleValued()) {
					prop.setLower(value.inferLowerMultiplicity());
					prop.setUpper(value.inferUpperMultiplicity());
				}
				checker.getSystemState().addGeneratedElement(prop);
			}
		}
		// type check
		if (!value.conformsToType(prop)) {
			if (null != errorContext) {
				if (null != prop.getType()) {
					checker.addOtherProblem(Diagnostic.ERROR, "Type check failed when trying to assign '" + name + "' to value: " + value
							+ ". Required type must conform to: " + prop.getType().getName() + ".", errorContext);
				} else {
					checker.addOtherProblem(Diagnostic.ERROR, "Type check failed when trying to assign '" + name + "' to value " + value + ".", errorContext);
				}
			}
			return false;
		}
		// multiplicity check
		if (!value.conformsToMultiplicity(prop)) {
			checker.addOtherProblem(Diagnostic.ERROR, "Multiplicity check failed when trying to assign '" + name + "' to value: " + value + ".", errorContext);
		}
		// do we have a slot that is needed?
		for (int i = 0; i < value.size(); ++i) {
			int index;
			if (isLinkMode) {
				index = 0;
			} else {
				index = i;
			}
			Slot slot = null;
			List<Slot> slots = filter(having(on(Slot.class).getDefiningFeature(), equalTo(prop)), variableContext.get(i).getSlots());
			if (slots.isEmpty()) {
				if (0 == index) {
					slot = variableContext.get(i).createSlot();
					slot.setDefiningFeature(prop);
				} else {
					checker.addOtherProblem(Diagnostic.ERROR,
							"Cannot initialize local variable '" + name + "' with index greater than 0 (" + String.valueOf(index) + ").", errorContext);
					return false;
				}
			} else {
				slot = slots.get(0);
			}
			if (!isLinkMode && i == 0) {
				// remove additional attribute values in first iteration
				if (slot.getValues().size() > value.size()) {
					for (int r = slot.getValues().size() - 1; r >= value.size(); --r) {
						slot.getValues().remove(r);
					}
				}
			}
			if (index < slot.getValues().size()) {
				// remove old value
				slot.getValues().remove(index);
			}
			if (index < 0 || index > slot.getValues().size()) {
				checker.addOtherProblem(Diagnostic.ERROR, "Array out of bounds error when trying to assign '" + name + "' (" + String.valueOf(index) + ").",
						errorContext);
				return false;
			} else {
				slot.getValues().add(index, EcoreUtil.copy(value.get(i)));
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.executors.InstanceExecutor#setVariable(java
	 * .lang.String, int, org.eclipse.uml2.uml.ValueSpecification,
	 * org.eclipse.uml2.uml.Element)
	 */
	public boolean setVariable(String name, int index, ValueSpecification value, Element errorContext) {
		ValueSpecification valueToSet = EcoreUtil.copy(value);
		Property prop = null;
		Slot slot = null;
		InstanceSpecification variableContext = instanceSpecification;
		Pair<Association, Property> assoc = findPropertyInAssociationFor(name);
		if (null != assoc) {
			// this is association
			// check if a link is already existing
			EList<InstanceSpecification> instances = checker.getSystemState().getExistingLinksForInstance(assoc.getLeft(), instanceSpecification);
			if (!instances.isEmpty() && index < instances.size()) {
				// we have a link
				variableContext = instances.get(index);
				prop = assoc.getRight();
				// reset index (always zero inside links)
				index = 0;
				// } else if (index == instances.size()) {
				// TODO add creation of new link
			} else {
				checker.addOtherProblem(Diagnostic.ERROR, "Link out of bounds error when trying to assign '" + name + "' (" + String.valueOf(index) + ").",
						errorContext);
				return false;
			}

		} else {
			// this is attribute
			prop = findAttributeFor(name);
		}
		if (null == prop) {
			// create local variable
			if (null == valueToSet.getType()) {
				checker.addOtherProblem(Diagnostic.ERROR,
						"Type couldn't be coerced from given value to any significant type when tried to create local variable '" + name + "'.", errorContext);
				return false;
			} else {
				prop = originalClass.createOwnedAttribute(name, valueToSet.getType());
				prop.setVisibility(VisibilityKind.PRIVATE_LITERAL);
				checker.getSystemState().addGeneratedElement(prop);
			}
		}
		// type check
		if (!prop.getType().conformsTo(valueToSet.getType())) {
			if (null != errorContext) {
				if (null != value.getType() && null != prop.getType()) {
					checker.addOtherProblem(Diagnostic.ERROR, "Type check failed when trying to assign '" + name + "' to value of type: "
							+ value.getType().getName() + ". Required type must conform to: " + prop.getType().getName() + ".", errorContext);
				} else {
					checker.addOtherProblem(Diagnostic.ERROR, "Type check failed when trying to assign '" + name + "'.", errorContext);
				}
			}
			return false;
		}
		// do we have a slot that is needed?
		if (null == slot) {
			List<Slot> slots = filter(having(on(Slot.class).getDefiningFeature(), equalTo(prop)), variableContext.getSlots());
			if (slots.isEmpty()) {
				if (0 == index) {
					slot = variableContext.createSlot();
					slot.setDefiningFeature(prop);
				} else {
					checker.addOtherProblem(Diagnostic.ERROR,
							"Cannot initialize local variable '" + name + "' with index greater than 0 (" + String.valueOf(index) + ").", errorContext);
					return false;
				}
			} else {
				slot = slots.get(0);
			}
		}
		if (index < slot.getValues().size()) {
			// remove old value
			slot.getValues().remove(index);
		}
		if (index < 0 || index > slot.getValues().size()) {
			checker.addOtherProblem(Diagnostic.ERROR, "Array out of bounds error when trying to assign '" + name + "' (" + String.valueOf(index) + ").",
					errorContext);
			return false;
		} else {
			slot.getValues().add(index, valueToSet);
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.executors.InstanceExecutor#getVariable(java
	 * .lang.String)
	 */
	public ValuesCollection getVariable(String name) {
		List<Slot> slots = filter(having(on(Slot.class).getDefiningFeature().getName(), equalTo(name)), instanceSpecification.getSlots());
		if (slots.isEmpty()) {
			return null;
		} else {
			return new ValuesList(slots.get(0).getValues());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.executors.InstanceExecutor#getVariable(java
	 * .lang.String, int)
	 */
	public ValueSpecification getVariable(String name, int index) {
		List<Slot> slots = filter(having(on(Slot.class).getDefiningFeature().getName(), equalTo(name)), instanceSpecification.getSlots());
		if (slots.isEmpty()) {
			return null;
		} else {
			if (index >= slots.get(0).getValues().size()) {
				// checker.addOtherProblem(Diagnostic.ERROR,
				// "Read of variable '" + name + "' out of bounds (" +
				// String.valueOf(index) + ").", errorContext);
				return null;
			} else {
				return slots.get(0).getValues().get(index);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.executors.InstanceExecutor#removeVariable
	 * (java.lang.String, boolean)
	 */
	public boolean removeVariable(String name, boolean removeDeclaringModelElement) {
		List<Slot> slots = filter(having(on(Slot.class).getDefiningFeature().getName(), equalTo(name)), instanceSpecification.getSlots());
		if (slots.isEmpty()) {
			return false;
		} else {
			Slot slot = slots.get(0);
			EcoreUtil.delete(slot, true);
			if (removeDeclaringModelElement) {
				Property prop = findAttributeFor(name);
				if (null != prop) {
					EcoreUtil.delete(prop, true);
				}
			}
			return true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.executors.InstanceExecutor#getOperationByName
	 * (java.lang.String)
	 */
	public Operation getOperationByName(String name) {
		for (Operation operation : originalClass.getAllOperations()) {
			if (operation.getName().equals(name)) {
				return operation;
			}
		}
		return null;
	}

	/**
	 * Creates a deep copy of instance values.
	 * 
	 * @return
	 */
	protected Collection<Slot> getDeepCopyOfMySlots() {
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

	/**
	 * Looks in attributes for given property.
	 * 
	 * @param name
	 * @return
	 */
	protected Property findAttributeFor(String name) {
		List<Property> properties = new ArrayList<Property>();
		// class variable or new local variable
		properties = filter(having(on(Property.class).getName(), equalTo(name)), originalClass.getAllAttributes());

		if (properties.isEmpty()) {
			return null;
		} else {
			return properties.get(0);
		}
	}

	/**
	 * Looks in associations for given property.
	 * 
	 * @param name
	 * @return
	 */
	protected Pair<Association, Property> findPropertyInAssociationFor(String name) {
		// class association
		for (Association assoc : originalClass.getAssociations()) {
			// there are always two ends
			List<Property> assocProp = assoc.getMemberEnds();
			if (assocProp.size() == 2) {
				if (assocProp.get(0).getType() == assocProp.get(1).getType()) {
					// self association
					List<Property> filtered = filter(having(on(Property.class).getName(), equalTo(name)), assoc.getOwnedEnds());
					if (!filtered.isEmpty()) {
						return new Pair<Association, Property>(assoc, filtered.get(0));
					}
				} else {
					// exclude end that point to us
					List<Property> filtered = filter(having(on(Property.class).getName(), equalTo(name)), assoc.getOwnedEnds());
					filtered = filter(having(on(Property.class).getType(), not(equalTo(originalClass))), filtered);
					if (!filtered.isEmpty()) {
						return new Pair<Association, Property>(assoc, filtered.get(0));
					}
				}
			}
		}

		return null;

	}

}
