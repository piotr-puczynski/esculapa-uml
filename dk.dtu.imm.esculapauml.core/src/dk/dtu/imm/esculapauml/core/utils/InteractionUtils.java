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
import static org.hamcrest.Matchers.is;

import java.util.List;

import org.eclipse.uml2.uml.BehaviorExecutionSpecification;
import org.eclipse.uml2.uml.CallEvent;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.OccurrenceSpecification;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Type;

/**
 * @author Piotr J. Puczynski
 * 
 */
public final class InteractionUtils {
	public static Type getMessageTargetType(Message message) {
		Lifeline result = getMessageTargetLifeline(message);
		if (null != result) {
			return result.getRepresents().getType();
		}
		return null;
	}

	public static Lifeline getMessageTargetLifeline(Message message) {
		if (message.getReceiveEvent() instanceof MessageOccurrenceSpecification) {
			MessageOccurrenceSpecification moc = (MessageOccurrenceSpecification) message.getReceiveEvent();
			return getLifelineOfFragment(moc);
		}
		return null;
	}

	public static Lifeline getMessageSourceLifeline(Message message) {
		if (message.getSendEvent() instanceof MessageOccurrenceSpecification) {
			MessageOccurrenceSpecification moc = (MessageOccurrenceSpecification) message.getSendEvent();
			return getLifelineOfFragment(moc);
		}
		return null;
	}

	public static Operation getMessageOperation(Message message) {
		if (message.getSendEvent() instanceof MessageOccurrenceSpecification) {
			MessageOccurrenceSpecification moc = (MessageOccurrenceSpecification) message.getSendEvent();
			if (moc.getEvent() instanceof CallEvent) {
				return ((CallEvent) moc.getEvent()).getOperation();
			}
		}
		return null;
	}
	
	public static void setMessageOperation(Message message, Operation operation) {
		if (message.getSendEvent() instanceof MessageOccurrenceSpecification) {
			MessageOccurrenceSpecification moc = (MessageOccurrenceSpecification) message.getSendEvent();
			if (moc.getEvent() instanceof CallEvent) {
				((CallEvent) moc.getEvent()).setOperation(operation);
			}
		}
		if (message.getReceiveEvent() instanceof MessageOccurrenceSpecification) {
			MessageOccurrenceSpecification moc = (MessageOccurrenceSpecification) message.getReceiveEvent();
			if (moc.getEvent() instanceof CallEvent) {
				((CallEvent) moc.getEvent()).setOperation(operation);
			}
		}
	}

	public static Lifeline getLifelineOfFragment(InteractionFragment occurrence) {
		if (occurrence.getCovereds().size() > 0) {
			return occurrence.getCovereds().get(0);
		}
		return null;
	}

	public static BehaviorExecutionSpecification getMessageSourceExecutionSpecification(Message message) {
		Lifeline lifeline = getMessageSourceLifeline(message);
		if (null != lifeline) {
			MessageOccurrenceSpecification moc = (MessageOccurrenceSpecification) message.getSendEvent();
			int mocIndex = lifeline.getCoveredBys().indexOf(moc);
			// we find all behavior execution specifications
			List<InteractionFragment> allBes = filter(is(BehaviorExecutionSpecification.class), lifeline.getCoveredBys());
			for (InteractionFragment ifbes : allBes) {
				BehaviorExecutionSpecification bes = (BehaviorExecutionSpecification) ifbes;
				OccurrenceSpecification start = bes.getStart();
				OccurrenceSpecification finish = bes.getFinish();
				// check borders
				if (moc == start || moc == finish) {
					return bes;
				}
				// otherwise check range
				if (null != start && null != finish) {
					int startIndex = lifeline.getCoveredBys().indexOf(start);
					int finishIndex = lifeline.getCoveredBys().indexOf(finish);
					if (mocIndex > startIndex && mocIndex < finishIndex) {
						return bes;
					}
				}
			}
		}

		return null;
	}

	public static BehaviorExecutionSpecification getMessageTargetExecutionSpecification(Message message) {
		Lifeline lifeline = getMessageTargetLifeline(message);
		if (null != lifeline) {
			MessageOccurrenceSpecification moc = (MessageOccurrenceSpecification) message.getReceiveEvent();
			int mocIndex = lifeline.getCoveredBys().indexOf(moc);
			// we find all behavior execution specifications
			List<InteractionFragment> allBes = filter(is(BehaviorExecutionSpecification.class), lifeline.getCoveredBys());
			for (InteractionFragment ifbes : allBes) {
				BehaviorExecutionSpecification bes = (BehaviorExecutionSpecification) ifbes;
				OccurrenceSpecification start = bes.getStart();
				OccurrenceSpecification finish = bes.getFinish();
				// check borders
				if (moc == start || moc == finish) {
					return bes;
				}
				// otherwise check range
				if (null != start && null != finish) {
					int startIndex = lifeline.getCoveredBys().indexOf(start);
					int finishIndex = lifeline.getCoveredBys().indexOf(finish);
					if (mocIndex > startIndex && mocIndex < finishIndex) {
						return bes;
					}
				}
			}
		}

		return null;
	}

	public static Lifeline findRepresentingLifeline(Interaction interaction, org.eclipse.uml2.uml.Class representant) {
		List<Lifeline> lifelines = filter(having(on(Lifeline.class).getRepresents().getType(), equalTo(representant)), interaction.getLifelines());
		if (lifelines.size() > 0) {
			return lifelines.get(0);
		}
		return null;
	}

	public static MessageOccurrenceSpecification getPreviousMessageOccurrence(MessageOccurrenceSpecification mos) {
		Lifeline lifeline = getLifelineOfFragment(mos);
		if (null != lifeline) {
			List<InteractionFragment> allMsgs = filter(is(MessageOccurrenceSpecification.class), lifeline.getCoveredBys());
			int index = allMsgs.indexOf(mos);
			if (index > 0) {
				return (MessageOccurrenceSpecification) allMsgs.get(index - 1);
			}
		}

		return null;
	}

	public static MessageOccurrenceSpecification getNextMessageOccurrence(MessageOccurrenceSpecification mos) {
		Lifeline lifeline = getLifelineOfFragment(mos);
		if (null != lifeline) {
			List<InteractionFragment> allMsgs = filter(is(MessageOccurrenceSpecification.class), lifeline.getCoveredBys());
			int index = allMsgs.indexOf(mos);
			if (index < allMsgs.size() - 1) {
				return (MessageOccurrenceSpecification) allMsgs.get(index + 1);
			}
		}

		return null;
	}

	/**
	 * Filters the specifications of CoveredBys given lifeline with given class
	 * type.
	 * 
	 * @param lifeline
	 * @param type
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static List<InteractionFragment> filterSpecifications(Lifeline lifeline, Class type) {
		return filter(is(type), lifeline.getCoveredBys());
	}

}
