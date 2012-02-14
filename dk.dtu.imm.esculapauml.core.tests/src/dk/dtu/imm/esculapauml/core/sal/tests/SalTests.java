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
package dk.dtu.imm.esculapauml.core.sal.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import dk.dtu.imm.esculapauml.core.sal.parser.*;

/**
 * Tests of the SAL parser
 * @author Piotr J. Puczynski
 * 
 */
public class SalTests {

	@Test
	public void parseSemicolons() throws ParseException {
		SALParser parser = new SALParser("fnc1; fnc2");
		

		SALNode root = parser.parse();
		//root.dump("");
		
		assertNotNull(root);
		assertEquals(2, root.jjtGetNumChildren());
		
		SALNode fnc1 = (SALNode) root.jjtGetChild(0);
		SALNode fnc2 = (SALNode) root.jjtGetChild(1);
		assertEquals(SALParserTreeConstants.JJTIDENTIFIER, fnc1.getId());
		assertEquals(SALParserTreeConstants.JJTIDENTIFIER, fnc2.getId());
		assertEquals("fnc1", fnc1.jjtGetValue());
		assertEquals("fnc2", fnc2.jjtGetValue());
		
	}
	
	@Test
	public void parseSemicolonsIgnoreLast() throws ParseException {
		SALParser parser = new SALParser("fnc1; fnc2;");
		

		SALNode root = parser.parse();
		//root.dump("");
		
		assertNotNull(root);
		assertEquals(2, root.jjtGetNumChildren());
		
		SALNode fnc1 = (SALNode) root.jjtGetChild(0);
		SALNode fnc2 = (SALNode) root.jjtGetChild(1);
		assertEquals(SALParserTreeConstants.JJTIDENTIFIER, fnc1.getId());
		assertEquals(SALParserTreeConstants.JJTIDENTIFIER, fnc2.getId());
		assertEquals("fnc1", fnc1.jjtGetValue());
		assertEquals("fnc2", fnc2.jjtGetValue());
		
	}
	
	@Test
	public void parseMember() throws ParseException {
		SALParser parser = new SALParser("obj1.member2");
		

		SALNode root = parser.parse();
		//root.dump("");
		
		assertNotNull(root);
		assertEquals(1, root.jjtGetNumChildren());
		
		SALNode member = (SALNode) root.jjtGetChild(0);
		assertEquals(SALParserTreeConstants.JJTMEMEBEROP, member.getId());
		assertEquals(2, member.jjtGetNumChildren());
		
		SALNode obj1 = (SALNode) member.jjtGetChild(0);
		SALNode member2 = (SALNode) member.jjtGetChild(1);
		
		assertEquals(SALParserTreeConstants.JJTIDENTIFIER, obj1.getId());
		assertEquals(SALParserTreeConstants.JJTIDENTIFIER, member2.getId());
		assertEquals("obj1", obj1.jjtGetValue());
		assertEquals("member2", member2.jjtGetValue());
		
	}
	
	@Test
	public void parseMemberFunction() throws ParseException {
		SALParser parser = new SALParser("obj1.member2(123, \"str\")");
		

		SALNode root = parser.parse();
		//root.dump("");
		
		assertNotNull(root);
		assertEquals(1, root.jjtGetNumChildren());
		
		SALNode member = (SALNode) root.jjtGetChild(0);
		assertEquals(SALParserTreeConstants.JJTMEMEBEROP, member.getId());
		assertEquals(2, member.jjtGetNumChildren());
		
		SALNode obj1 = (SALNode) member.jjtGetChild(0);
		SALNode member2 = (SALNode) member.jjtGetChild(1);
		
		assertEquals(SALParserTreeConstants.JJTIDENTIFIER, obj1.getId());
		assertEquals(SALParserTreeConstants.JJTCALL, member2.getId());
		assertEquals("obj1", obj1.jjtGetValue());
		SALNode ident = (SALNode) member2.jjtGetChild(0);
		SALNode params = (SALNode) member2.jjtGetChild(1);
		assertEquals(SALParserTreeConstants.JJTIDENTIFIER, ident.getId());
		assertEquals(SALParserTreeConstants.JJTPARAMETERS, params.getId());
		
		assertEquals(2, params.jjtGetNumChildren());
		SALNode num = (SALNode) params.jjtGetChild(0);
		SALNode str = (SALNode) params.jjtGetChild(1);
		assertEquals(SALParserTreeConstants.JJTINTEGERCONSTANT, num.getId());
		assertEquals(SALParserTreeConstants.JJTSTRINGCONSTANT, str.getId());
		assertEquals(123, num.jjtGetValue());
		assertEquals("str", str.jjtGetValue());
		
	}
	
	@Test
	public void returnTest() throws ParseException {
		SALParser parser = new SALParser("return true;");
		

		SALNode root = parser.parse();
		//root.dump("");
		
		assertNotNull(root);
		assertEquals(1, root.jjtGetNumChildren());
		
		SALNode member = (SALNode) root.jjtGetChild(0);
		assertEquals(SALParserTreeConstants.JJTRETURNSTATEMENT, member.getId());
		assertEquals(1, member.jjtGetNumChildren());
		
		SALNode obj = (SALNode) member.jjtGetChild(0);
		
		assertEquals(SALParserTreeConstants.JJTLOGICCONSTANT, obj.getId());
		assertEquals(true, obj.jjtGetValue());
		
	}
	
	

}
