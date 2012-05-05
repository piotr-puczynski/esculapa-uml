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

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.uml2.common.util.UML2Util;
import org.eclipse.uml2.uml.Element;

import dk.dtu.imm.esculapauml.core.checkers.AbstractChecker;
import dk.dtu.imm.esculapauml.core.executors.UseCaseExecutor;
import dk.dtu.imm.esculapauml.core.executors.coordination.EsculapaCallEvent;
import dk.dtu.imm.esculapauml.core.executors.coordination.EsculapaCallReturnControlEvent;
import dk.dtu.imm.esculapauml.core.executors.coordination.EsculapaReplyEvent;
import dk.dtu.imm.esculapauml.core.executors.coordination.ExecutionListener;

/**
 * Class used for optional termination of the execution in case of
 * user-specified termination conditions. This can be used to enforce stop of
 * execution of BSM to avoid e.g. infinite loops.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class ExecutionTerminator implements ExecutionListener {

	private UseCaseExecutor executor;
	// used to specify global maximum number of events during execution
	private int maxGlobalEvents = -1;
	// used to specify maximum number of repetitive subsequent events
	private int maxRepetitiveSubsequentEvents = -1;

	/**
	 * @param useCaseExecutor
	 */
	public ExecutionTerminator(UseCaseExecutor useCaseExecutor) {
		this.executor = useCaseExecutor;

		// check our options
		readOptions(executor.getInteraction());

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
				int maxGlobalEvents = Integer.getInteger(detail, -1);
				if(maxGlobalEvents < 0) {
					
				} else {
					this.maxGlobalEvents = maxGlobalEvents;
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
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.executors.coordination.ExecutionListener#
	 * replyEventOccurred
	 * (dk.dtu.imm.esculapauml.core.executors.coordination.EsculapaReplyEvent)
	 */
	@Override
	public void replyEventOccurred(EsculapaReplyEvent event) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.executors.coordination.ExecutionListener#
	 * callReturnControlEventOccurred
	 * (dk.dtu.imm.esculapauml.core.executors.coordination
	 * .EsculapaCallReturnControlEvent)
	 */
	@Override
	public void callReturnControlEventOccurred(EsculapaCallReturnControlEvent event) {
		// TODO Auto-generated method stub

	}

}
