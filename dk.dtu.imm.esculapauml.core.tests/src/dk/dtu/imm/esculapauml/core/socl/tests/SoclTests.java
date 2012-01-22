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
package dk.dtu.imm.esculapauml.core.socl.tests;

import static org.junit.Assert.*;

import org.junit.Test;
import dk.dtu.imm.esculapauml.core.socl.parser.*;

/**
 * @author Piotr J. Puczynski
 * 
 */
public class SoclTests {

	@Test
	public void test() throws ParseException {
		SOCLNode root = null;
		SOCLParser parser = new SOCLParser("hello( hi, howS); hi");
		// invoke it via its topmost production
		// and get a parse tree back

		root = parser.parse();
		root.dump("");
	}

}
