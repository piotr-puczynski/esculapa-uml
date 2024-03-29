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
 * 
 * Message without operation specified
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class Simple03Test extends LoggingTest {
	private Resource model = TestUtils.getUMLResource("Simple03.uml");

	@Test
	public void noOperationSpecified() {
		Interaction interaction = TestUtils.getInteraction(model, "UseCase1Detail");
		assertNotNull(interaction);
		UseCaseChecker checker = new UseCaseChecker(interaction);
		checker.check();
		Diagnostic diagnostic = checker.getDiagnostics();
		// there is an error
		assertEquals(Diagnostic.ERROR, diagnostic.getSeverity());
		assertEquals(1, TestUtils.getDiagnosticErrorsAndWarnings(diagnostic).size());
		// an error is...
		assertTrue(TestUtils.diagnosticMessageExists(diagnostic, Diagnostic.ERROR, "The message 'Message1' has no operation set."));
	}
}
