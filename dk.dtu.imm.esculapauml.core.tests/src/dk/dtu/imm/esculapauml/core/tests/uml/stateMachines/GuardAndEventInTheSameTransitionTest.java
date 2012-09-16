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
import static org.junit.Assert.assertTrue;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Interaction;
import org.junit.Test;

import dk.dtu.imm.esculapauml.core.checkers.UseCaseChecker;
import dk.dtu.imm.esculapauml.core.tests.uml.LoggingTest;
import dk.dtu.imm.esculapauml.core.tests.utils.TestUtils;

/**
 * Test of the transition that calculates based on the event argument in the
 * same transition. In this case the guard evaluates to false and the machine
 * couldn't respond to an event.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class GuardAndEventInTheSameTransitionTest extends LoggingTest {
	private Resource model = TestUtils.getUMLResource("GuardAndEventInTheSameTransition.uml");

	@Test
	public void guardAndEventInTheSameTransition() throws InterruptedException {
		Interaction interaction = TestUtils.getInteraction(model, "UseCase1Detail");
		assertNotNull(interaction);
		UseCaseChecker checker = new UseCaseChecker(interaction);
		checker.check();
		Diagnostic diagnostics = checker.getDiagnostics();
		TestUtils.printDiagnostic(diagnostics);
		// there is an error
		assertEquals(Diagnostic.ERROR, diagnostics.getSeverity());
		assertEquals(1, TestUtils.getDiagnosticErrorsAndWarnings(diagnostics).size());
		// error is
		assertTrue(TestUtils.diagnosticMessageExists(diagnostics, Diagnostic.ERROR, "Instance 'Lifeline2' is not ready to respond to the event 'm'."));
	}
}
