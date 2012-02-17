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

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.ValueSpecification;

/**
 * Class used to exchange a result of firing a transition and check if a reply
 * has been specified correctly.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class TransitionReplyChecker extends AbstractChecker<Transition> {
	private ValueSpecification reply = null;
	private boolean allowedToHaveReply = true;

	/**
	 * @param transition
	 */
	public TransitionReplyChecker(Checker checker, Transition transition) {
		super(checker, transition);
	}

	/**
	 * @return the reply
	 */
	public ValueSpecification getReply() {
		return reply;
	}

	public void setNextTransition(Transition transition) {
		checkee = transition;
	}

	/**
	 * @param reply
	 *            the reply to set
	 */
	public void setReply(ValueSpecification reply) {
		if (isAllowedToHaveReply()) {
			if (null != this.reply) {
				addProblem(Diagnostic.WARNING, "Reply statement used more than once for a trigger.");
			}
			this.reply = reply;
		} else {
			addProblem(Diagnostic.ERROR, "The transition is not allowed to have reply.");
		}

	}

	/**
	 * @return the allowedToHaveReply
	 */
	public boolean isAllowedToHaveReply() {
		return allowedToHaveReply;
	}

	/**
	 * @param allowedToHaveReply
	 *            the allowedToHaveReply to set
	 */
	public void setAllowedToHaveReply(boolean allowedToHaveReply) {
		this.allowedToHaveReply = allowedToHaveReply;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dk.dtu.imm.esculapauml.core.checkers.Checker#check()
	 */
	@Override
	public void check() {
		// nothing to check
	}
}
