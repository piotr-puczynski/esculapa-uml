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
import org.eclipse.uml2.uml.Transition;
import org.junit.Test;

import dk.dtu.imm.esculapauml.core.checkers.UseCaseChecker;
import dk.dtu.imm.esculapauml.core.tests.uml.LoggingTest;
import dk.dtu.imm.esculapauml.core.tests.utils.TestUtils;

/**
 * Test the situation when two reply statements are on one transition in one
 * effect. The checker should give a warning and the last statement should
 * provide returned reply.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class DoubleReplyTest extends LoggingTest {
	private Resource model = TestUtils.getUMLResource("DoubleReply.uml");
	private Resource referenceModel = TestUtils.getUMLResource("results/DoubleReply.uml");

	@Test
	public void extendInteraction() throws InterruptedException {
		Interaction interaction = TestUtils.getInteraction(model, "UseCase1Detail");
		assertNotNull(interaction);
		UseCaseChecker checker = new UseCaseChecker(interaction);
		checker.check();
		Diagnostic diagnostics = checker.getDiagnostics();
		// there is no error
		assertEquals(Diagnostic.WARNING, diagnostics.getSeverity());
		// models have no differences
		assertTrue(TestUtils.modelsHaveNoDifferences(model, referenceModel));

		// we have a test transition
		Transition transition = TestUtils.getTransitionByName(model, "testTransition");
		assertNotNull(transition);
		// behavior as name
		assertEquals("reply 100; reply 304;", transition.getEffect().getName());
		assertTrue(TestUtils.diagnosticExists(diagnostics, Diagnostic.WARNING, "Reply statement used more than once for a trigger.", transition));
	}
}
