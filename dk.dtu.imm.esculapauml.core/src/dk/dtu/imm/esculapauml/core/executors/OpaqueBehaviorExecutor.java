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
package dk.dtu.imm.esculapauml.core.executors;

import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static org.hamcrest.Matchers.equalTo;

import java.util.List;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.ElementImport;
import org.eclipse.uml2.uml.InstanceSpecification;
import org.eclipse.uml2.uml.LiteralBoolean;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.PrimitiveType;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.ValueSpecification;
import org.eclipse.uml2.uml.VisibilityKind;
import org.eclipse.uml2.uml.resource.UMLResource;

import dk.dtu.imm.esculapauml.core.checkers.BehaviorChecker;
import dk.dtu.imm.esculapauml.core.sal.parser.ParseException;
import dk.dtu.imm.esculapauml.core.sal.parser.SALNode;
import dk.dtu.imm.esculapauml.core.sal.parser.SALParser;
import dk.dtu.imm.esculapauml.core.sal.parser.SALParserTreeConstants;
import dk.dtu.imm.esculapauml.core.sal.parser.TokenMgrError;

/**
 * Executor to execute SAL statements as OpaqueBehavior in Effects of
 * transitions.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class OpaqueBehaviorExecutor extends AbstractInstanceExecutor<BehaviorChecker> {

	private String behavior;
	private EObject owner;
	private SALNode root = null;
	ValueSpecification reply = null;

	/**
	 * @return the reply
	 */
	public ValueSpecification getReply() {
		return reply;
	}

	/**
	 * Checks if reply exists.
	 * 
	 * @return boolean
	 */
	public boolean hasReply() {
		return null != reply;
	}

	/**
	 * @param checker
	 */
	public OpaqueBehaviorExecutor(BehaviorChecker checker, InstanceSpecification instanceSpecification, EObject owner, String behavior) {
		super(checker, instanceSpecification);
		this.owner = owner;
		if (null != behavior) {
			this.behavior = behavior;
		} else {
			this.behavior = "";
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dk.dtu.imm.esculapauml.core.executors.ExecutorInterface#prepare()
	 */
	@Override
	public void prepare() {
		SALParser parser = new SALParser(behavior);
		try {
			root = parser.parse();
		} catch (ParseException e) {
			checker.addOtherProblem(Diagnostic.ERROR, "[SAL] Parse error: " + e.getMessage(), owner);
		} catch (TokenMgrError e) {
			checker.addOtherProblem(Diagnostic.ERROR, "[SAL] " + e.getMessage(), owner);
		}
	}

	/**
	 * Executes a syntax tree in root.
	 * 
	 */
	public void execute() {
		for (int i = 0; !checker.hasErrors() && null != root && i < root.jjtGetNumChildren(); ++i) {
			executeNode(root.getChild(i));
		}
	}

	/**
	 * Executes first level nodes.
	 * 
	 * @param node
	 */
	protected void executeNode(SALNode node) {
		switch (node.getId()) {
		case SALParserTreeConstants.JJTREPLYSTATEMENT:
			executeReply(node);
			break;
		default:
			evaluateExpression(node);
			// checker.addOtherProblem(Diagnostic.ERROR,
			// "[SAL] Statement not allowed here:  " + node.toString(), owner);
		}
	}

	/**
	 * Executes reply. Reply result is written to local variable and then it is
	 * possible to get it.
	 * 
	 * @param node
	 */
	protected void executeReply(SALNode node) {
		reply = evaluateExpression(node.getChild(0));
	}

	/**
	 * Evaluates any kind of expression.
	 * 
	 * @param node
	 * @return
	 */
	protected ValueSpecification evaluateExpression(SALNode node) {
		ValueSpecification result = null;
		switch (node.getId()) {
		case SALParserTreeConstants.JJTLOGICCONSTANT:
			result = evaluateLogicalConstant(node);
			break;
		}
		return result;
	}

	/**
	 * Compiles SAL Logical Constant to UML Literal.
	 * 
	 * @param node
	 * @return
	 */
	protected ValueSpecification evaluateLogicalConstant(SALNode node) {
		LiteralBoolean boolResult = UMLFactory.eINSTANCE.createLiteralBoolean();
		PrimitiveType booleanPrimitiveType = importPrimitiveType("Boolean");
		boolResult.setType(booleanPrimitiveType);
		boolResult.setValue((boolean) node.jjtGetValue());
		return boolResult;
	}

	/**
	 * Imports UML primitive type. If no import is found already existing, the
	 * new import is automatically created.
	 * 
	 * @param name
	 * @return
	 */
	protected PrimitiveType importPrimitiveType(String name) {
		// try to find if model already import an element on package or on model
		List<ElementImport> imports = filter(having(on(ElementImport.class).getName(), equalTo(name)), checker.getCheckedObject().getNearestPackage()
				.getElementImports());
		imports.addAll(filter(having(on(ElementImport.class).getName(), equalTo(name)), checker.getCheckedObject().getModel().getElementImports()));
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

		Model umlLibrary = (Model) loadLibrary(URI.createURI(UMLResource.UML_PRIMITIVE_TYPES_LIBRARY_URI));

		primitiveType = (PrimitiveType) umlLibrary.getOwnedType(name);

		checker.getSystemState().addGeneratedElement(
				checker.getCheckedObject().getNearestPackage().createElementImport(primitiveType, VisibilityKind.PUBLIC_LITERAL));

		return primitiveType;
	}

	/**
	 * Loads library in the same resource set as for checked object.
	 * 
	 * @param uri
	 * @return
	 */
	protected org.eclipse.uml2.uml.Package loadLibrary(URI uri) {
		org.eclipse.uml2.uml.Package package_ = null;
		Resource eResource = checker.getCheckedObject().getNearestPackage().eResource();
		ResourceSet resourceSet = eResource == null ? null : eResource.getResourceSet();
		Resource resource = resourceSet.getResource(uri, true);
		package_ = (org.eclipse.uml2.uml.Package) EcoreUtil.getObjectByType(resource.getContents(), UMLPackage.Literals.PACKAGE);
		return package_;
	}

}
