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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.CallEvent;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.Trigger;
import org.eclipse.uml2.uml.Vertex;

import ch.lambdaj.function.matcher.Predicate;
import dk.dtu.imm.esculapauml.core.executors.guards.GuardEvaluator;
import dk.dtu.imm.esculapauml.core.executors.guards.GuardEvaluatorsFactory;

/**
 * Helper class that helps to evaluate protocol transitions during protocol
 * verification. Uses model checking to find the valid paths.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class OperationVerifier {

	private Map<Operation, Transition> operations = new HashMap<Operation, Transition>();
	private ProtocolVerifier protocolVerifier;
	private List<List<Transition>> paths = new ArrayList<List<Transition>>();
	InstanceExecutor executor;

	/**
	 * 
	 */
	public OperationVerifier(ProtocolVerifier protocolVerifier, InstanceExecutor executor) {
		super();
		this.protocolVerifier = protocolVerifier;
		this.executor = executor;
	}

	/**
	 * Finds the valid paths with valid preconditions for operations.
	 * 
	 * @param operation
	 */
	public void preCall(final Operation operation) {
		final Predicate<Transition> hasTriggerForOperation = new Predicate<Transition>() {
			public boolean apply(Transition item) {
				for (Trigger t : item.getTriggers()) {
					if (t.getEvent() instanceof CallEvent) {
						if (((CallEvent) t.getEvent()).getOperation() == operation) {
							return true;
						}
					}
				}
				return false;
			}
		};

		Predicate<Transition> canBeTaken = new Predicate<Transition>() {
			public boolean apply(Transition item) {
				return item.getTriggers().isEmpty() || hasTriggerForOperation.apply(item);
			}
		};

		if (operations.isEmpty()) {
			// generate initial paths
			for (Vertex vertex : protocolVerifier.getActiveConfiguration()) {
				GuardEvaluator ge = GuardEvaluatorsFactory.getGuardEvaluator(executor, vertex);
				ge.setPreconditions(canBeTaken);
				EList<Transition> satisfiedTransitions = ge.getTransitionsWithEnabledGuards();
				for (Transition transition : satisfiedTransitions) {
					List<Transition> path = new ArrayList<Transition>();
					path.add(transition);
					paths.add(path);
				}
			}
			
			// initialize visited
			Set<Vertex> visited = new HashSet<Vertex>();
			for (List<Transition> path : paths) {
				for (Transition transition : path) {
					if (null != transition.getSource())
						visited.add(transition.getSource());
				}
			}
			
			// check the paths
			while(!paths.isEmpty() && !getPathsLastElementSatisfingPredicate(paths, hasTriggerForOperation).isEmpty()) {
				
			}
		}
	}

	/**
	 * @param paths2
	 * @param hasTriggerForOperation2
	 * @return
	 */
	private List<List<Transition>> getPathsLastElementSatisfingPredicate(List<List<Transition>> paths2, Predicate<Transition> predicate) {
		List<List<Transition>> result = new ArrayList<List<Transition>>();
		for(List<Transition> path: paths2){
			if(!paths.isEmpty() ) {
				result.add(path);
			}
		}
		return result;
	}
}
