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
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Pseudostate;
import org.eclipse.uml2.uml.PseudostateKind;
import org.eclipse.uml2.uml.Region;
import org.eclipse.uml2.uml.Transition;
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
		logger = Logger.getLogger(RegionChecker.class);
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
		logger.debug(checkee.getLabel() +": start check");
		checkInitial();

	}

	/**
	 * Checks if region has only one initial pseudostate.
	 * Checks if initial is correct.
	 */
	protected void checkInitial() {
		Collection<Pseudostate> pseudostates = EcoreUtil.getObjectsByType(checkee.getSubvertices(), Literals.PSEUDOSTATE);
		List<Pseudostate> initials = filter(having(on(Pseudostate.class).getKind(), equalTo(PseudostateKind.INITIAL_LITERAL)), pseudostates);
		if (initials.size() != 1) {
			addProblem(Diagnostic.ERROR, "The Region \"" + checkee.getLabel() + "\" has no initial pseudostate o has more than one initial pseudostates.");
		} else {
			//An initial vertex can have at most one outgoing transition.
			Pseudostate initial = initials.get(0);
			if(initial.getOutgoings().size() > 1) {
				addOtherProblem(Diagnostic.ERROR, "More than one outgoing transitions from initial state in region \"" + checkee.getLabel() + "\".", initial);
			} else {
				if(initial.getOutgoings().size() == 1) {
					//The outgoing transition from an initial vertex may have a behavior, but not a trigger or guard.
					Transition out = initial.getOutgoings().get(0);
					if(out.getTriggers().size() > 0) {
						addOtherProblem(Diagnostic.ERROR, "Triggers declared on outgoing transition from initial state in region \"" + checkee.getLabel() + "\".", out);
					}
					if(out.getGuard() != null) {
						addOtherProblem(Diagnostic.ERROR, "Guard declared on outgoing transition from initial state in region \"" + checkee.getLabel() + "\".", out);
					}
				}
			}
			
			
		}
	}

}
