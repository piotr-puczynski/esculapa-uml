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
}
