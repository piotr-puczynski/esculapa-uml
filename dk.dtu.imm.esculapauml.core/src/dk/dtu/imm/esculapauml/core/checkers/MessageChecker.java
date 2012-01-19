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

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageSort;

/**
 * 
 * Checker for interaction messages
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class MessageChecker extends AbstractChecker<Message> {

	/**
	 * @param existingDiagnostics
	 */
	public MessageChecker(BasicDiagnostic existingDiagnostics, Message message) {
		super(existingDiagnostics, message);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dk.dtu.imm.esculapauml.core.checkers.CheckerInterface#check()
	 */
	@Override
	public void check() {
		structuralMessageConformanceCheck();

	}

	/**
	 * Checks if message calls existing operation and with correct parameters
	 */
	protected void structuralMessageConformanceCheck() {
		// we check the message type
		if ((checkee.getMessageSort().getValue() == MessageSort.SYNCH_CALL) || (checkee.getMessageSort().getValue() == MessageSort.ASYNCH_CALL)) {
			checkee.validateSignatureIsOperation(diagnostics, null);
		}
	}

}
