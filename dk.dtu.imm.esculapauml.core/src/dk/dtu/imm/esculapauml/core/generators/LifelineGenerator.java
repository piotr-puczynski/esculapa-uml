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
package dk.dtu.imm.esculapauml.core.generators;

import org.apache.log4j.Logger;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Connector;
import org.eclipse.uml2.uml.ConnectorEnd;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLFactory;

import dk.dtu.imm.esculapauml.core.checkers.Checker;

/**
 * Generates new lifeline.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class LifelineGenerator extends AbstractGenerator<Lifeline> {

	private Interaction interaction;
	private Type type;
	private String name;

	/**
	 * @param checker
	 * @param checkee
	 * @param targetClass
	 */
	public LifelineGenerator(Checker checker, Interaction interaction, Class type, String name) {
		super(checker);
		logger = Logger.getLogger(LifelineGenerator.class);
		this.interaction = interaction;
		this.type = type;
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dk.dtu.imm.esculapauml.core.generators.GeneratorInterface#generate()
	 */
	@Override
	public Lifeline generate() {
		Property prop = interaction.createOwnedAttribute(type.getLabel(), UMLFactory.eINSTANCE.createProperty().getType());
		prop.setType(type);
		systemState.addGeneratedElement(prop);
		Connector connector = interaction.getOwnedConnector("Connector1", true, true);
		ConnectorEnd end = connector.createEnd();
		end.setRole(prop);
		systemState.addGeneratedElement(end);
		generated = interaction.createLifeline(name);
		generated.setRepresents(prop);
		systemState.addGeneratedElement(generated);
		logger.info("Generated new element: " + generated.getLabel());
		return generated;
	}

}
