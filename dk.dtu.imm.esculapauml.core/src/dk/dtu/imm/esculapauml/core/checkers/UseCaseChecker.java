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
package dk.dtu.imm.esculapauml.core.checkers;

import org.apache.log4j.Logger;
import org.eclipse.uml2.uml.Interaction;

import dk.dtu.imm.esculapauml.core.executors.UseCaseExecutor;
import dk.dtu.imm.esculapauml.core.states.SystemState;

/**
 * @author Piotr. J. Puczynski (piotr.puczynski)
 * 
 */
public class UseCaseChecker extends AbstractInteractionChecker {

	protected UseCaseExecutor executor = new UseCaseExecutor(this);

	public UseCaseChecker(Interaction interaction) {
		super(new SystemState(), interaction);
		logger = Logger.getLogger(UseCaseChecker.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dk.dtu.imm.esculapauml.core.checkers.CheckerInterface#check()
	 */
	@Override
	public void check() {
		logger.info(checkee.getLabel() + ": starting use case check");
		systemState.prepare(this);
		checkComponents();
		checkExistingInstances();
		checkLifelines();
		checkMessages();
		if (hasErrors()) { // there are static errors
			logger.warn(checkee.getLabel() + ": use case has errors, execution is not started");
			return;
		}

		// if not we can execute
		logger.info(checkee.getLabel() + ": execution preparation");
		executor.prepare();
		logger.info(checkee.getLabel() + ": starting execution");
		executor.execute();
		logger.info(checkee.getLabel() + ": model execution finished");
		// printOutInteraction();
		executor.getSequencer().printSequence();

	}

}
