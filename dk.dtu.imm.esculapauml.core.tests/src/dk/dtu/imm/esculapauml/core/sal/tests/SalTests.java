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
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class SalTests {

	@Test
	public void parseSemicolons() throws ParseException {
		SALParser parser = new SALParser("fnc1; fnc2");

		SALNode root = parser.parse();
		// root.dump("");

		assertNotNull(root);
		assertEquals(2, root.jjtGetNumChildren());

		SALNode id1 = root.getChild(0);
		SALNode id2 = root.getChild(1);
		assertEquals(SALParserTreeConstants.JJTIDENT, id1.getId());
		assertEquals(SALParserTreeConstants.JJTIDENT, id2.getId());
		assertEquals("fnc1", id1.jjtGetValue());
		assertEquals("fnc2", id2.jjtGetValue());

	}

	@Test
	public void parseSemicolonsIgnoreLast() throws ParseException {
		SALParser parser = new SALParser("fnc1; fnc2;");

		SALNode root = parser.parse();
		// root.dump("");

		assertNotNull(root);
		assertEquals(2, root.jjtGetNumChildren());

		SALNode id1 = root.getChild(0);
		SALNode id2 = root.getChild(1);
		assertEquals(SALParserTreeConstants.JJTIDENT, id1.getId());
		assertEquals(SALParserTreeConstants.JJTIDENT, id2.getId());
		assertEquals("fnc1", id1.jjtGetValue());
		assertEquals("fnc2", id2.jjtGetValue());

	}

	@Test
	public void parseIdentSelector() throws ParseException {
		SALParser parser = new SALParser("obj1[1]");

		SALNode root = parser.parse();
		// root.dump("");

		assertNotNull(root);
		assertEquals(1, root.jjtGetNumChildren());

		SALNode member = root.getChild(0);
		assertEquals(SALParserTreeConstants.JJTIDENTSELECTOR, member.getId());
		assertEquals(2, member.jjtGetNumChildren());

		SALNode ident = member.getChild(0);
		SALNode selector = member.getChild(1);

		assertEquals(SALParserTreeConstants.JJTIDENT, ident.getId());
		assertEquals(SALParserTreeConstants.JJTSELECTOR, selector.getId());

	}

	@Test
	public void parseCallSelector() throws ParseException {
		SALParser parser = new SALParser("op()[1]");

		SALNode root = parser.parse();
		// root.dump("");

		assertNotNull(root);
		assertEquals(1, root.jjtGetNumChildren());

		SALNode member = root.getChild(0);
		assertEquals(SALParserTreeConstants.JJTCALLSELECTOR, member.getId());
		assertEquals(2, member.jjtGetNumChildren());

		SALNode call = member.getChild(0);
		SALNode selector = member.getChild(1);

		assertEquals(SALParserTreeConstants.JJTSELECTOR, selector.getId());
		assertEquals(SALParserTreeConstants.JJTCALL, call.getId());

	}

	@Test
	public void assignmentSelector() throws ParseException {
		SALParser parser = new SALParser("abc[20] := 90");

		SALNode root = parser.parse();
		// root.dump("");

		assertNotNull(root);
		assertEquals(1, root.jjtGetNumChildren());

		SALNode member = root.getChild(0);
		assertEquals(SALParserTreeConstants.JJTASSIGNMENTSELECTOR, member.getId());
		assertEquals(2, member.jjtGetNumChildren());

	}

	@Test
	public void collection() throws ParseException {
		SALParser parser = new SALParser("[2,5,6]");

		SALNode root = parser.parse();
		// root.dump("");

		assertNotNull(root);
		assertEquals(1, root.jjtGetNumChildren());

		SALNode member = root.getChild(0);
		assertEquals(SALParserTreeConstants.JJTCOLLECTIONEXPRESSION, member.getId());
		assertEquals(3, member.jjtGetNumChildren());

	}

	@Test
	public void parseMember() throws ParseException {
		SALParser parser = new SALParser("obj1.member2");

		SALNode root = parser.parse();
		// root.dump("");

		assertNotNull(root);
		assertEquals(1, root.jjtGetNumChildren());

		SALNode member = root.getChild(0);
		assertEquals(SALParserTreeConstants.JJTMEMBER, member.getId());
		assertEquals(2, member.jjtGetNumChildren());
		assertEquals("obj1", member.getChild(0).jjtGetValue());

		SALNode member2 = member.getChild(1);

		assertEquals("member2", member2.jjtGetValue());

	}

	@Test
	public void parseFunction() throws ParseException {
		SALParser parser = new SALParser("f(true, 123)");

		SALNode root = parser.parse();
		// root.dump("");

		assertNotNull(root);
		assertEquals(1, root.jjtGetNumChildren());

		SALNode call = root.getChild(0);
		assertEquals(SALParserTreeConstants.JJTCALL, call.getId());
		assertEquals(2, call.jjtGetNumChildren());
		assertEquals("f", call.jjtGetValue());

		SALNode arg1 = call.getChild(0);

		assertEquals(SALParserTreeConstants.JJTLOGICCONSTANT, arg1.getId());
		assertEquals(true, arg1.jjtGetValue());

		SALNode arg2 = call.getChild(1);
		assertEquals(SALParserTreeConstants.JJTINTEGERCONSTANT, arg2.getId());
		assertEquals(123, arg2.jjtGetValue());

	}

	@Test
	public void parseAsynch() throws ParseException {
		SALParser parser = new SALParser("async:f(true, 123)");

		SALNode root = parser.parse();
		// root.dump("");

		assertNotNull(root);
		assertEquals(1, root.jjtGetNumChildren());

		SALNode call = root.getChild(0);
		assertEquals(SALParserTreeConstants.JJTCALLASYNC, call.getId());
		assertEquals(2, call.jjtGetNumChildren());
		assertEquals("f", call.jjtGetValue());

		SALNode arg1 = call.getChild(0);

		assertEquals(SALParserTreeConstants.JJTLOGICCONSTANT, arg1.getId());
		assertEquals(true, arg1.jjtGetValue());

		SALNode arg2 = call.getChild(1);
		assertEquals(SALParserTreeConstants.JJTINTEGERCONSTANT, arg2.getId());
		assertEquals(123, arg2.jjtGetValue());

	}

	@Test
	public void parseMemberFunction() throws ParseException {
		SALParser parser = new SALParser("obj1.member2(123, \"str\")");

		SALNode root = parser.parse();
		// root.dump("");

		assertNotNull(root);
		assertEquals(1, root.jjtGetNumChildren());

		SALNode obj1 = root.getChild(0);
		assertEquals(SALParserTreeConstants.JJTMEMBERCALL, obj1.getId());
		assertEquals(2, obj1.jjtGetNumChildren());
		assertEquals("obj1", obj1.getChild(0).jjtGetValue());

		SALNode member2 = obj1.getChild(1);

		assertEquals(SALParserTreeConstants.JJTCALL, member2.getId());
		assertEquals("member2", member2.jjtGetValue());

		assertEquals(2, member2.jjtGetNumChildren());
		SALNode num = member2.getChild(0);
		SALNode str = member2.getChild(1);
		assertEquals(SALParserTreeConstants.JJTINTEGERCONSTANT, num.getId());
		assertEquals(SALParserTreeConstants.JJTSTRINGCONSTANT, str.getId());
		assertEquals(123, num.jjtGetValue());
		assertEquals("str", str.jjtGetValue());

	}

	@Test
	public void parseMember2() throws ParseException {
		SALParser parser = new SALParser("obj1.member2.Space");

		SALNode root = parser.parse();
		// root.dump("");

		assertNotNull(root);
		assertEquals(1, root.jjtGetNumChildren());

		SALNode obj1 = root.getChild(0);
		assertEquals(SALParserTreeConstants.JJTMEMBER, obj1.getId());
		assertEquals(2, obj1.jjtGetNumChildren());
		assertEquals("Space", obj1.getChild(1).jjtGetValue());

		SALNode member1 = obj1.getChild(0);
		assertEquals("obj1", member1.getChild(0).jjtGetValue());
		assertEquals("member2", member1.getChild(1).jjtGetValue());

	}

	@Test
	public void replyTest() throws ParseException {
		SALParser parser = new SALParser("reply true;");

		SALNode root = parser.parse();
		// root.dump("");

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
		// root.dump("");

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
		// root.dump("");

		assertNotNull(root);
		assertEquals(1, root.jjtGetNumChildren());

		SALNode assign = root.getChild(0);
		assertEquals(SALParserTreeConstants.JJTASSIGNMENT, assign.getId());
		assertEquals("myVar", assign.jjtGetValue());
		assertEquals(1, assign.jjtGetNumChildren());

		SALNode val = assign.getChild(0);

		assertEquals(SALParserTreeConstants.JJTMEMBERCALL, val.getId());

	}

	@Test
	public void nullTest() throws ParseException {
		SALParser parser = new SALParser("myVar := null");

		SALNode root = parser.parse();
		// root.dump("");

		assertNotNull(root);
		assertEquals(1, root.jjtGetNumChildren());

		SALNode assign = root.getChild(0);
		assertEquals(SALParserTreeConstants.JJTASSIGNMENT, assign.getId());
		assertEquals("myVar", assign.jjtGetValue());
		assertEquals(1, assign.jjtGetNumChildren());

		SALNode val = assign.getChild(0);

		assertEquals(SALParserTreeConstants.JJTNULLCONSTANT, val.getId());

	}

	@Test
	public void oclTest() throws ParseException {
		SALParser parser = new SALParser("myVar := ocl(`some ocl expression()`)");

		SALNode root = parser.parse();
		// root.dump("");

		assertNotNull(root);
		assertEquals(1, root.jjtGetNumChildren());

		SALNode assign = root.getChild(0);
		assertEquals(SALParserTreeConstants.JJTASSIGNMENT, assign.getId());
		assertEquals("myVar", assign.jjtGetValue());
		assertEquals(1, assign.jjtGetNumChildren());

		SALNode val = assign.getChild(0);

		assertEquals(SALParserTreeConstants.JJTOCLEXPRESSION, val.getId());

		assertEquals("some ocl expression()", val.jjtGetValue());

	}

	@Test
	public void addition() throws ParseException {
		SALParser parser = new SALParser("2+3");

		SALNode root = parser.parse();
		// root.dump("");

		assertNotNull(root);
		assertEquals(1, root.jjtGetNumChildren());

		SALNode add = root.getChild(0);
		assertEquals(SALParserTreeConstants.JJTADD, add.getId());
		assertEquals(2, add.jjtGetNumChildren());

		SALNode val1 = add.getChild(0);
		SALNode val2 = add.getChild(1);

		assertEquals(SALParserTreeConstants.JJTINTEGERCONSTANT, val1.getId());
		assertEquals(SALParserTreeConstants.JJTINTEGERCONSTANT, val2.getId());

		assertEquals(2, val1.jjtGetValue());
		assertEquals(3, val2.jjtGetValue());

	}

	@Test
	public void substraction() throws ParseException {
		SALParser parser = new SALParser("2-3");

		SALNode root = parser.parse();
		// root.dump("");

		assertNotNull(root);
		assertEquals(1, root.jjtGetNumChildren());

		SALNode add = root.getChild(0);
		assertEquals(SALParserTreeConstants.JJTSUBSTRACT, add.getId());
		assertEquals(2, add.jjtGetNumChildren());

		SALNode val1 = add.getChild(0);
		SALNode val2 = add.getChild(1);

		assertEquals(SALParserTreeConstants.JJTINTEGERCONSTANT, val1.getId());
		assertEquals(SALParserTreeConstants.JJTINTEGERCONSTANT, val2.getId());

		assertEquals(2, val1.jjtGetValue());
		assertEquals(3, val2.jjtGetValue());

	}

	@Test
	public void mul() throws ParseException {
		SALParser parser = new SALParser("2*3");

		SALNode root = parser.parse();
		// root.dump("");

		assertNotNull(root);
		assertEquals(1, root.jjtGetNumChildren());

		SALNode add = root.getChild(0);
		assertEquals(SALParserTreeConstants.JJTMULT, add.getId());
		assertEquals(2, add.jjtGetNumChildren());

		SALNode val1 = add.getChild(0);
		SALNode val2 = add.getChild(1);

		assertEquals(SALParserTreeConstants.JJTINTEGERCONSTANT, val1.getId());
		assertEquals(SALParserTreeConstants.JJTINTEGERCONSTANT, val2.getId());

		assertEquals(2, val1.jjtGetValue());
		assertEquals(3, val2.jjtGetValue());

	}

	@Test
	public void div() throws ParseException {
		SALParser parser = new SALParser("2/3");

		SALNode root = parser.parse();
		// root.dump("");

		assertNotNull(root);
		assertEquals(1, root.jjtGetNumChildren());

		SALNode add = root.getChild(0);
		assertEquals(SALParserTreeConstants.JJTDIV, add.getId());
		assertEquals(2, add.jjtGetNumChildren());

		SALNode val1 = add.getChild(0);
		SALNode val2 = add.getChild(1);

		assertEquals(SALParserTreeConstants.JJTINTEGERCONSTANT, val1.getId());
		assertEquals(SALParserTreeConstants.JJTINTEGERCONSTANT, val2.getId());

		assertEquals(2, val1.jjtGetValue());
		assertEquals(3, val2.jjtGetValue());

	}

	@Test
	public void mod() throws ParseException {
		SALParser parser = new SALParser("2%3");

		SALNode root = parser.parse();
		// root.dump("");

		assertNotNull(root);
		assertEquals(1, root.jjtGetNumChildren());

		SALNode add = root.getChild(0);
		assertEquals(SALParserTreeConstants.JJTMOD, add.getId());
		assertEquals(2, add.jjtGetNumChildren());

		SALNode val1 = add.getChild(0);
		SALNode val2 = add.getChild(1);

		assertEquals(SALParserTreeConstants.JJTINTEGERCONSTANT, val1.getId());
		assertEquals(SALParserTreeConstants.JJTINTEGERCONSTANT, val2.getId());

		assertEquals(2, val1.jjtGetValue());
		assertEquals(3, val2.jjtGetValue());

	}

	@Test
	public void mulPrecedence() throws ParseException {
		SALParser parser = new SALParser("2+3*9");

		SALNode root = parser.parse();
		// root.dump("");

		assertNotNull(root);
		assertEquals(1, root.jjtGetNumChildren());

		SALNode add = root.getChild(0);
		assertEquals(SALParserTreeConstants.JJTADD, add.getId());
		assertEquals(2, add.jjtGetNumChildren());

		SALNode val1 = add.getChild(0);
		SALNode val2 = add.getChild(1);

		assertEquals(SALParserTreeConstants.JJTINTEGERCONSTANT, val1.getId());
		assertEquals(SALParserTreeConstants.JJTMULT, val2.getId());

		assertEquals(2, val1.jjtGetValue());
	}

	@Test
	public void parantheses() throws ParseException {
		SALParser parser = new SALParser("(2+3)*-9");

		SALNode root = parser.parse();
		// root.dump("");

		assertNotNull(root);
		assertEquals(1, root.jjtGetNumChildren());

		SALNode add = root.getChild(0);
		assertEquals(SALParserTreeConstants.JJTMULT, add.getId());
		assertEquals(2, add.jjtGetNumChildren());

		SALNode val1 = add.getChild(0);
		SALNode val2 = add.getChild(1);

		assertEquals(SALParserTreeConstants.JJTADD, val1.getId());
		assertEquals(SALParserTreeConstants.JJTINTEGERCONSTANT, val2.getId());

		assertEquals(-9, val2.jjtGetValue());
	}

	@Test
	public void paranthesesExtra() throws ParseException {
		SALParser parser = new SALParser("((((2+3)))*-9)");

		SALNode root = parser.parse();
		// root.dump("");

		assertNotNull(root);
		assertEquals(1, root.jjtGetNumChildren());

		SALNode add = root.getChild(0);
		assertEquals(SALParserTreeConstants.JJTMULT, add.getId());
		assertEquals(2, add.jjtGetNumChildren());

		SALNode val1 = add.getChild(0);
		SALNode val2 = add.getChild(1);

		assertEquals(SALParserTreeConstants.JJTADD, val1.getId());
		assertEquals(SALParserTreeConstants.JJTINTEGERCONSTANT, val2.getId());

		assertEquals(-9, val2.jjtGetValue());
	}

	@Test
	public void and() throws ParseException {
		SALParser parser = new SALParser("true and false");

		SALNode root = parser.parse();
		// root.dump("");

		assertNotNull(root);
		assertEquals(1, root.jjtGetNumChildren());

		SALNode add = root.getChild(0);
		assertEquals(SALParserTreeConstants.JJTAND, add.getId());
		assertEquals(2, add.jjtGetNumChildren());

		SALNode val1 = add.getChild(0);
		SALNode val2 = add.getChild(1);

		assertEquals(SALParserTreeConstants.JJTLOGICCONSTANT, val1.getId());
		assertEquals(SALParserTreeConstants.JJTLOGICCONSTANT, val2.getId());

		assertEquals(true, val1.jjtGetValue());
		assertEquals(false, val2.jjtGetValue());
	}

	@Test
	public void or() throws ParseException {
		SALParser parser = new SALParser("true or false");

		SALNode root = parser.parse();
		// root.dump("");

		assertNotNull(root);
		assertEquals(1, root.jjtGetNumChildren());

		SALNode add = root.getChild(0);
		assertEquals(SALParserTreeConstants.JJTOR, add.getId());
		assertEquals(2, add.jjtGetNumChildren());

		SALNode val1 = add.getChild(0);
		SALNode val2 = add.getChild(1);

		assertEquals(SALParserTreeConstants.JJTLOGICCONSTANT, val1.getId());
		assertEquals(SALParserTreeConstants.JJTLOGICCONSTANT, val2.getId());

		assertEquals(true, val1.jjtGetValue());
		assertEquals(false, val2.jjtGetValue());
	}

	@Test
	public void not() throws ParseException {
		SALParser parser = new SALParser("true or not false");

		SALNode root = parser.parse();
		// root.dump("");

		assertNotNull(root);
		assertEquals(1, root.jjtGetNumChildren());

		SALNode add = root.getChild(0);
		assertEquals(SALParserTreeConstants.JJTOR, add.getId());
		assertEquals(2, add.jjtGetNumChildren());

		SALNode val1 = add.getChild(0);
		SALNode val2 = add.getChild(1);

		assertEquals(SALParserTreeConstants.JJTLOGICCONSTANT, val1.getId());
		assertEquals(SALParserTreeConstants.JJTNOT, val2.getId());

		assertEquals(true, val1.jjtGetValue());
	}

}
