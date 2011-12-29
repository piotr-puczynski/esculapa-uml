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
package dk.dtu.imm.esculapauml.core.checkers;

import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.GeneralOrdering;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionFragment;

/**
 * @author Piotr. J. Puczynski (piotr.puczynski)
 *
 */
public class UseCaseChecker extends AbstractInteractionChecker {
	
	public UseCaseChecker(Interaction interaction) {
		super(interaction);
	}

	/* (non-Javadoc)
	 * @see dk.dtu.imm.esculapauml.core.checkers.CheckerInterface#check()
	 */
	@Override
	public void check() {
		structuralLifelinesExistanceCheck();
		
		EList<InteractionFragment> fragments = interaction.getFragments();
		for (InteractionFragment f : fragments) {
			System.out.println(f.toString());
			
		}
		
	}

}
