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

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.BasicDiagnostic;

import dk.dtu.imm.esculapauml.core.checkers.Checker;
import dk.dtu.imm.esculapauml.core.states.SystemState;

/**
 * Abstract class for all generators.
 * @author Piotr J. Puczynski
 *
 */
public abstract class AbstractGenerator<T> implements GeneratorInterface<T> {
	protected SystemState systemState;
	protected BasicDiagnostic diagnostic;
	protected T generated = null;
	protected Logger logger = Logger.getLogger(AbstractGenerator.class);
	
	
	/**
	 * @param systemState
	 */
	public AbstractGenerator(SystemState systemState, BasicDiagnostic diagnostic) {
		super();
		this.systemState = systemState;
		this.diagnostic = diagnostic;
	}

	/**
	 * @param checker
	 */
	public AbstractGenerator(Checker checker) {
		super();
		this.systemState = checker.getSystemState();
		this.diagnostic = (BasicDiagnostic) checker.getDiagnostics();
	}
}
