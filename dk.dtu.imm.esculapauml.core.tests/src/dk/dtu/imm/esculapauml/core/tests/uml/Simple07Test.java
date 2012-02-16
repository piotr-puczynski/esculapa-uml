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
import static org.junit.Assert.assertTrue;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.Transition;
import org.junit.Test;

import dk.dtu.imm.esculapauml.core.checkers.UseCaseChecker;
import dk.dtu.imm.esculapauml.core.tests.utils.TestUtils;

/**
 * Test for conflicting transitions.
 * @author Piotr J. Puczynski
 *
 */
public class Simple07Test extends LoggingTest {
	
	private Resource model = TestUtils.getUMLResource("Simple07.uml");
	
	@Test
	public void conflictingTransitions() {
		Interaction interaction = TestUtils.getInteraction(model, "UseCase1Detail");
		assertNotNull(interaction);
		//get two conflicting transitions
		Transition t1 = TestUtils.getTransitionByName(model, "conflict1");
		Transition t2 = TestUtils.getTransitionByName(model, "conflict2");
		assertNotNull(t1);
		assertNotNull(t2);
		
		UseCaseChecker checker = new UseCaseChecker(interaction);
		checker.check();
		Diagnostic diagnostics = checker.getDiagnostics();
		// there is an error
		assertEquals(Diagnostic.ERROR, diagnostics.getSeverity());
		// there are errors
		assertEquals(2, TestUtils.getDiagnosticErrorsAndWarnings(diagnostics).size());
		// the errors are
		// TestUtils.printDiagnostic(diagnostics);
		assertTrue(TestUtils.diagnosticMessageExists(diagnostics, Diagnostic.ERROR, "StateMachine instance \"testInstance\" contains conflicting transitions and cannot process an event \"s\"."));
		assertTrue(TestUtils.diagnosticExists(diagnostics, Diagnostic.ERROR, "Conflicting transitions.", t2, t1));
	}

}
