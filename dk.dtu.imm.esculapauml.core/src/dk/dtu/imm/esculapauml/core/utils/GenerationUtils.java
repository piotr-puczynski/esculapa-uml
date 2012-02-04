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

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.uml2.uml.Element;

import dk.dtu.imm.esculapauml.core.checkers.AbstractChecker;

/**
 * @author Piotr J. Puczynski
 *
 */
public final class GenerationUtils {
	public static void annotateAsGenerated(Element element) {
		EAnnotation annotation = element.createEAnnotation(AbstractChecker.ESCULAPA_NAMESPACE);
		annotation.getDetails().put("generated", "true");
	}
}
