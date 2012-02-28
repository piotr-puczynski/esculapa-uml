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
 * Check for existence of errors when BES do not have start or finish set.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class EmptyStartAndFinishInBESTest extends LoggingTest {
	private Resource model = TestUtils.getUMLResource("EmptyStartAndFinishInBES.uml");

	@Test
	public void emptyMessageInMOC() {
		Interaction interaction = TestUtils.getInteraction(model, "UseCase1Detail");
		assertNotNull(interaction);
		UseCaseChecker checker = new UseCaseChecker(interaction);
		checker.check();
		Diagnostic diagnostics = checker.getDiagnostics();
		// TestUtils.printDiagnostic(diagnostics);

		// there is an error
		assertEquals(Diagnostic.ERROR, diagnostics.getSeverity());
		assertEquals(2, TestUtils.getDiagnosticErrorsAndWarnings(diagnostics).size());
		// error is
		assertTrue(TestUtils.diagnosticMessageExists(diagnostics, Diagnostic.ERROR,
				"BehaviorExecutionSpecification \"BehaviorExecutionSpecification1\" has no start set."));
		assertTrue(TestUtils.diagnosticMessageExists(diagnostics, Diagnostic.ERROR,
				"BehaviorExecutionSpecification \"BehaviorExecutionSpecification1\" has no finish set."));
	}
}