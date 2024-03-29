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
package dk.dtu.imm.esculapauml.core.tests.uml.stateMachines;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Interaction;
import org.junit.Test;

import dk.dtu.imm.esculapauml.core.checkers.UseCaseChecker;
import dk.dtu.imm.esculapauml.core.tests.uml.LoggingTest;
import dk.dtu.imm.esculapauml.core.tests.utils.TestUtils;

/**
 * 
 * Check for possibility of using more than one trigger on transition. It is
 * checked in two separate use cases calling different operation on the same
 * class.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class TwoTriggersTest extends LoggingTest {
	private Resource model = TestUtils.getUMLResource("TwoTriggers.uml");

	@Test
	public void okInteraction() {
		Interaction interaction = TestUtils.getInteraction(model, "UseCase1Detail");
		Interaction interaction2 = TestUtils.getInteraction(model, "UseCase2Detail");
		assertNotNull(interaction);
		assertNotNull(interaction2);
		UseCaseChecker checker = new UseCaseChecker(interaction);
		checker.check();
		Diagnostic diagnostics = checker.getDiagnostics();
		// no errors
		assertEquals(Diagnostic.OK, diagnostics.getSeverity());
		checker = new UseCaseChecker(interaction2);
		checker.check();
		diagnostics = checker.getDiagnostics();
		// no errors
		assertEquals(Diagnostic.OK, diagnostics.getSeverity());
	}
}
