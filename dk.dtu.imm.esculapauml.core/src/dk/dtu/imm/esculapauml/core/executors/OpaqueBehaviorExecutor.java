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
import static ch.lambdaj.Lambda.join;
import static org.hamcrest.Matchers.equalTo;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.ElementImport;
import org.eclipse.uml2.uml.LiteralBoolean;
import org.eclipse.uml2.uml.LiteralInteger;
import org.eclipse.uml2.uml.LiteralString;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.OpaqueBehavior;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.PrimitiveType;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.ValueSpecification;
import org.eclipse.uml2.uml.VisibilityKind;
import org.eclipse.uml2.uml.resource.UMLResource;

import dk.dtu.imm.esculapauml.core.checkers.BehaviorChecker;
import dk.dtu.imm.esculapauml.core.checkers.TransitionReplyChecker;
import dk.dtu.imm.esculapauml.core.sal.SALEvaluationHelper;
import dk.dtu.imm.esculapauml.core.sal.parser.ParseException;
import dk.dtu.imm.esculapauml.core.sal.parser.SALAssignment;
import dk.dtu.imm.esculapauml.core.sal.parser.SALCall;
import dk.dtu.imm.esculapauml.core.sal.parser.SALIdentifier;
import dk.dtu.imm.esculapauml.core.sal.parser.SALIntegerConstant;
import dk.dtu.imm.esculapauml.core.sal.parser.SALLogicConstant;
import dk.dtu.imm.esculapauml.core.sal.parser.SALMemeberOp;
import dk.dtu.imm.esculapauml.core.sal.parser.SALNode;
import dk.dtu.imm.esculapauml.core.sal.parser.SALParameters;
import dk.dtu.imm.esculapauml.core.sal.parser.SALParser;
import dk.dtu.imm.esculapauml.core.sal.parser.SALParserVisitor;
import dk.dtu.imm.esculapauml.core.sal.parser.SALReplyStatement;
import dk.dtu.imm.esculapauml.core.sal.parser.SALRoot;
import dk.dtu.imm.esculapauml.core.sal.parser.SALStringConstant;
import dk.dtu.imm.esculapauml.core.sal.parser.SimpleNode;
import dk.dtu.imm.esculapauml.core.sal.parser.TokenMgrError;

/**
 * Executor to execute SAL statements as OpaqueBehavior in Effects of
 * transitions.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class OpaqueBehaviorExecutor extends AbstractInstanceExecutor implements SALParserVisitor {

	protected OpaqueBehavior behavior;
	protected TransitionReplyChecker trc;
	protected SALNode root = null;
	protected BehaviorChecker checker;
	protected InstanceExecutor parent;
	public static final String LANG_ID = "SAL";

	/**
	 * @param checker
	 */
	public OpaqueBehaviorExecutor(InstanceExecutor instanceExecutor, TransitionReplyChecker trc) {
		super(instanceExecutor);
		parent = instanceExecutor;
		checker = (BehaviorChecker) instanceExecutor.getChecker();
		this.trc = trc;
		this.behavior = (OpaqueBehavior) trc.getCheckedObject().getEffect();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dk.dtu.imm.esculapauml.core.executors.ExecutorInterface#prepare()
	 */
	@Override
	public void prepare() {
		// the executor might work in two modes
		// advanced when the SAL string is taken from the bodies according to
		// meta-model
		// and with simple when the body is taken from the name (only if
		// advanced elements are not specified)
		String toParse;
		if (isInAdvancedMode()) {
			toParse = calculateBodyAdvanced();
		} else {
			toParse = calculateBodySimple();
		}
		SALParser parser = new SALParser(toParse);
		try {
			root = parser.parse();
		} catch (ParseException e) {
			trc.addProblem(Diagnostic.ERROR, "[SAL] Parse error: " + e.getMessage());
		} catch (TokenMgrError e) {
			trc.addProblem(Diagnostic.ERROR, "[SAL] " + e.getMessage());
		}
	}

	/**
	 * Calculate body from a name.
	 * 
	 * @return
	 */
	protected String calculateBodySimple() {
		return behavior.getName();
	}

	/**
	 * Calculate body from the bodies and languages.
	 * 
	 * @return
	 */
	protected String calculateBodyAdvanced() {
		int i = 0;
		List<String> bodies = new ArrayList<String>();
		for (String lang : behavior.getLanguages()) {
			if (lang.equalsIgnoreCase(LANG_ID)) {
				if (behavior.getBodies().size() > i) {
					bodies.add(behavior.getBodies().get(i));
				}
			}
			++i;
		}
		return join(bodies, ";");
	}

	/**
	 * If the mode is advanced the meta-model elements are used to get bodies of
	 * behavior.
	 * 
	 * @return
	 */
	protected boolean isInAdvancedMode() {
		return !behavior.getLanguages().isEmpty() || !behavior.getBodies().isEmpty();
	}

	/**
	 * Executes a syntax tree in root.
	 * 
	 */
	public void execute() {
		if (null != root) {
			SALEvaluationHelper helper = new SALEvaluationHelper();
			root.jjtAccept(this, helper);
		}
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.executors.InstanceExecutor#callOperation(
	 * org.eclipse.uml2.uml.Operation, org.eclipse.emf.common.util.EList,
	 * boolean, org.eclipse.uml2.uml.Element)
	 */
	@Override
	public ValueSpecification callOperation(Object source, Operation operation, EList<ValueSpecification> arguments, boolean isSynchronous, Element errorContext) {
		// redirect calls to parent
		return parent.callOperation(source, operation, arguments, isSynchronous, errorContext);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.sal.parser.SALParserVisitor#visit(dk.dtu.
	 * imm.esculapauml.core.sal.parser.SimpleNode,
	 * dk.dtu.imm.esculapauml.core.sal.SALEvaluationHelper)
	 */
	@Override
	public ValueSpecification visit(SimpleNode node, SALEvaluationHelper data) {
		// not visiting
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.sal.parser.SALParserVisitor#visit(dk.dtu.
	 * imm.esculapauml.core.sal.parser.SALRoot,
	 * dk.dtu.imm.esculapauml.core.sal.SALEvaluationHelper)
	 */
	@Override
	public ValueSpecification visit(SALRoot node, SALEvaluationHelper data) {
		ValueSpecification result = null;
		for (int i = 0; !checker.hasErrors() && i < node.jjtGetNumChildren(); ++i) {
			result = node.getChild(i).jjtAccept(this, data);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.sal.parser.SALParserVisitor#visit(dk.dtu.
	 * imm.esculapauml.core.sal.parser.SALAssignment,
	 * dk.dtu.imm.esculapauml.core.sal.SALEvaluationHelper)
	 */
	@Override
	public ValueSpecification visit(SALAssignment node, SALEvaluationHelper data) {
		String varName = (String) node.jjtGetValue();
		ValueSpecification value = node.getChild(0).jjtAccept(this, data);
		if (null != value) {
			if (!setVariable(varName, value, trc.getCheckedObject())) {
				return null;
			}
		}
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.sal.parser.SALParserVisitor#visit(dk.dtu.
	 * imm.esculapauml.core.sal.parser.SALReplyStatement,
	 * dk.dtu.imm.esculapauml.core.sal.SALEvaluationHelper)
	 */
	@Override
	public ValueSpecification visit(SALReplyStatement node, SALEvaluationHelper data) {
		ValueSpecification replyValue = node.getChild(0).jjtAccept(this, data);
		if (null != replyValue) {
			trc.setReply(replyValue);
		}
		return replyValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.sal.parser.SALParserVisitor#visit(dk.dtu.
	 * imm.esculapauml.core.sal.parser.SALIntegerConstant,
	 * dk.dtu.imm.esculapauml.core.sal.SALEvaluationHelper)
	 */
	@Override
	public ValueSpecification visit(SALIntegerConstant node, SALEvaluationHelper data) {
		LiteralInteger intResult = UMLFactory.eINSTANCE.createLiteralInteger();
		PrimitiveType intPrimitiveType = importPrimitiveType("Integer");
		intResult.setType(intPrimitiveType);
		intResult.setValue((Integer) node.jjtGetValue());
		return intResult;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.sal.parser.SALParserVisitor#visit(dk.dtu.
	 * imm.esculapauml.core.sal.parser.SALLogicConstant,
	 * dk.dtu.imm.esculapauml.core.sal.SALEvaluationHelper)
	 */
	@Override
	public ValueSpecification visit(SALLogicConstant node, SALEvaluationHelper data) {
		LiteralBoolean boolResult = UMLFactory.eINSTANCE.createLiteralBoolean();
		PrimitiveType booleanPrimitiveType = importPrimitiveType("Boolean");
		boolResult.setType(booleanPrimitiveType);
		boolResult.setValue((Boolean) node.jjtGetValue());
		return boolResult;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.sal.parser.SALParserVisitor#visit(dk.dtu.
	 * imm.esculapauml.core.sal.parser.SALStringConstant,
	 * dk.dtu.imm.esculapauml.core.sal.SALEvaluationHelper)
	 */
	@Override
	public ValueSpecification visit(SALStringConstant node, SALEvaluationHelper data) {
		LiteralString strResult = UMLFactory.eINSTANCE.createLiteralString();
		PrimitiveType strPrimitiveType = importPrimitiveType("String");
		strResult.setType(strPrimitiveType);
		strResult.setValue((String) node.jjtGetValue());
		return strResult;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.sal.parser.SALParserVisitor#visit(dk.dtu.
	 * imm.esculapauml.core.sal.parser.SALIdentifier,
	 * dk.dtu.imm.esculapauml.core.sal.SALEvaluationHelper)
	 */
	@Override
	public ValueSpecification visit(SALIdentifier node, SALEvaluationHelper data) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.sal.parser.SALParserVisitor#visit(dk.dtu.
	 * imm.esculapauml.core.sal.parser.SALCall,
	 * dk.dtu.imm.esculapauml.core.sal.SALEvaluationHelper)
	 */
	@Override
	public ValueSpecification visit(SALCall node, SALEvaluationHelper data) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.sal.parser.SALParserVisitor#visit(dk.dtu.
	 * imm.esculapauml.core.sal.parser.SALMemeberOp,
	 * dk.dtu.imm.esculapauml.core.sal.SALEvaluationHelper)
	 */
	@Override
	public ValueSpecification visit(SALMemeberOp node, SALEvaluationHelper data) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.sal.parser.SALParserVisitor#visit(dk.dtu.
	 * imm.esculapauml.core.sal.parser.SALParameters,
	 * dk.dtu.imm.esculapauml.core.sal.SALEvaluationHelper)
	 */
	@Override
	public ValueSpecification visit(SALParameters node, SALEvaluationHelper data) {
		// TODO Auto-generated method stub
		return null;
	}

}
