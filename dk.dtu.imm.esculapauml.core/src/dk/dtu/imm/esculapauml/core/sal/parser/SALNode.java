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
package dk.dtu.imm.esculapauml.core.sal.parser;

/**
 * @author Piotr J. Puczynski
 * 
 */
public class SALNode extends SimpleNode {

	/**
	 * @param i
	 */
	public SALNode(int i) {
		super(i);
	}

	/**
	 * @param p
	 * @param i
	 */
	public SALNode(SALParser p, int i) {
		super(p, i);
	}
	
	public int getId() {
		return id;
	}
	
	public SALNode getChild(int i) {
		return (SALNode) jjtGetChild(i);
	}

	@Override
	public String toString() {
		return SALParserTreeConstants.jjtNodeName[id] + ((jjtGetValue()==null)? "": " (" + jjtGetValue().toString() + ")");
	}

	@Override
	public String toString(String prefix) {
		return prefix + toString();
	}

}
