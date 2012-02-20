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

import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.Element;

import dk.dtu.imm.esculapauml.core.executors.InstanceExecutor;


/**
 * Factory used to create different types of validators.
 * @author Piotr J. Puczynski
 *
 */
public class ValidatorsFactory {
	private static ValidatorsFactory instance = null;
	
	
	protected ValidatorsFactory() {
		super();
	}
	
	public static ValidatorsFactory getInstance() {
		if (instance == null) {
			instance = new ValidatorsFactory();
		}
		return instance;
	}
	
	public Validator getValidatorFor(InstanceExecutor executor, Constraint constraint) {
		if(null == constraint.getSpecification()) {
			return new EmptyConstraintValidator(constraint);
		}
		return null;
	}
	
}
