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

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.Vertex;
import org.hamcrest.Matcher;

import ch.lambdaj.function.matcher.Predicate;

import dk.dtu.imm.esculapauml.core.executors.InstanceExecutor;
import dk.dtu.imm.esculapauml.core.validators.Validator;
import dk.dtu.imm.esculapauml.core.validators.ValidatorsFactory;

/**
 * Guard evaluator for vertices based on simple evaluation.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class SimpleGuardEvaluator implements GuardEvaluator {

	protected InstanceExecutor executor;
	protected Vertex vertex;

	protected Predicate<Transition> preconditions = null;

	protected Matcher<Transition> satisfied = new Predicate<Transition>() {
		public boolean apply(Transition item) {
			if (null == preconditions) {
				return isGuardSatisfied(item.getGuard());
			} else {
				if (preconditions.apply(item)) {
					return isGuardSatisfied(item.getGuard());
				} else {
					return false;
				}
			}
		}
	};

	/**
	 * @param executor
	 * @param vertex
	 */
	public SimpleGuardEvaluator(InstanceExecutor executor, Vertex vertex) {
		this.executor = executor;
		this.vertex = vertex;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dk.dtu.imm.esculapauml.core.executors.guards.GuardEvaluator#
	 * getTransitionsWithEnabledGuards()
	 */
	@Override
	public EList<Transition> getTransitionsWithEnabledGuards() {
		return new BasicEList<Transition>(filter(satisfied, vertex.getOutgoings()));
	}

	/**
	 * Check if guard is satisfied
	 * 
	 * @param guard
	 * @return
	 */
	protected boolean isGuardSatisfied(Constraint guard) {
		if (null == guard) {
			// no guard
			return true;
		}
		Validator validator = ValidatorsFactory.getInstance().getValidatorFor(executor, guard);
		if (null == validator) {
			// we do not have validator for this type of constraint
			executor.getChecker().addOtherProblem(Diagnostic.WARNING, "Guard on the transition is not supported by EsculapaUML.", guard.getOwner());
			return true;
		}
		return validator.validateConstraint();
	}

	
	/* (non-Javadoc)
	 * @see dk.dtu.imm.esculapauml.core.executors.guards.GuardEvaluator#getPreconditions()
	 */
	public Predicate<Transition> getPreconditions() {
		return preconditions;
	}

	
	/* (non-Javadoc)
	 * @see dk.dtu.imm.esculapauml.core.executors.guards.GuardEvaluator#setPreconditions(ch.lambdaj.function.matcher.Predicate)
	 */
	public void setPreconditions(Predicate<Transition> preconditions) {
		this.preconditions = preconditions;
	}

}
