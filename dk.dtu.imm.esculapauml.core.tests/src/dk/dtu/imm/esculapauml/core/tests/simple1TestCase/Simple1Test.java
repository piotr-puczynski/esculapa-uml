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
package dk.dtu.imm.esculapauml.core.tests.simple1TestCase;

import static org.junit.Assert.*;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.UMLFactory;
import org.junit.Test;

import dk.dtu.imm.esculapauml.core.checkers.UseCaseChecker;
import dk.dtu.imm.esculapauml.core.tests.utils.TestUtils;

/**
 * Simple scenario containing one interaction without messages / just structural
 * check
 * 
 * @author Piotr. J. Puczynski (piotr.puczynski)
 * 
 */
public class Simple1Test {

	private Resource model = TestUtils.getUMLResource("Simple1.uml");

	@Test
	public void emptyInteraction() {
		Interaction interaction = TestUtils.getInteraction(model, "UseCase1Detail");
		assertNotNull(interaction);
		UseCaseChecker checker = new UseCaseChecker(interaction);
		checker.check();
		Diagnostic diagnostics = checker.getDiagnostics();
		// no errors
		assertEquals(Diagnostic.OK, diagnostics.getSeverity());
	}

	@Test
	public void interactionWithLifelineWithoutRepresentant() {
		Interaction interaction = TestUtils.getInteraction(model, "UseCase1Detail");
		assertNotNull(interaction);

		// we add one lifeline without representant to interaction
		String lifelineName = "EmptyClass";
		Lifeline empty = interaction.createLifeline(lifelineName);
		UseCaseChecker checker = new UseCaseChecker(interaction);
		checker.check();
		Diagnostic diagnostics = checker.getDiagnostics();
		// there is an error
		assertEquals(Diagnostic.ERROR, diagnostics.getSeverity());
		// there is one error
		assertEquals(1, TestUtils.getDiagnosticErrorsAndWarnings(diagnostics).size());
		// an error is...
		assertTrue(TestUtils.diagnosticExists(diagnostics, Diagnostic.ERROR, "The Lifeline " + lifelineName + " has no representant.",
				empty));

		// let create a connector element that won't point to any representant
		Property prop = interaction.createOwnedAttribute("MyProperty", UMLFactory.eINSTANCE.createProperty().getType());
		empty.setRepresents(prop);
		checker = new UseCaseChecker(interaction);
		checker.check();
		diagnostics = checker.getDiagnostics();
		// there is an error
		assertEquals(Diagnostic.ERROR, diagnostics.getSeverity());
		// there is one error
		assertEquals(1, TestUtils.getDiagnosticErrorsAndWarnings(diagnostics).size());
		// an error is...
		assertTrue(TestUtils.diagnosticExists(diagnostics, Diagnostic.ERROR, "The Lifeline " + lifelineName + " has no representant set to any type.",
				empty));

	}

}
