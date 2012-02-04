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
package dk.dtu.imm.esculapauml.core.utils;

import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static org.hamcrest.Matchers.equalTo;

import java.util.List;

import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.Type;

/**
 * @author Piotr J. Puczynski
 * 
 */
public final class InteractionUtils {
	public static Type getMessageTargetType(Message message) {
		if (message.getReceiveEvent() instanceof MessageOccurrenceSpecification) {
			MessageOccurrenceSpecification moc = (MessageOccurrenceSpecification) message.getReceiveEvent();
			if (moc.getCovereds().size() > 0) {
				Lifeline lifeline = moc.getCovereds().get(0);
				return lifeline.getRepresents().getType();
			}

		}
		return null;
	}
	
	public static Lifeline findRepresentingLifeline(Interaction interaction, org.eclipse.uml2.uml.Class representant) {
		List<Lifeline> lifelines = filter(having(on(Lifeline.class).getRepresents().getType(), equalTo(representant)), interaction.getLifelines());
		if(lifelines.size() > 0) {
			return lifelines.get(0);
		}
		return null;
	}
}
