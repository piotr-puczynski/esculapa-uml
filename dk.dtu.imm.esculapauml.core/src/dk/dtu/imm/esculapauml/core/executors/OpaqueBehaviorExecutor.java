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
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.InstanceSpecification;
import org.eclipse.uml2.uml.InstanceValue;
import org.eclipse.uml2.uml.LiteralBoolean;
import org.eclipse.uml2.uml.LiteralInteger;
import org.eclipse.uml2.uml.OpaqueBehavior;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.ValueSpecification;

import dk.dtu.imm.esculapauml.core.checkers.BehaviorChecker;
import dk.dtu.imm.esculapauml.core.checkers.TransitionReplyChecker;
import dk.dtu.imm.esculapauml.core.collections.CallArguments;
import dk.dtu.imm.esculapauml.core.collections.OCLConversionException;
import dk.dtu.imm.esculapauml.core.collections.ValuesCollection;
import dk.dtu.imm.esculapauml.core.collections.ValuesList;
import dk.dtu.imm.esculapauml.core.executors.coordination.EsculapaCallEvent;
import dk.dtu.imm.esculapauml.core.ocl.OCLEvaluator;
import dk.dtu.imm.esculapauml.core.sal.SALEvaluationHelper;
import dk.dtu.imm.esculapauml.core.sal.parser.ParseException;
import dk.dtu.imm.esculapauml.core.sal.parser.SALAdd;
import dk.dtu.imm.esculapauml.core.sal.parser.SALAnd;
import dk.dtu.imm.esculapauml.core.sal.parser.SALAssignment;
import dk.dtu.imm.esculapauml.core.sal.parser.SALAssignmentSelector;
import dk.dtu.imm.esculapauml.core.sal.parser.SALCall;
import dk.dtu.imm.esculapauml.core.sal.parser.SALCallAsync;
import dk.dtu.imm.esculapauml.core.sal.parser.SALCallSelector;
import dk.dtu.imm.esculapauml.core.sal.parser.SALCollectionExpression;
import dk.dtu.imm.esculapauml.core.sal.parser.SALDiv;
import dk.dtu.imm.esculapauml.core.sal.parser.SALIdent;
import dk.dtu.imm.esculapauml.core.sal.parser.SALIdentSelector;
import dk.dtu.imm.esculapauml.core.sal.parser.SALIntegerConstant;
import dk.dtu.imm.esculapauml.core.sal.parser.SALLogicConstant;
import dk.dtu.imm.esculapauml.core.sal.parser.SALMember;
import dk.dtu.imm.esculapauml.core.sal.parser.SALMemberCall;
import dk.dtu.imm.esculapauml.core.sal.parser.SALMod;
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
import dk.dtu.imm.esculapauml.core.sal.parser.SALSelector;
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
	public ValuesCollection callOperation(Object source, InstanceSpecification caller, Operation operation, CallArguments arguments, boolean isSynchronous,
			Element errorContext) {
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
	public ValuesCollection callOperation(Object source, InstanceSpecification caller, String operationName, CallArguments arguments, boolean isSynchronous,
			Element errorContext) {
		// redirect calls to parent
		return parent.callOperation(source, caller, operationName, arguments, isSynchronous, errorContext);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.executors.InstanceExecutor#callOperation(
	 * dk.dtu.imm.esculapauml.core.executors.coordination.EsculapaCallEvent)
	 */
	@Override
	public ValuesCollection callOperation(EsculapaCallEvent event) {
		return parent.callOperation(event);
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
	public ValuesCollection visit(SimpleNode node, SALEvaluationHelper data) {
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
	public ValuesCollection visit(SALRoot node, SALEvaluationHelper data) {
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
	public ValuesCollection visit(SALAssignment node, SALEvaluationHelper data) {
		String varName = (String) node.jjtGetValue();
		ValuesCollection value = node.getChild(0).jjtAccept(this, data);
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
	 * imm.esculapauml.core.sal.parser.SALAssignmentSelector,
	 * dk.dtu.imm.esculapauml.core.sal.SALEvaluationHelper)
	 */
	@Override
	public ValuesCollection visit(SALAssignmentSelector node, SALEvaluationHelper data) {
		// assign single value
		// first is selector, then expression to assign
		String varName = (String) node.jjtGetValue();
		ValuesCollection values = node.getChild(1).jjtAccept(this, data);
		if (null != values) {
			if (values.isSingleValued(trc)) {
				ValuesCollection selector = node.getChild(0).jjtAccept(this, data);
				if (null != selector) {
					ValueSpecification value = values.get(0);
					setVariable(varName, ((LiteralInteger) selector.get(0)).getValue(), value, trc.getCheckedObject());
				}
			}
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
	public ValuesCollection visit(SALReplyStatement node, SALEvaluationHelper data) {
		ValuesCollection replyValue = node.getChild(0).jjtAccept(this, data);
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
	public ValuesCollection visit(SALIntegerConstant node, SALEvaluationHelper data) {
		return new ValuesList(UMLTypesUtil.getValue((Integer) node.jjtGetValue(), checker, checker.getCheckedObject()));
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
	public ValuesCollection visit(SALLogicConstant node, SALEvaluationHelper data) {
		return new ValuesList(UMLTypesUtil.getValue((Boolean) node.jjtGetValue(), checker, checker.getCheckedObject()));
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
	public ValuesCollection visit(SALStringConstant node, SALEvaluationHelper data) {
		return new ValuesList(UMLTypesUtil.getValue((String) node.jjtGetValue(), checker, checker.getCheckedObject()));
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
	public ValuesCollection visit(SALNullConstant node, SALEvaluationHelper data) {
		return new ValuesList(UMLTypesUtil.getNullValue());
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
	public ValuesCollection visit(SALCall node, SALEvaluationHelper data) {
		return callOperation(node, data, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.sal.parser.SALParserVisitor#visit(dk.dtu.
	 * imm.esculapauml.core.sal.parser.SALCallAsynch,
	 * dk.dtu.imm.esculapauml.core.sal.SALEvaluationHelper)
	 */
	@Override
	public ValuesCollection visit(SALCallAsync node, SALEvaluationHelper data) {
		return callOperation(node, data, false);
	}

	@SuppressWarnings("rawtypes")
	private ValuesCollection callOperation(SALNode node, SALEvaluationHelper data, boolean isSynchronous) {
		Object instances = data.getFunctionEvaluationContext();
		String name = (String) node.jjtGetValue();
		ValuesCollection result = new ValuesList();
		if (!(instances instanceof Collection)) {
			List<Object> list = new ArrayList<Object>();
			list.add(instances);
			instances = list;
		}
		for (Object instance : (Collection) instances) {
			if (instance instanceof InstanceValue) {
				instance = ((InstanceValue) instance).getInstance();
			}
			if (instance instanceof InstanceSpecification) {
				InstanceExecutor executor = checker.getSystemState().getInstanceExecutor((InstanceSpecification) instance);
				if (null == executor) {
					trc.addProblem(Diagnostic.ERROR, "[SAL] Cannot obtain executor for operation: '" + name + "'.");
					break;
				} else {
					// evaluate arguments in fresh context
					Object oldContext = data.setFunctionEvaluationContext(getInstanceSpecification());
					CallArguments arguments = new CallArguments();
					for (int i = 0; !checker.hasErrors() && i < node.jjtGetNumChildren(); ++i) {
						arguments.addArgument(node.getChild(i).jjtAccept(this, data));
					}
					data.setFunctionEvaluationContext(oldContext);
					if (checker.hasErrors()) {
						return null;
					}
					ValuesCollection partResult = executor.callOperation(parent, getInstanceSpecification(), name, arguments, isSynchronous,
							trc.getCheckedObject());
					if (null != partResult) {
						result.addAll(partResult);
					}
				}

			} else {
				trc.addProblem(Diagnostic.ERROR, "[SAL] Operation: '" + name + "' must be called in the context of existing instance specification.");
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
	public ValuesCollection visit(SALOCLExpression node, SALEvaluationHelper data) {
		String oclExpression = (String) node.jjtGetValue();
		OCLEvaluator ocl = checker.getSystemState().getOcl();
		ocl.setDebug(logger.getEffectiveLevel() == Level.DEBUG);
		Object result = ocl.evaluate(checker, getInstanceSpecification(), trc.getCheckedObject(), oclExpression);
		if (ocl.hasErrors()) {
			return null;
		}

		ValuesCollection umlResult = new ValuesList();
		try {
			umlResult.addFromOCL(result, checker, checker.getCheckedObject());
		} catch (OCLConversionException e) {
			trc.addProblem(Diagnostic.ERROR, "[SAL] Convertion from OCL ('" + oclExpression + "') to UML failed on object: " + e.getOclValue().toString());
			return null;
		}

		return umlResult;
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
	public ValuesCollection visit(SALOr node, SALEvaluationHelper data) {
		ValuesCollection args1 = node.getChild(0).jjtAccept(this, data);
		if (args1.isSingleValued(trc)) {
			ValueSpecification arg1 = args1.get(0);
			if (arg1 instanceof LiteralBoolean) {
				ValuesCollection args2 = node.getChild(1).jjtAccept(this, data);
				if (args2.isSingleValued(trc)) {
					ValueSpecification arg2 = args2.get(0);
					if (arg2 instanceof LiteralBoolean) {
						return new ValuesList(UMLTypesUtil.getValue(((LiteralBoolean) arg1).isValue() || ((LiteralBoolean) arg2).isValue(), checker,
								checker.getCheckedObject()));
					} else {
						trc.addProblem(Diagnostic.ERROR, "[SAL] Cannot convert '" + arg2 + "' to Boolean.");
					}
				}
			} else {
				trc.addProblem(Diagnostic.ERROR, "[SAL] Cannot convert '" + arg1 + "' to Boolean.");
			}
		}
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
	public ValuesCollection visit(SALAnd node, SALEvaluationHelper data) {
		ValuesCollection args1 = node.getChild(0).jjtAccept(this, data);
		if (args1.isSingleValued(trc)) {
			ValueSpecification arg1 = args1.get(0);
			if (arg1 instanceof LiteralBoolean) {
				ValuesCollection args2 = node.getChild(1).jjtAccept(this, data);
				if (args2.isSingleValued(trc)) {
					ValueSpecification arg2 = args2.get(0);
					if (arg2 instanceof LiteralBoolean) {
						return new ValuesList(UMLTypesUtil.getValue(((LiteralBoolean) arg1).isValue() && ((LiteralBoolean) arg2).isValue(), checker,
								checker.getCheckedObject()));
					} else {
						trc.addProblem(Diagnostic.ERROR, "[SAL] Cannot convert '" + arg2 + "' to Boolean.");
					}
				}
			} else {
				trc.addProblem(Diagnostic.ERROR, "[SAL] Cannot convert '" + arg1 + "' to Boolean.");
			}
		}
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
	public ValuesCollection visit(SALAdd node, SALEvaluationHelper data) {
		ValuesCollection args1 = node.getChild(0).jjtAccept(this, data);
		if (args1.isSingleValued(trc)) {
			ValueSpecification arg1 = args1.get(0);
			if (arg1 instanceof LiteralInteger) {
				ValuesCollection args2 = node.getChild(1).jjtAccept(this, data);
				if (args2.isSingleValued(trc)) {
					ValueSpecification arg2 = args2.get(0);
					if (arg2 instanceof LiteralInteger) {
						return new ValuesList(UMLTypesUtil.getValue(((LiteralInteger) arg1).getValue() + ((LiteralInteger) arg2).getValue(), checker,
								checker.getCheckedObject()));
					} else {
						trc.addProblem(Diagnostic.ERROR, "[SAL] Cannot convert '" + arg2.getLabel() + "' to integer.");
					}
				}
			} else {
				trc.addProblem(Diagnostic.ERROR, "[SAL] Cannot convert '" + arg1.getLabel() + "' to integer.");
			}
		}
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
	public ValuesCollection visit(SALSubstract node, SALEvaluationHelper data) {
		ValuesCollection args1 = node.getChild(0).jjtAccept(this, data);
		if (args1.isSingleValued(trc)) {
			ValueSpecification arg1 = args1.get(0);
			if (arg1 instanceof LiteralInteger) {
				ValuesCollection args2 = node.getChild(1).jjtAccept(this, data);
				if (args2.isSingleValued(trc)) {
					ValueSpecification arg2 = args2.get(0);
					if (arg2 instanceof LiteralInteger) {
						return new ValuesList(UMLTypesUtil.getValue(((LiteralInteger) arg1).getValue() - ((LiteralInteger) arg2).getValue(), checker,
								checker.getCheckedObject()));
					} else {
						trc.addProblem(Diagnostic.ERROR, "[SAL] Cannot convert '" + arg2.getLabel() + "' to integer.");
					}
				}
			} else {
				trc.addProblem(Diagnostic.ERROR, "[SAL] Cannot convert '" + arg1.getLabel() + "' to integer.");
			}
		}
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
	public ValuesCollection visit(SALMult node, SALEvaluationHelper data) {
		ValuesCollection args1 = node.getChild(0).jjtAccept(this, data);
		if (args1.isSingleValued(trc)) {
			ValueSpecification arg1 = args1.get(0);
			if (arg1 instanceof LiteralInteger) {
				ValuesCollection args2 = node.getChild(1).jjtAccept(this, data);
				if (args2.isSingleValued(trc)) {
					ValueSpecification arg2 = args2.get(0);
					if (arg2 instanceof LiteralInteger) {
						return new ValuesList(UMLTypesUtil.getValue(((LiteralInteger) arg1).getValue() * ((LiteralInteger) arg2).getValue(), checker,
								checker.getCheckedObject()));
					} else {
						trc.addProblem(Diagnostic.ERROR, "[SAL] Cannot convert '" + arg2.getLabel() + "' to integer.");
					}
				}
			} else {
				trc.addProblem(Diagnostic.ERROR, "[SAL] Cannot convert '" + arg1.getLabel() + "' to integer.");
			}
		}
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
	public ValuesCollection visit(SALDiv node, SALEvaluationHelper data) {
		ValuesCollection args1 = node.getChild(0).jjtAccept(this, data);
		if (args1.isSingleValued(trc)) {
			ValueSpecification arg1 = args1.get(0);
			if (arg1 instanceof LiteralInteger) {
				ValuesCollection args2 = node.getChild(1).jjtAccept(this, data);
				if (args2.isSingleValued(trc)) {
					ValueSpecification arg2 = args2.get(0);
					if (arg2 instanceof LiteralInteger) {
						return new ValuesList(UMLTypesUtil.getValue(((LiteralInteger) arg1).getValue() / ((LiteralInteger) arg2).getValue(), checker,
								checker.getCheckedObject()));
					} else {
						trc.addProblem(Diagnostic.ERROR, "[SAL] Cannot convert '" + arg2.getLabel() + "' to integer.");
					}
				}
			} else {
				trc.addProblem(Diagnostic.ERROR, "[SAL] Cannot convert '" + arg1.getLabel() + "' to integer.");
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.sal.parser.SALParserVisitor#visit(dk.dtu.
	 * imm.esculapauml.core.sal.parser.SALMod,
	 * dk.dtu.imm.esculapauml.core.sal.SALEvaluationHelper)
	 */
	@Override
	public ValuesCollection visit(SALMod node, SALEvaluationHelper data) {
		ValuesCollection args1 = node.getChild(0).jjtAccept(this, data);
		if (args1.isSingleValued(trc)) {
			ValueSpecification arg1 = args1.get(0);
			if (arg1 instanceof LiteralInteger) {
				ValuesCollection args2 = node.getChild(1).jjtAccept(this, data);
				if (args2.isSingleValued(trc)) {
					ValueSpecification arg2 = args2.get(0);
					if (arg2 instanceof LiteralInteger) {
						return new ValuesList(UMLTypesUtil.getValue(((LiteralInteger) arg1).getValue() % ((LiteralInteger) arg2).getValue(), checker,
								checker.getCheckedObject()));
					} else {
						trc.addProblem(Diagnostic.ERROR, "[SAL] Cannot convert '" + arg2.getLabel() + "' to integer.");
					}
				}
			} else {
				trc.addProblem(Diagnostic.ERROR, "[SAL] Cannot convert '" + arg1.getLabel() + "' to integer.");
			}
		}
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
	public ValuesCollection visit(SALNot node, SALEvaluationHelper data) {
		ValuesCollection args1 = node.getChild(0).jjtAccept(this, data);
		if (args1.isSingleValued(trc)) {
			ValueSpecification arg1 = args1.get(0);
			if (arg1 instanceof LiteralBoolean) {
				return new ValuesList(UMLTypesUtil.getValue(!((LiteralBoolean) arg1).isValue(), checker, checker.getCheckedObject()));
			} else {
				trc.addProblem(Diagnostic.ERROR, "[SAL] Cannot convert '" + arg1.getLabel() + "' to Boolean.");
			}
		}
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
	public ValuesCollection visit(SALMemberCall node, SALEvaluationHelper data) {
		// first child is a given context, second child is SALCall
		ValuesCollection context = node.getChild(0).jjtAccept(this, data);
		if (null != context && !context.isEmpty()) {
			Object oldContext = data.setFunctionEvaluationContext(context);
			ValuesCollection result = node.getChild(1).jjtAccept(this, data);
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
	public ValuesCollection visit(SALMember node, SALEvaluationHelper data) {
		// member second child will be always SALIdent or SALIdentSelector that
		// we will not evaluate but just grab its name and values
		ValuesCollection contextSpecs = node.getChild(0).jjtAccept(this, data);
		if (!contextSpecs.isSingleValued(trc)) {
			trc.addProblem(Diagnostic.ERROR, "[SAL] Cannot navigate because context was multiplicity many value.");
			return null;
		}
		String navigation = (String) node.getChild(1).jjtGetValue();
		boolean hasSelector = false;
		if (node.getChild(1).jjtGetNumChildren() > 0) {
			navigation = (String) node.getChild(1).getChild(0).jjtGetValue();
			// this is SALIdentSelector, accept selector
			hasSelector = true;
		} else {
			navigation = (String) node.getChild(1).jjtGetValue();
		}
		ValueSpecification contextSpec = contextSpecs.get(0);
		if (contextSpec instanceof InstanceValue) {
			InstanceSpecification context = ((InstanceValue) contextSpec).getInstance();
			OCLEvaluator ocl = checker.getSystemState().getOcl();
			ocl.setDebug(logger.getEffectiveLevel() == Level.DEBUG);
			Object result = ocl.evaluate(checker, context, trc.getCheckedObject(), navigation);
			if (ocl.hasErrors()) {
				return null;
			}
			ValuesCollection umlResult = new ValuesList();
			try {
				umlResult.addFromOCL(result, checker, checker.getCheckedObject());
			} catch (OCLConversionException e) {
				trc.addProblem(Diagnostic.ERROR, "[SAL] Cannot navigate to ('" + navigation + "'). Cannot convert to UML value: " + e.getOclValue().toString());
				return null;
			}
			if (hasSelector) {
				ValuesCollection selector = node.getChild(1).getChild(1).jjtAccept(this, data);
				if (null == selector) {
					// selector is invalid
					return null;
				}
				if (!umlResult.select(selector)) {
					trc.addProblem(Diagnostic.ERROR, "[SAL] Selector index out of bound '" + selector + "'.");
					return null;
				}
			}
			return umlResult;
		} else {
			trc.addProblem(Diagnostic.ERROR, "[SAL] Cannot navigate through '" + navigation + "' because it is not instance of complex class.");
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
	public ValuesCollection visit(SALIdent node, SALEvaluationHelper data) {
		// always evaluate this from self
		String name = (String) node.jjtGetValue();
		OCLEvaluator ocl = checker.getSystemState().getOcl();
		ocl.setDebug(logger.getEffectiveLevel() == Level.DEBUG);
		Object result = ocl.evaluate(checker, getInstanceSpecification(), trc.getCheckedObject(), name);
		if (ocl.hasErrors()) {
			return null;
		}
		ValuesCollection umlResult = new ValuesList();
		try {
			umlResult.addFromOCL(result, checker, checker.getCheckedObject());
		} catch (OCLConversionException e) {
			trc.addProblem(Diagnostic.ERROR, "[SAL] Cannot resolve ident ('" + name + "'). Cannot convert to UML value: " + e.getOclValue().toString());
			return null;
		}
		return umlResult;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.sal.parser.SALParserVisitor#visit(dk.dtu.
	 * imm.esculapauml.core.sal.parser.SALIdentSelector,
	 * dk.dtu.imm.esculapauml.core.sal.SALEvaluationHelper)
	 */
	@Override
	public ValuesCollection visit(SALIdentSelector node, SALEvaluationHelper data) {
		// first node is ident, second node is selector
		ValuesCollection context = node.getChild(0).jjtAccept(this, data);
		ValuesCollection selector = node.getChild(1).jjtAccept(this, data);
		if (null != context && null != selector) {
			if (!context.select(selector)) {
				trc.addProblem(Diagnostic.ERROR, "[SAL] Selector index out of bound '" + selector + "'.");
				return null;
			} else {
				return context;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.sal.parser.SALParserVisitor#visit(dk.dtu.
	 * imm.esculapauml.core.sal.parser.SALCallSelector,
	 * dk.dtu.imm.esculapauml.core.sal.SALEvaluationHelper)
	 */
	@Override
	public ValuesCollection visit(SALCallSelector node, SALEvaluationHelper data) {
		// first node is call, second node is selector
		ValuesCollection context = node.getChild(0).jjtAccept(this, data);
		ValuesCollection selector = node.getChild(1).jjtAccept(this, data);
		if (null != context && null != selector) {
			if (!context.select(selector)) {
				trc.addProblem(Diagnostic.ERROR, "[SAL] Selector index out of bound '" + selector + "'.");
				return null;
			} else {
				return context;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.sal.parser.SALParserVisitor#visit(dk.dtu.
	 * imm.esculapauml.core.sal.parser.SALCollectionExpression,
	 * dk.dtu.imm.esculapauml.core.sal.SALEvaluationHelper)
	 */
	@Override
	public ValuesCollection visit(SALCollectionExpression node, SALEvaluationHelper data) {
		ValuesCollection umlResult = new ValuesList();
		for (int i = 0; !checker.hasErrors() && i < node.jjtGetNumChildren(); ++i) {
			ValuesCollection partResult = node.getChild(i).jjtAccept(this, data);
			if (null == partResult) {
				return null;
			}
			umlResult.addAll(partResult);
		}
		return umlResult;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.dtu.imm.esculapauml.core.sal.parser.SALParserVisitor#visit(dk.dtu.
	 * imm.esculapauml.core.sal.parser.SALSelector,
	 * dk.dtu.imm.esculapauml.core.sal.SALEvaluationHelper)
	 */
	@Override
	public ValuesCollection visit(SALSelector node, SALEvaluationHelper data) {
		ValuesCollection selection = node.getChild(0).jjtAccept(this, data);
		if (selection.isSingleValued(trc)) {
			ValueSpecification arg1 = selection.get(0);
			if (arg1 instanceof LiteralInteger) {
				return selection;
			} else {
				trc.addProblem(Diagnostic.ERROR, "[SAL] Cannot convert '" + arg1.getLabel() + "' to integer. Selector must be integer value.");
			}
		}
		return null;
	}
}
