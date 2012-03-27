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
package dk.dtu.imm.esculapauml.core.validators;

import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.Element;

import dk.dtu.imm.esculapauml.core.executors.InstanceExecutor;

/**
 * @author Piotr J. Puczynski
 * 
 */
public abstract class AbstractValidator implements Validator {

	protected InstanceExecutor executor;
	protected Constraint constraint;
	protected Element errorContext;

	/**
	 * @param executor
	 * @param constraint
	 */
	public AbstractValidator(InstanceExecutor executor, Constraint constraint) {
		super();
		this.executor = executor;
		this.constraint = constraint;
		// default error context
		errorContext = constraint.getOwner();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dk.dtu.imm.esculapauml.core.validators.Validator#getConstraint()
	 */
	public Constraint getConstraint() {
		return constraint;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.validators.Validator#setErrorContext(org.
	 * eclipse.uml2.uml.Element)
	 */
	public void setErrorContext(Element errorContext) {
		this.errorContext = errorContext;
	}

}
