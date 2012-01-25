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
 * Tests of the SOCL parser
 * @author Piotr J. Puczynski
 * 
 */
public class SoclTests {

	@Test
	public void parseSemicolons() throws ParseException {
		SOCLParser parser = new SOCLParser("fnc1; fnc2");
		

		SOCLNode root = parser.parse();
		//root.dump("");
		
		assertNotNull(root);
		assertEquals(2, root.jjtGetNumChildren());
		
		SOCLNode fnc1 = (SOCLNode) root.jjtGetChild(0);
		SOCLNode fnc2 = (SOCLNode) root.jjtGetChild(1);
		assertEquals(SOCLParserTreeConstants.JJTIDENTIFIER, fnc1.getId());
		assertEquals(SOCLParserTreeConstants.JJTIDENTIFIER, fnc2.getId());
		assertEquals("fnc1", fnc1.jjtGetValue());
		assertEquals("fnc2", fnc2.jjtGetValue());
		
	}
	
	@Test
	public void parseSemicolonsIgnoreLast() throws ParseException {
		SOCLParser parser = new SOCLParser("fnc1; fnc2;");
		

		SOCLNode root = parser.parse();
		//root.dump("");
		
		assertNotNull(root);
		assertEquals(2, root.jjtGetNumChildren());
		
		SOCLNode fnc1 = (SOCLNode) root.jjtGetChild(0);
		SOCLNode fnc2 = (SOCLNode) root.jjtGetChild(1);
		assertEquals(SOCLParserTreeConstants.JJTIDENTIFIER, fnc1.getId());
		assertEquals(SOCLParserTreeConstants.JJTIDENTIFIER, fnc2.getId());
		assertEquals("fnc1", fnc1.jjtGetValue());
		assertEquals("fnc2", fnc2.jjtGetValue());
		
	}
	
	@Test
	public void parseMember() throws ParseException {
		SOCLParser parser = new SOCLParser("obj1.member2");
		

		SOCLNode root = parser.parse();
		//root.dump("");
		
		assertNotNull(root);
		assertEquals(1, root.jjtGetNumChildren());
		
		SOCLNode member = (SOCLNode) root.jjtGetChild(0);
		assertEquals(SOCLParserTreeConstants.JJTMEMEBEROP, member.getId());
		assertEquals(2, member.jjtGetNumChildren());
		
		SOCLNode obj1 = (SOCLNode) member.jjtGetChild(0);
		SOCLNode member2 = (SOCLNode) member.jjtGetChild(1);
		
		assertEquals(SOCLParserTreeConstants.JJTIDENTIFIER, obj1.getId());
		assertEquals(SOCLParserTreeConstants.JJTIDENTIFIER, member2.getId());
		assertEquals("obj1", obj1.jjtGetValue());
		assertEquals("member2", member2.jjtGetValue());
		
	}
	
	@Test
	public void parseMemberFunction() throws ParseException {
		SOCLParser parser = new SOCLParser("obj1.member2(123, \"str\")");
		

		SOCLNode root = parser.parse();
		//root.dump("");
		
		assertNotNull(root);
		assertEquals(1, root.jjtGetNumChildren());
		
		SOCLNode member = (SOCLNode) root.jjtGetChild(0);
		assertEquals(SOCLParserTreeConstants.JJTMEMEBEROP, member.getId());
		assertEquals(2, member.jjtGetNumChildren());
		
		SOCLNode obj1 = (SOCLNode) member.jjtGetChild(0);
		SOCLNode member2 = (SOCLNode) member.jjtGetChild(1);
		
		assertEquals(SOCLParserTreeConstants.JJTIDENTIFIER, obj1.getId());
		assertEquals(SOCLParserTreeConstants.JJTCALL, member2.getId());
		assertEquals("obj1", obj1.jjtGetValue());
		SOCLNode ident = (SOCLNode) member2.jjtGetChild(0);
		SOCLNode params = (SOCLNode) member2.jjtGetChild(1);
		assertEquals(SOCLParserTreeConstants.JJTIDENTIFIER, ident.getId());
		assertEquals(SOCLParserTreeConstants.JJTPARAMETERS, params.getId());
		
		assertEquals(2, params.jjtGetNumChildren());
		SOCLNode num = (SOCLNode) params.jjtGetChild(0);
		SOCLNode str = (SOCLNode) params.jjtGetChild(1);
		assertEquals(SOCLParserTreeConstants.JJTINTEGERCONSTANT, num.getId());
		assertEquals(SOCLParserTreeConstants.JJTSTRINGCONSTANT, str.getId());
		assertEquals(123, num.jjtGetValue());
		assertEquals("str", str.jjtGetValue());
		
	}
	
	@Test
	public void returnTest() throws ParseException {
		SOCLParser parser = new SOCLParser("return true;");
		

		SOCLNode root = parser.parse();
		//root.dump("");
		
		assertNotNull(root);
		assertEquals(1, root.jjtGetNumChildren());
		
		SOCLNode member = (SOCLNode) root.jjtGetChild(0);
		assertEquals(SOCLParserTreeConstants.JJTRETURNSTATEMENT, member.getId());
		assertEquals(1, member.jjtGetNumChildren());
		
		SOCLNode obj = (SOCLNode) member.jjtGetChild(0);
		
		assertEquals(SOCLParserTreeConstants.JJTLOGICCONSTANT, obj.getId());
		assertEquals(true, obj.jjtGetValue());
		
	}
	
	

}