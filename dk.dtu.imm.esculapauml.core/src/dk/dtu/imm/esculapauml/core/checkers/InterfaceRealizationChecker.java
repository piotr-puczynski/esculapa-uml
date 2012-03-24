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

import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static org.hamcrest.Matchers.equalTo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.BehavioredClassifier;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ProtocolStateMachine;
import org.eclipse.uml2.uml.Type;

/**
 * Checks interface realizations, i.e. the contract conformance of the type to
 * the interface. This includes checking that operations / receptions in
 * interface are implemented methods in the type.
 * 
 * There is no easy way to check that the properties of the interface are
 * implemented in the realizing class as the semantics of UML leaves more leeway
 * for implementing these.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class InterfaceRealizationChecker extends AbstractChecker<BehavioredClassifier> {

	private Interface interface_;
	private Map<Operation, Operation> methodsToOperations = new HashMap<Operation, Operation>();
	private boolean hasValidProtocol = false;

	/**
	 * @param checker
	 * @param objectToCheck
	 */
	InterfaceRealizationChecker(Checker checker, BehavioredClassifier objectToCheck, Interface interface_) {
		super(checker, objectToCheck);
		this.interface_ = interface_;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dk.dtu.imm.esculapauml.core.checkers.Checker#check()
	 */
	@Override
	public void check() {
		checkMethods();
		if (!hasErrors()) {
			checkProtocol();
		}
	}

	/**
	 * Checks the protocol of the interface.
	 */
	private void checkProtocol() {
		ProtocolStateMachine psm = interface_.getProtocol();
		if (null != psm) {
			ProtocolStateMachineChecker psmc = new ProtocolStateMachineChecker(this, psm);
			psmc.check();
			if (!psmc.hasErrors()) {
				hasValidProtocol = true;
			}
		}

	}

	/**
	 * Checks if operations of interface are present as methods in realization.
	 * 
	 */
	private void checkMethods() {
		EList<Operation> methods = checkee.getAllOperations();
		for (Operation op : interface_.getAllOperations()) {
			List<Operation> methodsWithTheSameName = filter(having(on(Operation.class).getName(), equalTo(op.getName())), methods);
			boolean hasMethod = false;
			for (Operation method : methodsWithTheSameName) {
				if (isIdenticalWith(op, method)) {
					methodsToOperations.put(method, op);
					hasMethod = true;
					break;
				}
			}
			if (!hasMethod) {
				addProblem(Diagnostic.ERROR, "Classifier does not realize the interface '" + interface_.getLabel() + "': operation '" + op.getName()
						+ "' has no implementing method.");
			}
		}

	}

	/**
	 * Checks if operations are identical.
	 * 
	 * @param operation
	 * @param op
	 * @return
	 */
	private boolean isIdenticalWith(Operation operation, Operation op) {

		EList<Parameter> ownedParameters = operation.getOwnedParameters();
		int ownedParametersSize = ownedParameters.size();
		EList<Parameter> opOwnedParameters = op.getOwnedParameters();

		EList<Parameter> returnResult = operation.returnResult();
		int returnResultSize = returnResult.size();
		EList<Parameter> opReturnResult = op.returnResult();

		if (ownedParametersSize == opOwnedParameters.size() && returnResultSize == opReturnResult.size()) {

			for (int i = 0; i < ownedParametersSize; i++) {
				Type opOwnedParameterType = opOwnedParameters.get(i).getType();
				Type ownedParameterType = ownedParameters.get(i).getType();

				String opOwnedParameterName = opOwnedParameters.get(i).getName();
				String ownedParameterName = ownedParameters.get(i).getName();

				if (!opOwnedParameterName.equals(ownedParameterName)) {
					return false;
				}

				if (opOwnedParameterType == null ? ownedParameterType != null : !opOwnedParameterType.equals(ownedParameterType)) {

					return false;
				}
			}

			for (int i = 0; i < returnResultSize; i++) {
				Type opReturnResultType = opReturnResult.get(i).getType();
				Type returnResultType = returnResult.get(i).getType();

				if (opReturnResultType == null ? returnResultType != null : !opReturnResultType.equals(returnResultType)) {

					return false;
				}
			}

			return true;
		}

		return false;
	}

	/**
	 * Returns map of implementing methods to operations in checked classifier.
	 * 
	 * @return the methodsToOperations
	 */
	public Map<Operation, Operation> getMethodsToOperations() {
		return methodsToOperations;
	}

	/**
	 * @return the hasProtocol
	 */
	public boolean hasValidProtocol() {
		return hasValidProtocol;
	}

}
