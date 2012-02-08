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

import org.apache.log4j.Logger;

import dk.dtu.imm.esculapauml.core.checkers.BehaviorChecker;

/**
 * @author Piotr J. Puczynski
 *
 */
public class BehaviorExecutor extends AbstractStateMachineExecutor<BehaviorChecker> {
	
	

	/**
	 * @param checker
	 */
	public BehaviorExecutor(BehaviorChecker checker, String name) {
		super(checker);
		instanceName = name;
		logger = Logger.getLogger(BehaviorExecutor.class);
		logger.debug(checkee.getLabel()  + "[" + instanceName + "]: executor created");
	}

	/* (non-Javadoc)
	 * @see dk.dtu.imm.esculapauml.core.executors.AbstractStateMachineExecutor#prepare()
	 */
	@Override
	public void prepare() {
		//set the specification to the classifier
		instanceSpecification.getClassifiers().add(checker.getCheckedObject().getContext());
		instanceSpecification.setName(instanceName);
		//add to instance package
		checker.getSystemState().getInstancePackage().getPackagedElements().add(instanceSpecification);
		checker.getSystemState().addGeneratedElement(instanceSpecification);
		
		super.prepare();
		logger.debug(checkee.getLabel()  + "[" + instanceName + "]: executor prepared");
	}
	
	

}
