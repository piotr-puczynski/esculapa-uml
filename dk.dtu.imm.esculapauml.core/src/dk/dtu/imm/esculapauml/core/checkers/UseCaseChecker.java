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

import org.eclipse.uml2.uml.Actor;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageSort;
import org.eclipse.uml2.uml.Type;

import dk.dtu.imm.esculapauml.core.states.SystemState;
import dk.dtu.imm.esculapauml.core.utils.MessageUtils;

/**
 * @author Piotr. J. Puczynski (piotr.puczynski)
 * 
 */
public class UseCaseChecker extends AbstractInteractionChecker implements ExecutorInterface {
	
	private Message currentMessage;

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
		execute();
		
		printOutInteraction();
		// System.out.println("First: " + getFirstMessage().toString());

	}
	
	/* (non-Javadoc)
	 * @see dk.dtu.imm.esculapauml.core.executors.ExecutorInterface#execute()
	 */
	@Override
	public void execute() {
		currentMessage = getFirstMessage();
		//execute all messages
		while(null != currentMessage) {
			if (executeMessage(currentMessage)) {
				currentMessage = getNextMessage(currentMessage);
			} else {
				//TODO: some error here
				break;
			}
			
		}
		
	}
	
	/**
	 * Executes one message.
	 * @param message
	 * @return
	 */
	private boolean executeMessage(Message message) {
		if(message == currentMessage) {
			Type target = MessageUtils.getMessageTargetType(message);
			if ((target instanceof Actor) && (message.getMessageSort() != MessageSort.REPLY_LITERAL)) {
				//calling actor, generate an error
				return false;
			}
		}
		return false;
	}
	
	

}
