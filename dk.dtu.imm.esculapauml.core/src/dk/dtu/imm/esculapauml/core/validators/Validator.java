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

/**
 * Interface used by all types of validators.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public interface Validator {
	/**
	 * Validates a constraint and returns boolean result.
	 * 
	 * @return
	 */
	boolean validateConstraint();

	/**
	 * Gets the validated constraint.
	 * 
	 * @return
	 */
	Constraint getConstraint();

	/**
	 * Sets custom error context (if needed).
	 * 
	 * @return
	 */
	void setErrorContext(Element errorContext);
}
