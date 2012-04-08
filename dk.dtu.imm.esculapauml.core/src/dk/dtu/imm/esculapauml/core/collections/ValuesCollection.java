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

import java.util.Collection;

import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.MultiplicityElement;
import org.eclipse.uml2.uml.TypedElement;
import org.eclipse.uml2.uml.ValueSpecification;

import dk.dtu.imm.esculapauml.core.checkers.BehaviorChecker;
import dk.dtu.imm.esculapauml.core.checkers.Checker;
import org.eclipse.uml2.uml.Type;

/**
 * Inferface for UML collection for handling arguments values during execution.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public interface ValuesCollection extends Collection<ValueSpecification> {

	/**
	 * @param index
	 * @return
	 */
	ValueSpecification get(int index);

	/**
	 * @param index
	 * @param element
	 */
	void add(int index, ValueSpecification element);

	/**
	 * @param o
	 * @return
	 */
	int indexOf(Object o);

	/**
	 * @param o
	 * @return
	 */
	int lastIndexOf(Object o);

	/**
	 * @param index
	 * @return
	 */
	ValueSpecification remove(int index);

	/**
	 * @param index
	 * @param element
	 * @return
	 */
	ValueSpecification set(int index, ValueSpecification element);

	/**
	 * Sets all arguments to have the given name.
	 * 
	 * @param name
	 */
	void setName(String name);

	/**
	 * Converts OCL result values to ValueSpecifications in UML and adds them to
	 * the collection.
	 * 
	 * @param result
	 * @param checker
	 * @param errorContext
	 * @throws OCLConversionException
	 */
	void addFromOCL(Object oclValue, BehaviorChecker checker, Element errorContext) throws OCLConversionException;

	/**
	 * Checks if there is exactly one value in the collection. If not it places
	 * good error message in the checker and then returns false.
	 * 
	 * @param trc
	 * 
	 * @return
	 */
	boolean isSingleValued(Checker trc);

	/**
	 * Used to evaluate selector on given collection. Selector must be
	 * IntegerLiteral.
	 * 
	 * @param selector
	 * @return
	 */
	boolean select(ValuesCollection selector);

	/**
	 * Check if collection can be assigned to given typed element.
	 * 
	 * @param returnParam
	 * @return
	 */
	boolean conformsToType(TypedElement type);

	/**
	 * Check if collection can be assigned to given multiplicity element.
	 * 
	 * @param returnParam
	 * @return
	 */
	boolean conformsToMultiplicity(MultiplicityElement multiplicity);

	/**
	 * Infers generic type of collection. If collection type cannot be inferred,
	 * it returns null.
	 * 
	 * @return
	 */
	Type inferType();

	/**
	 * Infers lower multiplicity of collection.
	 * 
	 * @return
	 */
	int inferLowerMultiplicity();

	/**
	 * Infers upper multiplicity of collection.
	 * 
	 * @return
	 */
	int inferUpperMultiplicity();

	/**
	 * @return
	 */
	boolean isSingleValued();

}
