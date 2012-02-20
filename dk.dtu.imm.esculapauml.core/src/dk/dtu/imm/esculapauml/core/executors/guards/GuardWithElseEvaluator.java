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
package dk.dtu.imm.esculapauml.core.executors.guards;

import static ch.lambdaj.Lambda.filter;
import static org.hamcrest.Matchers.equalToIgnoringCase;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.OpaqueExpression;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.Vertex;
import org.hamcrest.Matcher;

import ch.lambdaj.function.matcher.Predicate;

import dk.dtu.imm.esculapauml.core.executors.InstanceExecutor;

/**
 * Guard evaluator for vertices with possible "else" statement.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class GuardWithElseEvaluator extends SimpleGuardEvaluator implements GuardEvaluator {
	
	protected Matcher<Transition> isElseTransition = new Predicate<Transition>() {
		public boolean apply(Transition item) {
			return isElse(item.getGuard());
		}
	};

	/**
	 * @param executor
	 * @param vertex
	 */
	public GuardWithElseEvaluator(InstanceExecutor executor, Vertex vertex) {
		super(executor, vertex);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dk.dtu.imm.esculapauml.core.executors.guards.GuardEvaluator#
	 * getTransitionsWithEnabledGuards()
	 */
	@Override
	public EList<Transition> getTransitionsWithEnabledGuards() {
		// check if we have else in the transitions
		BasicEList<Transition> elses = new BasicEList<Transition>(filter(isElseTransition, vertex.getOutgoings()));
		if (elses.size() > 1) {
			// only one else is allowed
			executor.getChecker().addOtherProblem(Diagnostic.ERROR, "Only one \"else\" is allowed.", elses.toArray());
			// return empty list
			return new BasicEList<Transition>();
		}
		BasicEList<Transition> goodTransitions = new BasicEList<Transition>(vertex.getOutgoings());
		goodTransitions.removeAll(elses);
		goodTransitions.retainAll(filter(satisfied, goodTransitions));
		if(goodTransitions.isEmpty()) {
			return elses;
		} else {
			return goodTransitions;
		}
	}

	/**
	 * Check if guard is else guard.
	 * 
	 * @param guard
	 * @return
	 */
	protected boolean isElse(Constraint guard) {
		if (null == guard) {
			// no guard
			return false;
		}
		if (guard.getSpecification() instanceof OpaqueExpression) {
			return !filter(equalToIgnoringCase("else"), ((OpaqueExpression) guard.getSpecification()).getBodies()).isEmpty();
		}

		return false;
	}

}
