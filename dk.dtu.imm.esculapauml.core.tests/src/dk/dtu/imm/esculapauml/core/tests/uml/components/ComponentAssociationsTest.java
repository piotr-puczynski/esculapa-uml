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
package dk.dtu.imm.esculapauml.core.tests.uml.components;

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
 * Test of an error in case classes in components have association to class in
 * other component.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class ComponentAssociationsTest extends LoggingTest {
	private Resource model = TestUtils.getUMLResource("ComponentAssociations.uml");

	@Test
	public void componentAssociations() throws InterruptedException {
		Interaction interaction = TestUtils.getInteraction(model, "UseCase1Detail");
		assertNotNull(interaction);
		UseCaseChecker checker = new UseCaseChecker(interaction);
		checker.check();
		Diagnostic diagnostics = checker.getDiagnostics();
		// TestUtils.printDiagnostic(diagnostics);
		// there is an error
		assertEquals(Diagnostic.ERROR, diagnostics.getSeverity());
		assertEquals(3, TestUtils.getDiagnosticErrorsAndWarnings(diagnostics).size());
		// error is
		assertTrue(TestUtils.diagnosticMessageExists(diagnostics, Diagnostic.ERROR,
				"The component 'CompA' has type 'A' that has association to other type 'B' in other component 'CompB'."));

		assertTrue(TestUtils.diagnosticMessageExists(diagnostics, Diagnostic.ERROR,
				"The component 'CompB' has type 'B' that has association to other type 'A' in other component 'CompA'."));

		assertTrue(TestUtils.diagnosticMessageExists(diagnostics, Diagnostic.ERROR,
				"The component 'CompA' has classifier 'A' that has attribute of type 'B' that is located in other component 'CompB'."));
	}
}
