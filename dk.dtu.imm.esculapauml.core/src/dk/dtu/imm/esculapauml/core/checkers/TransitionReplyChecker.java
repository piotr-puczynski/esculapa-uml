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
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterDirectionKind;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.ValueSpecification;

/**
 * Class used to exchange a result of firing a transition and check if a reply
 * for an operation has been specified correctly.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class TransitionReplyChecker extends AbstractChecker<Transition> {
	private ValueSpecification reply = null;
	private Type returnType = null;
	private Operation operation;
	private boolean allowedToHaveReply = true;
	private boolean acceptReplies = true;

	/**
	 * @param transition
	 */
	public TransitionReplyChecker(Checker checker, Transition transition, Operation operation) {
		super(checker, transition);
		this.operation = operation;
		if (null != operation) {
			calculateOperationReturnType();
		}
	}

	/**
	 * Calculates what is the type of result of operation.
	 */
	private void calculateOperationReturnType() {
		for (Parameter parameter : operation.getOwnedParameters()) {
			if (parameter.getDirection() == ParameterDirectionKind.RETURN_LITERAL) {
				returnType = parameter.getType();
				return;
			}
		}

	}

	/**
	 * Gets the reply. This should be called only when there is a reply already
	 * generated.
	 * 
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
			if (!isAcceptingReplies()) {
				addProblem(Diagnostic.WARNING, "Reply will be ignored. It was used in the context of asynchronous call.");
				return;
			}
			if (null != this.reply) {
				// duplication of reply for one operation
				addProblem(Diagnostic.WARNING, "Reply statement used more than once for a trigger.");
			}

			if (null != operation) {
				if (null == returnType) {
					// reply was issued for operation that do not have return
					// value
					addProblem(Diagnostic.ERROR, "Failed to assign return value for operation '" + operation.getName() + "' to value of type: "
							+ reply.getType().getName() + ". The operation is declared without result value.");
					return;
				} else {
					if (!returnType.conformsTo(reply.getType())) {
						// the reply failed type check
						addProblem(Diagnostic.ERROR, "Type check failed when trying to assign return value for operation '" + operation.getName()
								+ "' to value of type: " + reply.getType().getName() + ". Required type must conform to: " + returnType.getName() + ".");
						return;
					}
				}
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

	/**
	 * @return the operation
	 */
	public Operation getOperation() {
		return operation;
	}

	/**
	 * @return the acceptReplies
	 */
	public boolean isAcceptingReplies() {
		return acceptReplies;
	}

	/**
	 * @param acceptReplies
	 *            the acceptReplies to set
	 */
	public void setAcceptReplies(boolean acceptReplies) {
		this.acceptReplies = acceptReplies;
	}
}
