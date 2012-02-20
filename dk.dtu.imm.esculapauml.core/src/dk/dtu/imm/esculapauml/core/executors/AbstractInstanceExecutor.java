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

import org.eclipse.uml2.uml.InstanceSpecification;
import org.eclipse.uml2.uml.UMLFactory;

import dk.dtu.imm.esculapauml.core.checkers.Checker;

/**
 * Executes instances.
 * @author Piotr J. Puczynski
 *
 */
public abstract class AbstractInstanceExecutor extends AbstractExecutor implements InstanceExecutor {
	
	protected InstanceSpecification instanceSpecification;
	protected String instanceName;

	/**
	 * @param checker
	 */
	public AbstractInstanceExecutor(Checker checker) {
		super(checker);
		instanceSpecification = UMLFactory.eINSTANCE.createInstanceSpecification();
	}
	
	/**
	 * @param checker
	 */
	public AbstractInstanceExecutor(Checker checker, InstanceSpecification instanceSpecification) {
		super(checker);
		this.instanceSpecification = instanceSpecification;
	}

	/* (non-Javadoc)
	 * @see dk.dtu.imm.esculapauml.core.executors.InstanceExecutor#getInstanceSpecification()
	 */
	public InstanceSpecification getInstanceSpecification() {
		return instanceSpecification;
	}

	/* (non-Javadoc)
	 * @see dk.dtu.imm.esculapauml.core.executors.InstanceExecutor#getInstanceName()
	 */
	public String getInstanceName() {
		return instanceName;
	}
	

}
