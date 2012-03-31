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
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Level;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.InstanceSpecification;
import org.eclipse.uml2.uml.InstanceValue;
import org.eclipse.uml2.uml.OpaqueBehavior;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.ValueSpecification;

import dk.dtu.imm.esculapauml.core.checkers.BehaviorChecker;
import dk.dtu.imm.esculapauml.core.checkers.TransitionReplyChecker;
import dk.dtu.imm.esculapauml.core.ocl.OCLEvaluator;
import dk.dtu.imm.esculapauml.core.sal.SALEvaluationHelper;
import dk.dtu.imm.esculapauml.core.sal.parser.ParseException;
import dk.dtu.imm.esculapauml.core.sal.parser.SALAdd;
import dk.dtu.imm.esculapauml.core.sal.parser.SALAnd;
import dk.dtu.imm.esculapauml.core.sal.parser.SALAssignment;
import dk.dtu.imm.esculapauml.core.sal.parser.SALCall;
import dk.dtu.imm.esculapauml.core.sal.parser.SALDiv;
import dk.dtu.imm.esculapauml.core.sal.parser.SALIdent;
import dk.dtu.imm.esculapauml.core.sal.parser.SALIntegerConstant;
import dk.dtu.imm.esculapauml.core.sal.parser.SALLogicConstant;
import dk.dtu.imm.esculapauml.core.sal.parser.SALMember;
import dk.dtu.imm.esculapauml.core.sal.parser.SALMemberCall;
import dk.dtu.imm.esculapauml.core.sal.parser.SALMult;
import dk.dtu.imm.esculapauml.core.sal.parser.SALNode;
import dk.dtu.imm.esculapauml.core.sal.parser.SALNot;
import dk.dtu.imm.esculapauml.core.sal.parser.SALNullConstant;
import dk.dtu.imm.esculapauml.core.sal.parser.SALOCLExpression;
import dk.dtu.imm.esculapauml.core.sal.parser.SALOr;
import dk.dtu.imm.esculapauml.core.sal.parser.SALParser;
import dk.dtu.imm.esculapauml.core.sal.parser.SALParserVisitor;
import dk.dtu.imm.esculapauml.core.sal.parser.SALReplyStatement;
import dk.dtu.imm.esculapauml.core.sal.parser.SALRoot;
import dk.dtu.imm.esculapauml.core.sal.parser.SALStringConstant;
import dk.dtu.imm.esculapauml.core.sal.parser.SALSubstract;
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
			SALEvaluationHelper helper = new SALEvaluationHelper(getInstanceSpecification());
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
	public ValueSpecification callOperation(Object source, InstanceSpecification caller, Operation operation, EList<ValueSpecification> arguments,
			boolean isSynchronous, Element errorContext) {
		// redirect calls to parent
		return parent.callOperation(source, caller, operation, arguments, isSynchronous, errorContext);
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
	public ValueSpecification callOperation(Object source, InstanceSpecification caller, String operationName, EList<ValueSpecification> arguments,
			boolean isSynchronous, Element errorContext) {
		// redirect calls to parent
		return parent.callOperation(source, caller, operationName, arguments, isSynchronous, errorContext);
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
		for (int i = 0; !checker.hasErrors() && i < node.jjtGetNumChildren(); ++i) {
			node.getChild(i).jjtAccept(this, data);
		}
		return null;
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
			setVariable(varName, value, trc.getCheckedObject());
		}
		return null;
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
		return null;
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
	 * imm.esculapauml.core.sal.parser.SALNullConstant,
	 * dk.dtu.imm.esculapauml.core.sal.SALEvaluationHelper)
	 */
	@Override
	public ValueSpecification visit(SALNullConstant node, SALEvaluationHelper data) {
		return UMLTypesUtil.getNullValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.sal.parser.SALParserVisitor#visit(dk.dtu.
	 * imm.esculapauml.core.sal.parser.SALCall,
	 * dk.dtu.imm.esculapauml.core.sal.SALEvaluationHelper)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public ValueSpecification visit(SALCall node, SALEvaluationHelper data) {
		Object instance = data.getFunctionEvaluationContext();
		String name = (String) node.jjtGetValue();
		if (!(instance instanceof Collection)) {
			// create collection
			ArrayList<Object> array = new ArrayList<Object>();
			array.add(instance);
			instance = array;
		}
		ValueSpecification result = null;
		for (Object instanceObject : (Collection) instance) {
			if (instanceObject instanceof InstanceValue) {
				instanceObject = ((InstanceValue) instanceObject).getInstance();
			}
			if (instanceObject instanceof InstanceSpecification) {
				InstanceExecutor executor = checker.getSystemState().getInstanceExecutor((InstanceSpecification) instanceObject);
				if (null == executor) {
					trc.addProblem(Diagnostic.ERROR, "[SAL] Cannot obtain executor for operation: '" + name + "'.");
				} else {
					// evaluate arguments in fresh context
					Object oldContext = data.setFunctionEvaluationContext(getInstanceSpecification());
					EList<ValueSpecification> arguments = new BasicEList<ValueSpecification>();
					for (int i = 0; !checker.hasErrors() && i < node.jjtGetNumChildren(); ++i) {
						arguments.add(node.getChild(i).jjtAccept(this, data));
					}
					data.setFunctionEvaluationContext(oldContext);
					if (checker.hasErrors()) {
						return null;
					}
					result = executor.callOperation(parent, getInstanceSpecification(), name, arguments, true, trc.getCheckedObject());
				}

			} else {
				trc.addProblem(Diagnostic.ERROR, "[SAL] Operation: '" + name + "' must be called in the context of existing instance specification.");
				break;
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.sal.parser.SALParserVisitor#visit(dk.dtu.
	 * imm.esculapauml.core.sal.parser.SALOCLExpression,
	 * dk.dtu.imm.esculapauml.core.sal.SALEvaluationHelper)
	 */
	@Override
	public ValueSpecification visit(SALOCLExpression node, SALEvaluationHelper data) {
		String oclExpression = (String) node.jjtGetValue();
		OCLEvaluator ocl = new OCLEvaluator(checker, getInstanceSpecification(), trc.getCheckedObject());
		ocl.setDebug(logger.getEffectiveLevel() == Level.DEBUG);
		Object result = ocl.evaluate(oclExpression);
		if (ocl.hasErrors()) {
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
	 * imm.esculapauml.core.sal.parser.SALOr,
	 * dk.dtu.imm.esculapauml.core.sal.SALEvaluationHelper)
	 */
	@Override
	public ValueSpecification visit(SALOr node, SALEvaluationHelper data) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.sal.parser.SALParserVisitor#visit(dk.dtu.
	 * imm.esculapauml.core.sal.parser.SALAnd,
	 * dk.dtu.imm.esculapauml.core.sal.SALEvaluationHelper)
	 */
	@Override
	public ValueSpecification visit(SALAnd node, SALEvaluationHelper data) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.sal.parser.SALParserVisitor#visit(dk.dtu.
	 * imm.esculapauml.core.sal.parser.SALAdd,
	 * dk.dtu.imm.esculapauml.core.sal.SALEvaluationHelper)
	 */
	@Override
	public ValueSpecification visit(SALAdd node, SALEvaluationHelper data) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.sal.parser.SALParserVisitor#visit(dk.dtu.
	 * imm.esculapauml.core.sal.parser.SALSubstract,
	 * dk.dtu.imm.esculapauml.core.sal.SALEvaluationHelper)
	 */
	@Override
	public ValueSpecification visit(SALSubstract node, SALEvaluationHelper data) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.sal.parser.SALParserVisitor#visit(dk.dtu.
	 * imm.esculapauml.core.sal.parser.SALMult,
	 * dk.dtu.imm.esculapauml.core.sal.SALEvaluationHelper)
	 */
	@Override
	public ValueSpecification visit(SALMult node, SALEvaluationHelper data) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.sal.parser.SALParserVisitor#visit(dk.dtu.
	 * imm.esculapauml.core.sal.parser.SALDiv,
	 * dk.dtu.imm.esculapauml.core.sal.SALEvaluationHelper)
	 */
	@Override
	public ValueSpecification visit(SALDiv node, SALEvaluationHelper data) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.sal.parser.SALParserVisitor#visit(dk.dtu.
	 * imm.esculapauml.core.sal.parser.SALNot,
	 * dk.dtu.imm.esculapauml.core.sal.SALEvaluationHelper)
	 */
	@Override
	public ValueSpecification visit(SALNot node, SALEvaluationHelper data) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.sal.parser.SALParserVisitor#visit(dk.dtu.
	 * imm.esculapauml.core.sal.parser.SALMemberCall,
	 * dk.dtu.imm.esculapauml.core.sal.SALEvaluationHelper)
	 */
	@Override
	public ValueSpecification visit(SALMemberCall node, SALEvaluationHelper data) {
		// first child is a given context, second child is SALCall
		ValueSpecification context = node.getChild(0).jjtAccept(this, data);
		if (null != context) {
			Object oldContext = data.setFunctionEvaluationContext(context);
			ValueSpecification result = node.getChild(1).jjtAccept(this, data);
			data.setFunctionEvaluationContext(oldContext);
			return result;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.sal.parser.SALParserVisitor#visit(dk.dtu.
	 * imm.esculapauml.core.sal.parser.SALMember,
	 * dk.dtu.imm.esculapauml.core.sal.SALEvaluationHelper)
	 */
	@Override
	public ValueSpecification visit(SALMember node, SALEvaluationHelper data) {
		// member second child will be always SALIdent that we will not evaluate
		// but just grab its name
		ValueSpecification contextSpec = node.getChild(0).jjtAccept(this, data);
		if (contextSpec instanceof InstanceValue) {
			InstanceSpecification context = ((InstanceValue) contextSpec).getInstance();
			String navigation = (String) node.getChild(1).jjtGetValue();
			OCLEvaluator ocl = new OCLEvaluator(checker, context, trc.getCheckedObject());
			ocl.setDebug(logger.getEffectiveLevel() == Level.DEBUG);
			Object result = ocl.evaluate(navigation);
			if (ocl.hasErrors() || null == result) {
				return null;
			}
			if (result instanceof ValueSpecification) {
				return (ValueSpecification) result;
			}
			if (UMLTypesUtil.canBeConverted(result)) {
				return UMLTypesUtil.getObjectValue(result, checker, checker.getCheckedObject());
			}
		} else {
			// TODO: error
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.sal.parser.SALParserVisitor#visit(dk.dtu.
	 * imm.esculapauml.core.sal.parser.SALIdent,
	 * dk.dtu.imm.esculapauml.core.sal.SALEvaluationHelper)
	 */
	@Override
	public ValueSpecification visit(SALIdent node, SALEvaluationHelper data) {
		// always evaluate this from self
		String name = (String) node.jjtGetValue();
		OCLEvaluator ocl = new OCLEvaluator(checker, getInstanceSpecification(), trc.getCheckedObject());
		ocl.setDebug(logger.getEffectiveLevel() == Level.DEBUG);
		Object result = ocl.evaluate(name);
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

}
