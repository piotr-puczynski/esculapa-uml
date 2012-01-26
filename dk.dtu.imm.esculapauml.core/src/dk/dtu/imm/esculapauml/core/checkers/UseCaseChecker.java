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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dk.dtu.imm.esculapauml.core.checkers.CheckerInterface#check()
	 */
	@Override
	public void check() {
		checkLifelines();
		checkMessages();
		if (hasErrors()) { // there are static errors
			return;
		}

		// if not we can execute
		executor.prepare();
		executor.execute();
		
		printOutInteraction();
		// System.out.println("First: " + getFirstMessage().toString());

	}
	
	
	

}
