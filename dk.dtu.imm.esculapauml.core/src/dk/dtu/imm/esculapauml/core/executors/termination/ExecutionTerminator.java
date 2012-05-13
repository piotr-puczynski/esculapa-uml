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
package dk.dtu.imm.esculapauml.core.executors.termination;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.uml2.common.util.UML2Util;
import org.eclipse.uml2.uml.Element;

import dk.dtu.imm.esculapauml.core.checkers.AbstractChecker;
import dk.dtu.imm.esculapauml.core.executors.UseCaseExecutor;
import dk.dtu.imm.esculapauml.core.executors.coordination.EsculapaCallEvent;
import dk.dtu.imm.esculapauml.core.executors.coordination.EsculapaCompletionEvent;
import dk.dtu.imm.esculapauml.core.executors.coordination.ExecutionCallListener;
import dk.dtu.imm.esculapauml.core.executors.coordination.ExecutionCompletionListener;

/**
 * Class used for optional termination of the execution in case of
 * user-specified termination conditions. This can be used to enforce stop of
 * execution of BSM to avoid e.g. infinite loops.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class ExecutionTerminator implements ExecutionCallListener, ExecutionCompletionListener {

	private UseCaseExecutor executor;
	// used to specify global maximum number of events during execution
	private long maxGlobalEvents = -1;
	// used to specify maximum number of repetitive subsequent events
	private long maxRepetitiveSubsequentEvents = -1;

	private long numberOfExecutedEvents = 0;

	private EsculapaCallEvent lastEvent = null;

	private long numberOfRepetitiveEvents = 0;

	/**
	 * @param useCaseExecutor
	 */
	public ExecutionTerminator(UseCaseExecutor useCaseExecutor) {
		this.executor = useCaseExecutor;

		// check our options
		readOptions(executor.getInteraction());
		if (isActive(maxGlobalEvents) || isActive(maxRepetitiveSubsequentEvents)) {
			// we are active, register us as listener
			executor.getChecker().getSystemState().getCoordinator().addExecutionListener(this);
		}
	}

	/**
	 * checks if option is active (is set)
	 * 
	 * @param option
	 * @return
	 */
	private boolean isActive(long option) {
		return option > -1L;
	}

	/**
	 * Reads options for terminator from the model element.
	 * 
	 * @param interaction
	 */
	private void readOptions(Element element) {
		EAnnotation annotation = UML2Util.getEAnnotation(element, AbstractChecker.ESCULAPA_NAMESPACE, false);
		if (null != annotation) {
			String detail = annotation.getDetails().get("max-global-events");
			if (null != detail) {
				long maxGlobalEvents;
				try {
					maxGlobalEvents = Long.parseLong(detail);
				} catch (NumberFormatException ex) {
					maxGlobalEvents = -1L;
				}
				if (!isActive(maxGlobalEvents)) {
					executor.getChecker().addOtherProblem(Diagnostic.CANCEL, "Value of 'max-global-events' option is not correct (must be greater than 0).",
							annotation);
				} else {
					this.maxGlobalEvents = maxGlobalEvents;
				}
			}

			detail = annotation.getDetails().get("max-repetitive-subsequent-events");
			if (null != detail) {
				long maxRepetitiveSubsequentEvents;
				try {
					maxRepetitiveSubsequentEvents = Long.parseLong(detail);
				} catch (NumberFormatException ex) {
					maxRepetitiveSubsequentEvents = -1L;
				}
				if (!isActive(maxRepetitiveSubsequentEvents)) {
					executor.getChecker().addOtherProblem(Diagnostic.CANCEL,
							"Value of 'max-repetitive-subsequent-events' option is not correct (must be greater than 0).", annotation);
				} else {
					this.maxRepetitiveSubsequentEvents = maxRepetitiveSubsequentEvents;
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.executors.coordination.ExecutionListener#
	 * callEventOccurred
	 * (dk.dtu.imm.esculapauml.core.executors.coordination.EsculapaCallEvent)
	 */
	@Override
	public void callEventOccurred(EsculapaCallEvent event) {
		countEvents();

		if (isActive(maxRepetitiveSubsequentEvents)) {
			if (null == lastEvent) {
				resetLastEvent(event);
			} else {
				if (lastEvent.getOperation() == event.getOperation() && lastEvent.getSource() == event.getSource()) {
					++numberOfRepetitiveEvents;
				} else {
					resetLastEvent(event);
				}
			}

			if (numberOfRepetitiveEvents > maxRepetitiveSubsequentEvents) {
				executor.getChecker().addOtherProblem(Diagnostic.CANCEL,
						"Value of 'max-repetitive-subsequent-events' has been exceeded. The execution is stopped.", executor.getInteraction());
			}

		}

	}

	/**
	 * 
	 */
	private void countEvents() {
		if (isActive(maxGlobalEvents)) {
			if (++numberOfExecutedEvents > maxGlobalEvents) {
				executor.getChecker().addOtherProblem(Diagnostic.CANCEL, "Value of 'max-global-events' has been exceeded. The execution is stopped.",
						executor.getInteraction());
			}
		}
	}

	/**
	 * @param event
	 */
	private void resetLastEvent(EsculapaCallEvent event) {
		lastEvent = event;
		numberOfRepetitiveEvents = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dk.dtu.imm.esculapauml.core.executors.coordination.
	 * ExecutionCompletionListener
	 * #completionEventOccurred(dk.dtu.imm.esculapauml
	 * .core.executors.coordination.EsculapaCompletionEvent)
	 */
	@Override
	public void completionEventOccurred(EsculapaCompletionEvent event) {
		countEvents();
	}

}
