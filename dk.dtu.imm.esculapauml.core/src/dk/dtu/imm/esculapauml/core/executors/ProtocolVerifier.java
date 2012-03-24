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

import java.util.Map;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.uml2.uml.CallEvent;
import org.eclipse.uml2.uml.InstanceSpecification;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.ProtocolStateMachine;
import org.eclipse.uml2.uml.Trigger;

import dk.dtu.imm.esculapauml.core.checkers.Checker;
import dk.dtu.imm.esculapauml.core.executors.coordination.EsculapaCallEvent;
import dk.dtu.imm.esculapauml.core.executors.coordination.EsculapaCallReturnControlEvent;
import dk.dtu.imm.esculapauml.core.executors.coordination.EsculapaReplyEvent;
import dk.dtu.imm.esculapauml.core.executors.coordination.ExecutionListener;

/**
 * Verifies protocol state machines by observing the system execution and
 * verifying that it conforms to the protocols.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class ProtocolVerifier extends AbstractExecutor implements ExecutionListener {
	private InstanceSpecification instanceSpecification;
	private Interface interface_;
	private Map<Operation, Operation> methodsToOperations;
	private EList<Operation> unreferredOperations;
	private ProtocolStateMachine protocol;

	/**
	 * @param checker
	 */
	public ProtocolVerifier(Checker checker, Interface interface_, InstanceSpecification instanceSpecification, Map<Operation, Operation> methodsToOperations) {
		super(checker);
		this.interface_ = interface_;
		this.instanceSpecification = instanceSpecification;
		this.methodsToOperations = methodsToOperations;
		protocol = interface_.getProtocol();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dk.dtu.imm.esculapauml.core.executors.Executor#prepare()
	 */
	@Override
	public void prepare() {
		calculateUnrefferedOperations();
		checker.getSystemState().getCoordinator().addExecutionListener(this);
	}

	/**
	 * Unreferred operations are operations for which there are no triggers in
	 * the PSM and they can be executed at any time without change of PSM state.
	 * 
	 */
	private void calculateUnrefferedOperations() {
		EList<Operation> allOperations = interface_.getAllOperations();
		unreferredOperations = new BasicEList<Operation>(allOperations);
		TreeIterator<EObject> contents = protocol.eAllContents();
		while (contents.hasNext()) {
			EObject o = contents.next();
			if (o instanceof Trigger) {
				Trigger t = (Trigger) o;
				if (t.getEvent() instanceof CallEvent) {
					Operation op = ((CallEvent) t.getEvent()).getOperation();
					if (null == op) {
						checker.addOtherProblem(Diagnostic.ERROR, "Call event reffered from PSM '" + protocol.getLabel() + "' has no operation defined.",
								t.getEvent());
					} else {
						if (unreferredOperations.contains(op)) {
							// white list it
							unreferredOperations.remove(op);
						} else {
							if (!allOperations.contains(op)) {
								checker.addOtherProblem(Diagnostic.ERROR, "Call event reffered from PSM '" + protocol.getLabel()
										+ "' has operation that is not defined for interface '" + interface_.getLabel() + "'.", t.getEvent());
							}
						}
					}
				}
			}
		}

	}

	/**
	 * @return the instanceSpecification
	 */
	public InstanceSpecification getInstanceSpecification() {
		return instanceSpecification;
	}

	/**
	 * @return the interface_
	 */
	public Interface getInterface() {
		return interface_;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.executors.coordination.ExecutionListener#
	 * callEventOccurred
	 * (dk.dtu.imm.esculapauml.core.executors.coordination.EsculapaCallEvent)
	 */
	@Override
	public void callEventOccurred(EsculapaCallEvent event) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.executors.coordination.ExecutionListener#
	 * replyEventOccurred
	 * (dk.dtu.imm.esculapauml.core.executors.coordination.EsculapaReplyEvent)
	 */
	@Override
	public void replyEventOccurred(EsculapaReplyEvent event) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.executors.coordination.ExecutionListener#
	 * callReturnControlEventOccurred
	 * (dk.dtu.imm.esculapauml.core.executors.coordination
	 * .EsculapaCallReturnControlEvent)
	 */
	@Override
	public void callReturnControlEventOccurred(EsculapaCallReturnControlEvent event) {
		// TODO Auto-generated method stub

	}

}
