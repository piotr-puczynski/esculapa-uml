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

import static org.junit.Assert.*;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Interaction;
import org.junit.Test;

import dk.dtu.imm.esculapauml.core.checkers.UseCaseChecker;
import dk.dtu.imm.esculapauml.core.tests.uml.LoggingTest;
import dk.dtu.imm.esculapauml.core.tests.utils.TestUtils;

/**
 * Test for the redefinition of a local variable in state machine instance.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class RedefinitionOfLocalVariableTest extends LoggingTest {
	private Resource model = TestUtils.getUMLResource("RedefinitionOfLocalVariable.uml");
	private Resource referenceModel = TestUtils.getUMLResource("results/RedefinitionOfLocalVariable.uml");

	@Test
	public void redefinitionOfLocalVariableTest() throws InterruptedException {
		Interaction interaction = TestUtils.getInteraction(model, "UseCase1Detail");
		assertNotNull(interaction);
		UseCaseChecker checker = new UseCaseChecker(interaction);
		checker.check();
		Diagnostic diagnostics = checker.getDiagnostics();
		// TestUtils.printDiagnostic(diagnostics);
		// there is no error
		TestUtils.printDiagnostic(diagnostics);
		assertEquals(Diagnostic.OK, diagnostics.getSeverity());

		// models have no differences
		assertTrue(TestUtils.modelsHaveNoDifferences(model, referenceModel));
	}
}
