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

import java.util.Collection;

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Pseudostate;
import org.eclipse.uml2.uml.PseudostateKind;
import org.eclipse.uml2.uml.Region;
import org.eclipse.uml2.uml.UMLPackage.Literals;

import dk.dtu.imm.esculapauml.core.states.SystemState;
import static ch.lambdaj.Lambda.*;
import static org.hamcrest.Matchers.*;

/**
 * @author Piotr J. Puczynski
 * 
 */
public class RegionChecker extends AbstractChecker<Region> {

	/**
	 * @param systemState
	 * @param existingDiagnostics
	 * @param objectToCheck
	 */
	public RegionChecker(SystemState systemState, BasicDiagnostic existingDiagnostics, Region objectToCheck) {
		super(systemState, existingDiagnostics, objectToCheck);
	}

	/**
	 * @param systemState
	 * @param objectToCheck
	 */
	public RegionChecker(SystemState systemState, Region objectToCheck) {
		super(systemState, objectToCheck);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dk.dtu.imm.esculapauml.core.checkers.CheckerInterface#check()
	 */
	@Override
	public void check() {
		checkInitial();

	}

	/**
	 * Checks if region has only one initial pseudostate
	 */
	protected void checkInitial() {
		Collection<Pseudostate> pseudostates = EcoreUtil.getObjectsByType(checkee.getSubvertices(), Literals.PSEUDOSTATE);
		if (filter(having(on(Pseudostate.class).getKind(), equalTo(PseudostateKind.INITIAL_LITERAL)), pseudostates).size() != 1) {
			addProblem(Diagnostic.ERROR, "The Region \"" + checkee.getLabel() + "\" has no initial pseudostate o has more than one initial pseudostates.");
		}
	}

}
