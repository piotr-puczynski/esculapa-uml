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

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.uml2.uml.Connector;
import org.eclipse.uml2.uml.ConnectorEnd;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLFactory;

import dk.dtu.imm.esculapauml.core.checkers.LifelineChecker;
import dk.dtu.imm.esculapauml.core.states.SystemState;

/**
 * Generates new lifeline.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class LifelineGenerator extends AbstractGenerator<Lifeline> {

	private Interaction interaction;
	private Type type;

	/**
	 * @param systemState
	 * @param interaction
	 * @param type
	 */
	public LifelineGenerator(SystemState systemState, BasicDiagnostic diagnostic, Interaction interaction, Type type) {
		super(systemState, diagnostic);
		this.interaction = interaction;
		this.type = type;
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
		annotateAsGenerated(prop);
		Connector connector = interaction.getOwnedConnector("Connector1", true, true);
		ConnectorEnd end = connector.createEnd();
		end.setRole(prop);
		annotateAsGenerated(end);
		generated = interaction.createLifeline(type.getLabel());
		generated.setRepresents(prop);
		annotateAsGenerated(generated);
		// check the lifeline to create an executor
		LifelineChecker lc = new LifelineChecker(systemState, diagnostic, generated);
		lc.check();
		return generated;
	}

}
