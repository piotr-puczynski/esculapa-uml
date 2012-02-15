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

import dk.dtu.imm.esculapauml.core.checkers.BehaviorChecker;
import dk.dtu.imm.esculapauml.core.sal.parser.ParseException;
import dk.dtu.imm.esculapauml.core.sal.parser.SALNode;
import dk.dtu.imm.esculapauml.core.sal.parser.SALParser;

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
			checker.addOtherProblem(Diagnostic.ERROR, "SAL parse error: " + e.getMessage(), owner);
		}
	}

}
