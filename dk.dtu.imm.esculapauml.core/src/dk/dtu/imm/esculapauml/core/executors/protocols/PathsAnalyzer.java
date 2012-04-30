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
package dk.dtu.imm.esculapauml.core.executors.protocols;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Region;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.Vertex;

import dk.dtu.imm.esculapauml.core.executors.InstanceExecutor;
import dk.dtu.imm.esculapauml.core.executors.ProtocolVerifier;
import dk.dtu.imm.esculapauml.core.executors.coordination.EsculapaCallEvent;
import dk.dtu.imm.esculapauml.core.executors.coordination.EsculapaReplyEvent;
import dk.dtu.imm.esculapauml.core.utils.StateMachineUtils;

/**
 * Helper class that helps to evaluate protocol transitions during protocol
 * verification. Keeps all valid paths in PSM and eliminates invalid ones.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class PathsAnalyzer {

	private ProtocolVerifier protocolVerifier;
	private List<PSMState> states = new ArrayList<PSMState>();
	private InstanceExecutor executor;

	/**
	 * 
	 */
	public PathsAnalyzer(ProtocolVerifier protocolVerifier, InstanceExecutor executor) {
		super();
		this.protocolVerifier = protocolVerifier;
		this.executor = executor;
	}

	/**
	 * Finds the valid paths with valid preconditions for operation.
	 * 
	 * @param operation
	 * @param event
	 */
	public void preCall(Operation operation, EsculapaCallEvent event) {
		for (PSMState psmState : new ArrayList<PSMState>(states)) {
			psmState.preCall(operation, event);
		}
		eliminateDuplicatedAndTerminatedStates();

		if (states.isEmpty()) {
			executor.getChecker().addOtherProblem(
					Diagnostic.ERROR,
					"Operation '" + operation.getLabel() + "' cannot be executed by instance '" + executor.getInstanceName() + "' according to PSM '"
							+ protocolVerifier.getProtocol().getLabel() + "' (pre-state) specified in context of interface '"
							+ protocolVerifier.getInterface().getLabel() + "'.", event.getErrorContext());
		}
	}

	/**
	 * Finds the valid paths with valid postconditions for operation.
	 * 
	 * @param operation
	 * @param event
	 */
	public void postCall(Operation operation, EsculapaReplyEvent event) {
		// add result of operation so that it could be evaluated by potential
		// result in post-conditions
		if (null != event.getResult()) {
			executor.setVariable("result", event.getResult(), event.getErrorContext());
		}

		for (PSMState psmState : new ArrayList<PSMState>(states)) {
			psmState.postCall(event);
		}
		eliminateDuplicatedAndTerminatedStates();

		// delete result if necessary
		if (null != event.getResult()) {
			executor.removeVariable("result", true);
		}

		if (states.isEmpty()) {
			executor.getChecker().addOtherProblem(
					Diagnostic.ERROR,
					"Operation '" + operation.getLabel() + "' cannot be executed by instance '" + executor.getInstanceName() + "' according to PSM '"
							+ protocolVerifier.getProtocol().getLabel() + "' (post-condition failed) specified in context of interface '"
							+ protocolVerifier.getInterface().getLabel() + "'.", event.getErrorContext());
		}
	}

	/**
	 * Creates initial paths for PSM.
	 */
	public void initiate() {
		// enable initials
		for (Region r : protocolVerifier.getProtocol().getRegions()) {
			Set<Vertex> activeConfiguration = new HashSet<Vertex>();
			activeConfiguration.add(StateMachineUtils.getInitial(r));
			states.add(new PSMState(this, activeConfiguration));
		}

		eliminateDuplicatedAndTerminatedStates();

	}

	/**
	 * Eliminates terminated and duplicated states after full step was
	 * committed.
	 * 
	 */
	private void eliminateDuplicatedAndTerminatedStates() {
		// remove terminated states
		Iterator<PSMState> it = states.iterator();
		while (it.hasNext()) {
			PSMState pmsState = it.next();
			if (pmsState.isTerminated()) {
				it.remove();
			}
		}
		// remove duplicates
		Set<PSMState> hs = new HashSet<PSMState>();
		hs.addAll(states);
		states.clear();
		states.addAll(hs);
	}

	/**
	 * @return the executor
	 */
	public InstanceExecutor getExecutor() {
		return executor;
	}

	/**
	 * @return the protocolVerifier
	 */
	public ProtocolVerifier getProtocolVerifier() {
		return protocolVerifier;
	}

	/**
	 * Creates new state of PSM based on given state and going to transition.
	 * 
	 * @param psmState
	 * @param transition
	 */
	public void createNewState(PSMState psmState, Transition transition) {
		states.add(new PSMState(psmState, transition));
	}

	/**
	 * Creates new state of PSM based on given configuration and going to
	 * transition with event.
	 * 
	 * @param psmState
	 * @param event
	 * @param transition
	 */
	public void createNewState(PSMState psmState, EsculapaCallEvent event, Transition transition) {
		states.add(new PSMState(psmState, event, transition));
	}

}
