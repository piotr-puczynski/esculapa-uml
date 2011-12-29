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

import org.eclipse.emf.ecore.resource.Resource;
import org.junit.Test;

import dk.dtu.imm.esculapauml.core.checkers.UseCaseChecker;
import dk.dtu.imm.esculapauml.core.tests.utils.TestUtils;

/**
 * @author Piotr. J. Puczynski (piotr.puczynski)
 *
 */
public class Simple1Test {

	@Test
	public void emptyInteraction() {
		Resource model = TestUtils.getUMLResource("Simple1.uml");

		//UseCaseChecker checker = new UseCaseChecker();
	}

}
