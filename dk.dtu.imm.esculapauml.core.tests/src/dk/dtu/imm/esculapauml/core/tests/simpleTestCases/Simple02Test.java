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
package dk.dtu.imm.esculapauml.core.tests.simpleTestCases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.Message;
import org.junit.Test;

import dk.dtu.imm.esculapauml.core.checkers.UseCaseChecker;
import dk.dtu.imm.esculapauml.core.tests.utils.TestUtils;

/**
 * 
 * Simple scenario containing one interaction with one message / just structural
 * check
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class Simple02Test extends LoggingTest {
	private Resource model = TestUtils.getUMLResource("Simple02.uml");

	@Test
	public void okInteraction() {
		Interaction interaction = TestUtils.getInteraction(model, "UseCase1Detail");
		assertNotNull(interaction);
		UseCaseChecker checker = new UseCaseChecker(interaction);
		checker.check();
		Diagnostic diagnostics = checker.getDiagnostics();
		// no errors
		assertEquals(Diagnostic.OK, diagnostics.getSeverity());
	}

	@Test
	public void hangingMessage() {
		Resource myModel = TestUtils.cloneResource(model);
		Interaction interaction = TestUtils.getInteraction(myModel, "UseCase1Detail");
		// we create message
		final String messageName = "Message100";
		Message empty = interaction.createMessage(messageName);
		assertNotNull(interaction);
		UseCaseChecker checker = new UseCaseChecker(interaction);
		checker.check();
		Diagnostic diagnostics = checker.getDiagnostics();
		// there is an error
		assertEquals(Diagnostic.ERROR, diagnostics.getSeverity());
		// there are more errors
		assertEquals(3, TestUtils.getDiagnosticErrorsAndWarnings(diagnostics).size());
		// the errors are
		assertTrue(TestUtils.diagnosticExists(diagnostics, Diagnostic.ERROR, "The Message \"" + empty.getLabel()
				+ "\" does not have send message occurence specification.", empty));
		assertTrue(TestUtils.diagnosticExists(diagnostics, Diagnostic.ERROR, "The Message \"" + empty.getLabel()
				+ "\" does not have receive message occurence specification.", empty));
		assertTrue(TestUtils.diagnosticExists(diagnostics, Diagnostic.ERROR, "The Message \"" + empty.getLabel() + "\" has no operation set.", empty));
	}
}
