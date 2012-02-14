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
import static org.junit.Assert.assertTrue;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Interaction;
import org.junit.Test;

import dk.dtu.imm.esculapauml.core.checkers.UseCaseChecker;
import dk.dtu.imm.esculapauml.core.tests.utils.TestUtils;

/**
 * Test for generation of part of missing model interaction: three lifelines to
 * generate, three messages to generate four replies to generate.
 * 
 * 
 * Testing message flow and sequence of messages.
 * @author Piotr J. Puczynski
 * 
 */
public class Simple10Test extends LoggingTest {
	private Resource model = TestUtils.getUMLResource("Simple10.uml");
	private Resource referenceModel = TestUtils.getUMLResource("results/Simple10.uml");

	@Test
	public void extendInteraction() throws InterruptedException {
		Interaction interaction = TestUtils.getInteraction(model, "UseCase1Detail");
		UseCaseChecker checker = new UseCaseChecker(interaction);
		checker.check();
		Diagnostic diagnostics = checker.getDiagnostics();
		// there is no error
		assertEquals(Diagnostic.OK, diagnostics.getSeverity());
		// models have no differences
		assertTrue(TestUtils.modelsHaveNoDifferences(model, referenceModel));
	}
}
