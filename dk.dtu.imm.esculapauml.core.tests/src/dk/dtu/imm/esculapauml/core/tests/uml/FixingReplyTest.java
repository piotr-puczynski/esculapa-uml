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
package dk.dtu.imm.esculapauml.core.tests.uml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.Message;
import org.junit.Test;

import dk.dtu.imm.esculapauml.core.checkers.UseCaseChecker;
import dk.dtu.imm.esculapauml.core.tests.utils.TestUtils;
import dk.dtu.imm.esculapauml.core.utils.InteractionUtils;

/**
 * Test checks if the checker correctly fill the operation if the reply was
 * drawn without specifying operation.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class FixingReplyTest extends LoggingTest {
	private Resource model = TestUtils.getUMLResource("FixingReply.uml");
	private Resource referenceModel = TestUtils.getUMLResource("results/FixingReply.uml");

	@Test
	public void extendInteraction() throws InterruptedException {
		Interaction interaction = TestUtils.getInteraction(model, "UseCase1Detail");
		assertNotNull(interaction);
		Message message = interaction.getMessage("m");
		Message reply = interaction.getMessage("Message1");
		assertNotNull(message);
		assertNotNull(reply);

		assertNotNull(InteractionUtils.getMessageOperation(message));
		// reply points to no operation
		assertNull(InteractionUtils.getMessageOperation(reply));
		UseCaseChecker checker = new UseCaseChecker(interaction);
		checker.check();
		Diagnostic diagnostics = checker.getDiagnostics();
		// there is no error
		assertEquals(Diagnostic.OK, diagnostics.getSeverity());
		// models have no differences
		assertTrue(TestUtils.modelsHaveNoDifferences(model, referenceModel));
		// they now point to the same operation
		assertNotNull(InteractionUtils.getMessageOperation(reply));
		assertEquals(InteractionUtils.getMessageOperation(message), InteractionUtils.getMessageOperation(reply));
	}
}
