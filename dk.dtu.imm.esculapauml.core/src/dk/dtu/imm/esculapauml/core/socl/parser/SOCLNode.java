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
package dk.dtu.imm.esculapauml.core.socl.parser;

/**
 * @author Piotr J. Puczynski
 * 
 */
public class SOCLNode extends SimpleNode {

	/**
	 * @param i
	 */
	public SOCLNode(int i) {
		super(i);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param p
	 * @param i
	 */
	public SOCLNode(SOCLParser p, int i) {
		super(p, i);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return SOCLParserTreeConstants.jjtNodeName[id] + ((jjtGetValue()==null)? "": " (" + jjtGetValue().toString() + ")");
	}

	@Override
	public String toString(String prefix) {
		return prefix + toString();
	}

}
