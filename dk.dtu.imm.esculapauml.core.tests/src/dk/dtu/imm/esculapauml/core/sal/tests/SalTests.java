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
		
		SALNode fnc1 = root.getChild(0);
		SALNode fnc2 = root.getChild(1);
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
		
		SALNode fnc1 = root.getChild(0);
		SALNode fnc2 = root.getChild(1);
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
		
		SALNode member = root.getChild(0);
		assertEquals(SALParserTreeConstants.JJTMEMEBEROP, member.getId());
		assertEquals(2, member.jjtGetNumChildren());
		
		SALNode obj1 = member.getChild(0);
		SALNode member2 = member.getChild(1);
		
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
		
		SALNode member = root.getChild(0);
		assertEquals(SALParserTreeConstants.JJTMEMEBEROP, member.getId());
		assertEquals(2, member.jjtGetNumChildren());
		
		SALNode obj1 = member.getChild(0);
		SALNode member2 = member.getChild(1);
		
		assertEquals(SALParserTreeConstants.JJTIDENTIFIER, obj1.getId());
		assertEquals(SALParserTreeConstants.JJTCALL, member2.getId());
		assertEquals("obj1", obj1.jjtGetValue());
		assertEquals("member2", member2.jjtGetValue());
		SALNode params = member2.getChild(0);
		assertEquals(SALParserTreeConstants.JJTPARAMETERS, params.getId());
		
		assertEquals(2, params.jjtGetNumChildren());
		SALNode num = params.getChild(0);
		SALNode str = params.getChild(1);
		assertEquals(SALParserTreeConstants.JJTINTEGERCONSTANT, num.getId());
		assertEquals(SALParserTreeConstants.JJTSTRINGCONSTANT, str.getId());
		assertEquals(123, num.jjtGetValue());
		assertEquals("str", str.jjtGetValue());
		
	}
	
	@Test
	public void parseMember2() throws ParseException {
		SALParser parser = new SALParser("obj1.member2.Space");
		

		SALNode root = parser.parse();
		//root.dump("");
		
		assertNotNull(root);
		assertEquals(1, root.jjtGetNumChildren());
		
		SALNode member = root.getChild(0);
		assertEquals(SALParserTreeConstants.JJTMEMEBEROP, member.getId());
		assertEquals(2, member.jjtGetNumChildren());
		
		SALNode obj1 = member.getChild(0);
		SALNode space = member.getChild(1);
		
		assertEquals(SALParserTreeConstants.JJTIDENTIFIER, obj1.getId());
		assertEquals(SALParserTreeConstants.JJTMEMEBEROP, space.getId());
		assertEquals("obj1", obj1.jjtGetValue());
		assertEquals("Space", space.getChild(1).jjtGetValue());
		
	}
	
	@Test
	public void replyTest() throws ParseException {
		SALParser parser = new SALParser("reply true;");
		

		SALNode root = parser.parse();
		//root.dump("");
		
		assertNotNull(root);
		assertEquals(1, root.jjtGetNumChildren());
		
		SALNode member = root.getChild(0);
		assertEquals(SALParserTreeConstants.JJTREPLYSTATEMENT, member.getId());
		assertEquals(1, member.jjtGetNumChildren());
		
		SALNode obj = member.getChild(0);
		
		assertEquals(SALParserTreeConstants.JJTLOGICCONSTANT, obj.getId());
		assertEquals(true, obj.jjtGetValue());
		
	}
	
	@Test
	public void assignmentTest() throws ParseException {
		SALParser parser = new SALParser("myVar := 777");
		

		SALNode root = parser.parse();
		//root.dump("");
		
		assertNotNull(root);
		assertEquals(1, root.jjtGetNumChildren());
		
		SALNode assign = root.getChild(0);
		assertEquals(SALParserTreeConstants.JJTASSIGNMENT, assign.getId());
		assertEquals("myVar", assign.jjtGetValue());
		assertEquals(1, assign.jjtGetNumChildren());
		
		SALNode val = assign.getChild(0);
		
		assertEquals(SALParserTreeConstants.JJTINTEGERCONSTANT, val.getId());
		assertEquals(777, val.jjtGetValue());
		
	}
	
	@Test
	public void assignmentTest2() throws ParseException {
		SALParser parser = new SALParser("myVar := sonia.brokeLeg(left)");
		

		SALNode root = parser.parse();
		//root.dump("");
		
		assertNotNull(root);
		assertEquals(1, root.jjtGetNumChildren());
		
		SALNode assign = root.getChild(0);
		assertEquals(SALParserTreeConstants.JJTASSIGNMENT, assign.getId());
		assertEquals("myVar", assign.jjtGetValue());
		assertEquals(1, assign.jjtGetNumChildren());
		
		SALNode val = assign.getChild(0);
		
		
		assertEquals(SALParserTreeConstants.JJTMEMEBEROP, val.getId());
		
	}
	
	

}
