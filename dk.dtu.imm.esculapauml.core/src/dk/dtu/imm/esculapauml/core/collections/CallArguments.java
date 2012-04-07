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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.UniqueEList;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterDirectionKind;
import org.eclipse.uml2.uml.ValueSpecification;

/**
 * Container that holds arguments for operation.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class CallArguments {
	private List<ValuesCollection> arguments = new ArrayList<ValuesCollection>();

	/**
	 * 
	 */
	public CallArguments() {
		super();
	}

	/**
	 * Creates arguments for operation based on flattened list of arguments.
	 * 
	 * @param operation
	 * @param arguments2
	 */
	public CallArguments(Operation operation, EList<ValueSpecification> args) {
		EList<Parameter> parameters = new UniqueEList.FastCompare<Parameter>((operation).getOwnedParameters());
		// match arguments for parameters in the order of declaration
		Iterator<Parameter> p = parameters.iterator();
		while (p.hasNext()) {
			Parameter param = p.next();
			if (param.getDirection() == ParameterDirectionKind.IN_LITERAL) {
				addArgument(new ValuesList(param.getName(), args));
			}
		}
	}

	/**
	 * @return the arguments
	 */
	public List<ValuesCollection> getArguments() {
		return arguments;
	}

	public ValuesCollection getArgument(int index) {
		return arguments.get(index);
	}

	public boolean addArgument(ValuesCollection arg) {
		return arguments.add(arg);
	}

	/**
	 * @return
	 */
	public boolean isEmpty() {
		return arguments.isEmpty();
	}

	/**
	 * @return
	 */
	public Iterator<ValuesCollection> iterator() {
		return arguments.iterator();
	}

	/**
	 * Gets flattened list of all arguments (for message presentation)
	 * 
	 * @return
	 */
	public EList<ValueSpecification> getFlattened() {
		EList<ValueSpecification> result = new BasicEList<ValueSpecification>();
		for (ValuesCollection arg : arguments) {
			result.addAll(arg);
		}
		return result;
	}

}
