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
package dk.dtu.imm.esculapauml.core.checkers;

import java.util.Iterator;

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.UniqueEList;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageSort;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.ValueSpecification;

/**
 * 
 * Checker for interaction messages
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class MessageChecker extends AbstractChecker<Message> {

	/**
	 * @param existingDiagnostics
	 */
	public MessageChecker(BasicDiagnostic existingDiagnostics, Message message) {
		super(existingDiagnostics, message);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dk.dtu.imm.esculapauml.core.checkers.CheckerInterface#check()
	 */
	@Override
	public void check() {
		structuralMessageConformanceCheck();

	}

	/**
	 * Checks if message calls existing operation and with correct arguments
	 * Parts of original code were copied from MessageOperations.java in UML2
	 */
	protected void structuralMessageConformanceCheck() {
		NamedElement signature = checkee.getSignature();

		if ((checkee.getMessageSort() == MessageSort.SYNCH_CALL_LITERAL) || (checkee.getMessageSort() == MessageSort.ASYNCH_CALL_LITERAL)) {
			if (signature instanceof Operation) {
				EList<ValueSpecification> arguments = checkee.getArguments();

				if (!arguments.isEmpty()) {
					EList<Parameter> parameters = new UniqueEList.FastCompare<Parameter>(((Operation) signature).getOwnedParameters());

					if (arguments.size() != parameters.size()) {
						addProblem(Diagnostic.ERROR, "The Message " + checkee.getLabel() + " has wrong number of arguments.");
					} else {
						Iterator<ValueSpecification> a = arguments.iterator();
						Iterator<Parameter> p = parameters.iterator();

						while (a.hasNext() && p.hasNext()) {
							Type argumentType = a.next().getType();
							Type parameterType = p.next().getType();

							if (argumentType == null ? parameterType != null : !argumentType.conformsTo(parameterType)) {
								addProblem(Diagnostic.ERROR, "The Message " + checkee.getLabel() + " has wrong type of arguments.");
								break;
							}
						}
					}
				}
			} else { // operation is not specified
				addProblem(Diagnostic.ERROR, "The Message " + checkee.getLabel() + " has no operation set.");
			}
		}

	}

}
