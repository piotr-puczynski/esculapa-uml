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

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.uml2.uml.InstanceSpecification;
import org.eclipse.uml2.uml.LiteralString;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.ValueSpecification;

import dk.dtu.imm.esculapauml.core.checkers.BehaviorChecker;
import dk.dtu.imm.esculapauml.core.sal.parser.ParseException;
import dk.dtu.imm.esculapauml.core.sal.parser.SALNode;
import dk.dtu.imm.esculapauml.core.sal.parser.SALParser;
import dk.dtu.imm.esculapauml.core.sal.parser.SALParserTreeConstants;

/**
 * Executor to execute SAL statements as OpaqueBehavior in Effects of
 * transitions.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class OpaqueBehaviorExecutor extends AbstractInstanceExecutor<BehaviorChecker> {

	private String behavior;
	private EObject owner;
	private SALNode root;
	ValueSpecification reply = null;

	/**
	 * @return the reply
	 */
	public ValueSpecification getReply() {
		return reply;
	}

	/**
	 * @param checker
	 */
	public OpaqueBehaviorExecutor(BehaviorChecker checker, InstanceSpecification instanceSpecification, EObject owner, String behavior) {
		super(checker, instanceSpecification);
		this.owner = owner;
		if (null != behavior) {
			this.behavior = behavior;
		} else {
			this.behavior = "";
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dk.dtu.imm.esculapauml.core.executors.ExecutorInterface#prepare()
	 */
	@Override
	public void prepare() {
		SALParser parser = new SALParser(behavior);
		try {
			root = parser.parse();
		} catch (ParseException e) {
			checker.addOtherProblem(Diagnostic.ERROR, "[SAL] Parse error: " + e.getMessage(), owner);
		}
	}

	public void execute() {
		for (int i = 0; i < root.jjtGetNumChildren() && !checker.hasErrors(); ++i) {
			executeNode(root.getChild(i));
		}
	}

	protected void executeNode(SALNode node) {
		switch (node.getId()) {
		case SALParserTreeConstants.JJTREPLYSTATEMENT:
			executeReply(node);
			break;
		default:
			checker.addOtherProblem(Diagnostic.ERROR, "[SAL] Statement not allowed here:  " + node.toString(), owner);
		}
	}

	protected void executeReply(SALNode node) {
		reply = evaluateExpression(node.getChild(0));
	}

	protected ValueSpecification evaluateExpression(SALNode node) {
		ValueSpecification result = null;
		switch (node.getId()) {
		case SALParserTreeConstants.JJTLOGICCONSTANT:
			result = UMLFactory.eINSTANCE.createLiteralString();
			((LiteralString)result).setValue(String.valueOf(node.jjtGetValue()));

			break;
		}
		return result;
	}

}
