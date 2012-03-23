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
import org.eclipse.uml2.uml.ProtocolStateMachine;

/**
 * Checks the protocol state machines. This class is a little relaxed from
 * syntactical UML notation. We do not require only protocol transitions in
 * protocol. The reason is some tools do not support them.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class ProtocolStateMachineChecker extends AbstractStateMachineChecker<ProtocolStateMachine> {

	/**
	 * @param checker
	 * @param objectToCheck
	 */
	ProtocolStateMachineChecker(Checker checker, ProtocolStateMachine objectToCheck) {
		super(checker, objectToCheck);
		logger = Logger.getLogger(ProtocolStateMachineChecker.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dk.dtu.imm.esculapauml.core.checkers.Checker#check()
	 */
	@Override
	public void check() {
		logger.debug(checkee.getLabel() + ": start check");
		checkRegions();
	}

}
