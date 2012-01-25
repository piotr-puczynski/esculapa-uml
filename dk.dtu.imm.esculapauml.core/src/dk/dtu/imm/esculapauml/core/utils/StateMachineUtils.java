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
package dk.dtu.imm.esculapauml.core.utils;

import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static org.hamcrest.Matchers.equalTo;

import java.util.Collection;
import java.util.List;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Pseudostate;
import org.eclipse.uml2.uml.PseudostateKind;
import org.eclipse.uml2.uml.Region;
import org.eclipse.uml2.uml.UMLPackage.Literals;

/**
 * @author Piotr J. Puczynski
 *
 */
public final class StateMachineUtils {
	public static Pseudostate getInitial(Region region) {
		Collection<Pseudostate> pseudostates = EcoreUtil.getObjectsByType(region.getSubvertices(), Literals.PSEUDOSTATE);
		List<Pseudostate> initials = filter(having(on(Pseudostate.class).getKind(), equalTo(PseudostateKind.INITIAL_LITERAL)), pseudostates);
		assert initials.size() == 1;
		return initials.get(0);
	}
}
