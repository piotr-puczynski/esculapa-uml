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
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.uml2.uml.Element;

import dk.dtu.imm.esculapauml.core.checkers.AbstractChecker;
import dk.dtu.imm.esculapauml.core.states.SystemState;

/**
 * Abstract class for all generators.
 * @author Piotr J. Puczynski
 *
 */
public abstract class AbstractGenerator<T> implements GeneratorInterface<T> {
	protected SystemState systemState;
	protected BasicDiagnostic diagnostic;
	protected T generated = null;
	
	
	/**
	 * @param systemState
	 */
	public AbstractGenerator(SystemState systemState, BasicDiagnostic diagnostic) {
		super();
		this.systemState = systemState;
		this.diagnostic = diagnostic;
	}


	/**
	 * Adds an annotation for new generated elements.
	 * The annotations might be used later by other plug-ins, e.g. to draw new elements.
	 * @param element
	 */
	
	protected void annotateAsGenerated(Element element) {
		EAnnotation annotation = element.createEAnnotation(AbstractChecker.ESCULAPA_NAMESPACE);
		annotation.getDetails().put("generated", "true");
	}
}
