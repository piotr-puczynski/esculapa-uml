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
package dk.dtu.imm.esculapauml.core.ocl.convert;

import java.util.Collection;

import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.ValueSpecification;

import dk.dtu.imm.esculapauml.core.checkers.Checker;
import dk.dtu.imm.esculapauml.core.utils.UMLTypesUtil;

/**
 * Converts OCL results to UML values.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public final class OclToUmlConverter {
	public static ValueSpecification convertToOCLSingleValue(Object oclValue, Checker checker, Element errorContext) throws OCLConversionException {
		if (oclValue instanceof ValueSpecification) {
			return (ValueSpecification) oclValue;
		}
		if (oclValue instanceof Collection) {
			@SuppressWarnings("rawtypes")
			Collection collection = (Collection) oclValue;
			if (collection.isEmpty()) {
				throw new OCLEmptyCollectionException();
			} else if (collection.size() == 1) {
				oclValue = collection.toArray()[0];
				if (oclValue instanceof ValueSpecification) {
					return (ValueSpecification) oclValue;
				}
			} else {
				throw new OCLMultiplicityManyCollectionException();
			}
		}
		if (UMLTypesUtil.canBeConverted(oclValue)) {
			return UMLTypesUtil.getObjectValue(oclValue, checker, errorContext);
		}
		if (null == oclValue) {
			return UMLTypesUtil.getNullValue();
		}
		throw new OCLConversionException();
	}
}
