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

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.ElementImport;
import org.eclipse.uml2.uml.InstanceSpecification;
import org.eclipse.uml2.uml.InstanceValue;
import org.eclipse.uml2.uml.LiteralBoolean;
import org.eclipse.uml2.uml.LiteralInteger;
import org.eclipse.uml2.uml.LiteralNull;
import org.eclipse.uml2.uml.LiteralString;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.PrimitiveType;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.ValueSpecification;
import org.eclipse.uml2.uml.VisibilityKind;
import org.eclipse.uml2.uml.resource.UMLResource;

import dk.dtu.imm.esculapauml.core.checkers.Checker;

/**
 * Class translates from Java types to UML primitive types.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class UMLTypesUtil {

	/**
	 * @param result
	 * @param checker
	 * @param checkedObject
	 * @return
	 */
	public static ValueSpecification getObjectValue(Object value, Checker checker, Element context) {
		if (value instanceof String) {
			return getValue((String) value, checker, context);
		} else if (value instanceof Integer) {
			return getValue((Integer) value, checker, context);
		} else if (value instanceof Boolean) {
			return getValue((Boolean) value, checker, context);
		} else if (value instanceof InstanceSpecification) {
			return getValue((InstanceSpecification) value, checker, context);
		}
		return null;
	}

	/**
	 * Creates boolean value.
	 * 
	 * @param value
	 * @param checker
	 * @param context
	 * @return
	 */
	static public ValueSpecification getValue(Boolean value, Checker checker, Element context) {
		LiteralBoolean boolResult = UMLFactory.eINSTANCE.createLiteralBoolean();
		PrimitiveType booleanPrimitiveType = importPrimitiveType("Boolean", checker, context);
		boolResult.setType(booleanPrimitiveType);
		boolResult.setValue(value);
		return boolResult;
	}

	/**
	 * Creates string value.
	 * 
	 * @param value
	 * @param checker
	 * @param context
	 * @return
	 */
	static public ValueSpecification getValue(String value, Checker checker, Element context) {
		LiteralString strResult = UMLFactory.eINSTANCE.createLiteralString();
		PrimitiveType strPrimitiveType = importPrimitiveType("String", checker, context);
		strResult.setType(strPrimitiveType);
		strResult.setValue(value);
		return strResult;
	}

	/**
	 * Creates integer value.
	 * 
	 * @param value
	 * @param checker
	 * @param context
	 * @return
	 */
	static public ValueSpecification getValue(Integer value, Checker checker, Element context) {
		LiteralInteger intResult = UMLFactory.eINSTANCE.createLiteralInteger();
		PrimitiveType intPrimitiveType = importPrimitiveType("Integer", checker, context);
		intResult.setType(intPrimitiveType);
		intResult.setValue(value);
		return intResult;
	}

	/**
	 * Creates null value.
	 * 
	 * @return
	 */
	public static ValueSpecification getNullValue() {
		LiteralNull nullResult = UMLFactory.eINSTANCE.createLiteralNull();
		return nullResult;
	}

	/**
	 * Creates instance value.
	 * 
	 * @param value
	 * @param checker
	 * @param context
	 * @return
	 */
	static public ValueSpecification getValue(InstanceSpecification value, Checker checker, Element context) {
		InstanceValue instanceResult = UMLFactory.eINSTANCE.createInstanceValue();
		instanceResult.setType(value.getClassifiers().get(0));
		instanceResult.setInstance(value);
		return instanceResult;
	}

	/**
	 * Checks whether Java value can be converted to UML primitive type.
	 * 
	 * @param value
	 * @return
	 */
	static public boolean canBeConverted(Object value) {
		if (value instanceof String || value instanceof Integer || value instanceof Boolean || value instanceof InstanceSpecification) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Imports UML primitive type. If no import is found already existing, the
	 * new import is automatically created.
	 * 
	 * @param name
	 * @return
	 */
	static private PrimitiveType importPrimitiveType(String name, Checker checker, Element context) {
		// try to find if model already import an element on package or on model
		List<ElementImport> imports = filter(having(on(ElementImport.class).getName(), equalTo(name)), context.getNearestPackage().getElementImports());
		imports.addAll(filter(having(on(ElementImport.class).getName(), equalTo(name)), context.getModel().getElementImports()));
		int i = 0;
		while (!imports.isEmpty()) {
			ElementImport ei = imports.get(i);
			PackageableElement pe = ei.getImportedElement();
			if (pe instanceof PrimitiveType) {
				if (pe.getQualifiedName().equals("UMLPrimitiveTypes::" + name)) {
					return (PrimitiveType) pe;
				}
			}
			imports.remove(i++);
		}
		// if we are here, no good import was found, we need to import ourself
		PrimitiveType primitiveType = null;

		Model umlLibrary = (Model) loadLibrary(URI.createURI(UMLResource.UML_PRIMITIVE_TYPES_LIBRARY_URI), context);

		primitiveType = (PrimitiveType) umlLibrary.getOwnedType(name);

		checker.getSystemState().addGeneratedElement(context.getNearestPackage().createElementImport(primitiveType, VisibilityKind.PUBLIC_LITERAL));

		return primitiveType;
	}

	/**
	 * Loads library in the same resource set as for checked object.
	 * 
	 * @param uri
	 * @return
	 */
	static private org.eclipse.uml2.uml.Package loadLibrary(URI uri, Element context) {
		org.eclipse.uml2.uml.Package package_ = null;
		Resource eResource = context.getNearestPackage().eResource();
		ResourceSet resourceSet = eResource == null ? null : eResource.getResourceSet();
		Resource resource = resourceSet.getResource(uri, true);
		package_ = (org.eclipse.uml2.uml.Package) EcoreUtil.getObjectByType(resource.getContents(), UMLPackage.Literals.PACKAGE);
		return package_;
	}
}
