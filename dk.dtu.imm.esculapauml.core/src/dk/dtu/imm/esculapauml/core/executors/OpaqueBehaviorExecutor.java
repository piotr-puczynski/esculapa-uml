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

import static ch.lambdaj.Lambda.join;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.log4j.Level;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.InstanceSpecification;
import org.eclipse.uml2.uml.OpaqueBehavior;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.ValueSpecification;

import dk.dtu.imm.esculapauml.core.checkers.BehaviorChecker;
import dk.dtu.imm.esculapauml.core.checkers.TransitionReplyChecker;
import dk.dtu.imm.esculapauml.core.ocl.OCLEvaluator;
import dk.dtu.imm.esculapauml.core.sal.SALEvaluationHelper;
import dk.dtu.imm.esculapauml.core.sal.parser.ParseException;
import dk.dtu.imm.esculapauml.core.sal.parser.SALAssignment;
import dk.dtu.imm.esculapauml.core.sal.parser.SALCall;
import dk.dtu.imm.esculapauml.core.sal.parser.SALIdentifier;
import dk.dtu.imm.esculapauml.core.sal.parser.SALIntegerConstant;
import dk.dtu.imm.esculapauml.core.sal.parser.SALLogicConstant;
import dk.dtu.imm.esculapauml.core.sal.parser.SALMemeberOp;
import dk.dtu.imm.esculapauml.core.sal.parser.SALNode;
import dk.dtu.imm.esculapauml.core.sal.parser.SALParser;
import dk.dtu.imm.esculapauml.core.sal.parser.SALParserVisitor;
import dk.dtu.imm.esculapauml.core.sal.parser.SALReplyStatement;
import dk.dtu.imm.esculapauml.core.sal.parser.SALRoot;
import dk.dtu.imm.esculapauml.core.sal.parser.SALStringConstant;
import dk.dtu.imm.esculapauml.core.sal.parser.SimpleNode;
import dk.dtu.imm.esculapauml.core.sal.parser.TokenMgrError;
import dk.dtu.imm.esculapauml.core.utils.UMLTypesUtil;

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
	 * dk.dtu.imm.esculapauml.core.executors.InstanceExecutor#callOperation(
	 * java.lang.Object, java.lang.String, org.eclipse.emf.common.util.EList,
	 * boolean, org.eclipse.uml2.uml.Element)
	 */
	@Override
	public ValueSpecification callOperation(Object source, String operationName, EList<ValueSpecification> arguments, boolean isSynchronous,
			Element errorContext) {
		// redirect calls to parent
		return parent.callOperation(source, operationName, arguments, isSynchronous, errorContext);
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
		return UMLTypesUtil.getValue((Integer) node.jjtGetValue(), checker, checker.getCheckedObject());
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
		return UMLTypesUtil.getValue((Boolean) node.jjtGetValue(), checker, checker.getCheckedObject());
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
		return UMLTypesUtil.getValue((String) node.jjtGetValue(), checker, checker.getCheckedObject());
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
		String name = (String) node.jjtGetValue();
		data.pushEvaluationContext(name);
		String oclExpression = data.getEvaluationContextExpression();
		data.popEvaluationContext();
		OCLEvaluator ocl = new OCLEvaluator(checker, getInstanceSpecification(), trc.getCheckedObject());
		ocl.setDebug(logger.getEffectiveLevel() == Level.DEBUG);
		Object result = ocl.evaluate(oclExpression);
		if (ocl.hasErrors() || null == result) {
			return null;
		}
		if (result instanceof ValueSpecification) {
			return (ValueSpecification) result;
		}
		if (UMLTypesUtil.canBeConverted(result)) {
			return UMLTypesUtil.getObjectValue(result, checker, checker.getCheckedObject());
		}
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
		Object instance = getInstanceSpecification();
		String name = (String) node.jjtGetValue();
		if (data.hasEvaluationContext()) {
			String oclExpression = data.getEvaluationContextExpression();
			OCLEvaluator ocl = new OCLEvaluator(checker, getInstanceSpecification(), trc.getCheckedObject());
			ocl.setDebug(logger.getEffectiveLevel() == Level.DEBUG);
			instance = ocl.evaluate(oclExpression);
			if (ocl.hasErrors()) {
				return null;
			}
		}
		if (instance instanceof InstanceSpecification) {
			InstanceExecutor executor = checker.getSystemState().getInstanceExecutor((InstanceSpecification) instance);
			if (null == executor) {
				trc.addProblem(Diagnostic.ERROR, "[SAL] Cannot obtain executor for operation: '" + name + "'.");
			} else {
				// evaluate arguments
				EList<ValueSpecification> arguments = new BasicEList<ValueSpecification>();
				Stack<String> oldContext = data.swapEvaluationContext(new Stack<String>());
				for (int i = 0; !checker.hasErrors() && i < node.jjtGetNumChildren(); ++i) {
					arguments.add(node.getChild(i).jjtAccept(this, data));
				}
				data.swapEvaluationContext(oldContext);
				if (checker.hasErrors()) {
					return null;
				}
				return executor.callOperation(parent, name, arguments, true, trc.getCheckedObject());
			}

		} else {
			trc.addProblem(Diagnostic.ERROR, "[SAL] Operation: '" + name + "' must be called in the context of existing instance specification.");
		}
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
		data.pushEvaluationContext((String) node.jjtGetValue());
		ValueSpecification result = node.getChild(0).jjtAccept(this, data);
		data.popEvaluationContext();
		return result;
	}

}
