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
package dk.dtu.imm.esculapauml.core.tests.uml.sequence;

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
 * Test for warning in case state machine is not able to accept call event.
 * Event is lost.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class Simple06Test extends LoggingTest {

	private Resource model = TestUtils.getUMLResource("Simple06.uml");

	@Test
	public void eventLost() {
		Interaction interaction = TestUtils.getInteraction(model, "UseCase1Detail");
		assertNotNull(interaction);
		UseCaseChecker checker = new UseCaseChecker(interaction);
		checker.check();
		Diagnostic diagnostic = checker.getDiagnostics();
		// there is an error
		assertEquals(Diagnostic.ERROR, diagnostic.getSeverity());
		assertEquals(1, TestUtils.getDiagnosticErrorsAndWarnings(diagnostic).size());
		// a error is...
		// TestUtils.printDiagnostic(diagnostic);
		assertTrue(TestUtils.diagnosticMessageExists(diagnostic, Diagnostic.ERROR, "Instance 'testInstance' is not ready to respond to the event 's'."));
	}

}
