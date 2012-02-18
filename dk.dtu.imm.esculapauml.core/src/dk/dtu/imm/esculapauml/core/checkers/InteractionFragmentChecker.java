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

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.uml2.uml.BehaviorExecutionSpecification;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;

import dk.dtu.imm.esculapauml.core.utils.InteractionUtils;

/**
 * Checker to check coveredbys on lifelines.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class InteractionFragmentChecker extends AbstractChecker<InteractionFragment> {

	/**
	 * @param checker
	 * @param objectToCheck
	 */
	InteractionFragmentChecker(Checker checker, InteractionFragment objectToCheck) {
		super(checker, objectToCheck);
		logger = Logger.getLogger(InteractionFragmentChecker.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dk.dtu.imm.esculapauml.core.checkers.Checker#check()
	 */
	@Override
	public void check() {
		logger.debug(checkee.getLabel() + ": start check");
		if (checkee instanceof MessageOccurrenceSpecification) {
			checkMessageOccurrence();
		}
		if (checkee instanceof BehaviorExecutionSpecification) {
			checkBehavior();
		}
	}

	/**
	 * 
	 */
	protected void checkBehavior() {
		BehaviorExecutionSpecification bes = (BehaviorExecutionSpecification) checkee;
		if (null == bes.getStart()) {
			addProblem(Diagnostic.ERROR, "BehaviorExecutionSpecification \"" + checkee.getLabel() + "\" has no start set.");
		}
		if (null == bes.getFinish()) {
			addProblem(Diagnostic.ERROR, "BehaviorExecutionSpecification \"" + checkee.getLabel() + "\" has no finish set.");
		}
		// detect problems with order of the start and finish
		if (bes.getStart() instanceof MessageOccurrenceSpecification && bes.getFinish() instanceof MessageOccurrenceSpecification) {
			int startIndex = InteractionUtils.getLifelineOfFragment(bes).getCoveredBys().indexOf(bes.getStart());
			int finishIndex = InteractionUtils.getLifelineOfFragment(bes).getCoveredBys().indexOf(bes.getFinish());
			if (-1 == startIndex) {
				addProblem(Diagnostic.ERROR, "BehaviorExecutionSpecification \"" + checkee.getLabel()
						+ "\" start points to fragment not placed on covering lifeline.");
			}
			if (-1 == finishIndex) {
				addProblem(Diagnostic.ERROR, "BehaviorExecutionSpecification \"" + checkee.getLabel()
						+ "\" finish points to fragment not placed on covering lifeline.");
			}
			if (-1 != startIndex && -1 != finishIndex && startIndex > finishIndex) {
				addProblem(Diagnostic.ERROR, "BehaviorExecutionSpecification \"" + checkee.getLabel()
						+ "\" start and finish have wrong order on the covering lifeline.");
			}
		}
	}

	/**
	 * 
	 */
	protected void checkMessageOccurrence() {
		MessageOccurrenceSpecification mos = (MessageOccurrenceSpecification) checkee;
		if (null == mos.getMessage()) {
			addProblem(Diagnostic.ERROR, "MessageOccurrenceSpecification \"" + checkee.getLabel() + "\" points to no message.");
		}
	}

}
