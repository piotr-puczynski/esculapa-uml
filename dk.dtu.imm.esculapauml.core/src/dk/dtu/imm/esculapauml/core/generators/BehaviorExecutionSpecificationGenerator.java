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
package dk.dtu.imm.esculapauml.core.generators;

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.uml2.uml.BehaviorExecutionSpecification;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.UMLFactory;

import dk.dtu.imm.esculapauml.core.states.SystemState;

/**
 * Generates BehaviorExecutionSpecification at specified position on lifeline.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class BehaviorExecutionSpecificationGenerator extends AbstractGenerator<BehaviorExecutionSpecification> {

	public static final int POSITION_BEGINNING = -2;
	public static final int POSITION_END = -1;

	private int position;
	private Lifeline lifeline;

	/**
	 * @param systemState
	 * @param diagnostic
	 */
	public BehaviorExecutionSpecificationGenerator(SystemState systemState, BasicDiagnostic diagnostic, Lifeline owner, int position) {
		super(systemState, diagnostic);
		this.position = position;
		lifeline = owner;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dk.dtu.imm.esculapauml.core.generators.GeneratorInterface#generate()
	 */
	@Override
	public BehaviorExecutionSpecification generate() {
		BehaviorExecutionSpecification result = UMLFactory.eINSTANCE.createBehaviorExecutionSpecification();
		result.setEnclosingInteraction(lifeline.getInteraction());
		result.setName("BehaviorExecutionSpecification");
		switch (position) {
		case POSITION_END:
			lifeline.getCoveredBys().add(result);
			break;
		case POSITION_BEGINNING:
			position = 0;
		default:
			lifeline.getCoveredBys().add(position, result);
		}
		lifeline.getCoveredBys().add(result);
		systemState.addGeneratedElement(result);
		return null;
	}

}
