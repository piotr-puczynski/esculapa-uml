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
import org.eclipse.uml2.uml.LiteralBoolean;
import org.eclipse.uml2.uml.LiteralInteger;
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
	@Override
	public ValueSpecification visit(SALCall node, SALEvaluationHelper data) {
		Object instance = data.getFunctionEvaluationContext();
		String name = (String) node.jjtGetValue();
		ValueSpecification result = null;
		if (instance instanceof InstanceValue) {
			instance = ((InstanceValue) instance).getInstance();
		}
		if (instance instanceof InstanceSpecification) {
			InstanceExecutor executor = checker.getSystemState().getInstanceExecutor((InstanceSpecification) instance);
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

		return translateOCLResult(result, oclExpression);
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
		ValueSpecification arg1 = node.getChild(0).jjtAccept(this, data);
		if (arg1 instanceof LiteralBoolean) {
			ValueSpecification arg2 = node.getChild(1).jjtAccept(this, data);
			if (arg2 instanceof LiteralBoolean) {
				return UMLTypesUtil.getValue(((LiteralBoolean) arg1).isValue() || ((LiteralBoolean) arg2).isValue(), checker, checker.getCheckedObject());
			} else {
				trc.addProblem(Diagnostic.ERROR, "[SAL] Cannot convert '" + arg2.getLabel() + "' to Boolean.");
			}
		} else {
			trc.addProblem(Diagnostic.ERROR, "[SAL] Cannot convert '" + arg1.getLabel() + "' to Boolean.");
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
	public ValueSpecification visit(SALAnd node, SALEvaluationHelper data) {
		ValueSpecification arg1 = node.getChild(0).jjtAccept(this, data);
		if (arg1 instanceof LiteralBoolean) {
			ValueSpecification arg2 = node.getChild(1).jjtAccept(this, data);
			if (arg2 instanceof LiteralBoolean) {
				return UMLTypesUtil.getValue(((LiteralBoolean) arg1).isValue() && ((LiteralBoolean) arg2).isValue(), checker, checker.getCheckedObject());
			} else {
				trc.addProblem(Diagnostic.ERROR, "[SAL] Cannot convert '" + arg2.getLabel() + "' to Boolean.");
			}
		} else {
			trc.addProblem(Diagnostic.ERROR, "[SAL] Cannot convert '" + arg1.getLabel() + "' to Boolean.");
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
	public ValueSpecification visit(SALAdd node, SALEvaluationHelper data) {
		ValueSpecification arg1 = node.getChild(0).jjtAccept(this, data);
		if (arg1 instanceof LiteralInteger) {
			ValueSpecification arg2 = node.getChild(1).jjtAccept(this, data);
			if (arg2 instanceof LiteralInteger) {
				return UMLTypesUtil.getValue(((LiteralInteger) arg1).getValue() + ((LiteralInteger) arg2).getValue(), checker, checker.getCheckedObject());
			} else {
				trc.addProblem(Diagnostic.ERROR, "[SAL] Cannot convert '" + arg2.getLabel() + "' to integer.");
			}
		} else {
			trc.addProblem(Diagnostic.ERROR, "[SAL] Cannot convert '" + arg1.getLabel() + "' to integer.");
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
	public ValueSpecification visit(SALSubstract node, SALEvaluationHelper data) {
		ValueSpecification arg1 = node.getChild(0).jjtAccept(this, data);
		if (arg1 instanceof LiteralInteger) {
			ValueSpecification arg2 = node.getChild(1).jjtAccept(this, data);
			if (arg2 instanceof LiteralInteger) {
				return UMLTypesUtil.getValue(((LiteralInteger) arg1).getValue() - ((LiteralInteger) arg2).getValue(), checker, checker.getCheckedObject());
			} else {
				trc.addProblem(Diagnostic.ERROR, "[SAL] Cannot convert '" + arg2.getLabel() + "' to integer.");
			}
		} else {
			trc.addProblem(Diagnostic.ERROR, "[SAL] Cannot convert '" + arg1.getLabel() + "' to integer.");
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
	public ValueSpecification visit(SALMult node, SALEvaluationHelper data) {
		ValueSpecification arg1 = node.getChild(0).jjtAccept(this, data);
		if (arg1 instanceof LiteralInteger) {
			ValueSpecification arg2 = node.getChild(1).jjtAccept(this, data);
			if (arg2 instanceof LiteralInteger) {
				return UMLTypesUtil.getValue(((LiteralInteger) arg1).getValue() * ((LiteralInteger) arg2).getValue(), checker, checker.getCheckedObject());
			} else {
				trc.addProblem(Diagnostic.ERROR, "[SAL] Cannot convert '" + arg2.getLabel() + "' to integer.");
			}
		} else {
			trc.addProblem(Diagnostic.ERROR, "[SAL] Cannot convert '" + arg1.getLabel() + "' to integer.");
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
	public ValueSpecification visit(SALDiv node, SALEvaluationHelper data) {
		ValueSpecification arg1 = node.getChild(0).jjtAccept(this, data);
		if (arg1 instanceof LiteralInteger) {
			ValueSpecification arg2 = node.getChild(1).jjtAccept(this, data);
			if (arg2 instanceof LiteralInteger) {
				return UMLTypesUtil.getValue(((LiteralInteger) arg1).getValue() / ((LiteralInteger) arg2).getValue(), checker, checker.getCheckedObject());
			} else {
				trc.addProblem(Diagnostic.ERROR, "[SAL] Cannot convert '" + arg2.getLabel() + "' to integer.");
			}
		} else {
			trc.addProblem(Diagnostic.ERROR, "[SAL] Cannot convert '" + arg1.getLabel() + "' to integer.");
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
	public ValueSpecification visit(SALMod node, SALEvaluationHelper data) {
		ValueSpecification arg1 = node.getChild(0).jjtAccept(this, data);
		if (arg1 instanceof LiteralInteger) {
			ValueSpecification arg2 = node.getChild(1).jjtAccept(this, data);
			if (arg2 instanceof LiteralInteger) {
				return UMLTypesUtil.getValue(((LiteralInteger) arg1).getValue() % ((LiteralInteger) arg2).getValue(), checker, checker.getCheckedObject());
			} else {
				trc.addProblem(Diagnostic.ERROR, "[SAL] Cannot convert '" + arg2.getLabel() + "' to integer.");
			}
		} else {
			trc.addProblem(Diagnostic.ERROR, "[SAL] Cannot convert '" + arg1.getLabel() + "' to integer.");
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
	public ValueSpecification visit(SALNot node, SALEvaluationHelper data) {
		ValueSpecification arg1 = node.getChild(0).jjtAccept(this, data);
		if (arg1 instanceof LiteralBoolean) {
			return UMLTypesUtil.getValue(!((LiteralBoolean) arg1).isValue(), checker, checker.getCheckedObject());
		} else {
			trc.addProblem(Diagnostic.ERROR, "[SAL] Cannot convert '" + arg1.getLabel() + "' to Boolean.");
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
		String navigation = (String) node.getChild(1).jjtGetValue();
		if (contextSpec instanceof InstanceValue) {
			InstanceSpecification context = ((InstanceValue) contextSpec).getInstance();
			OCLEvaluator ocl = new OCLEvaluator(checker, context, trc.getCheckedObject());
			ocl.setDebug(logger.getEffectiveLevel() == Level.DEBUG);
			Object result = ocl.evaluate(navigation);
			if (ocl.hasErrors()) {
				return null;
			}
			return translateOCLResult(result, navigation);
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
	public ValueSpecification visit(SALIdent node, SALEvaluationHelper data) {
		// always evaluate this from self
		String name = (String) node.jjtGetValue();
		OCLEvaluator ocl = new OCLEvaluator(checker, getInstanceSpecification(), trc.getCheckedObject());
		ocl.setDebug(logger.getEffectiveLevel() == Level.DEBUG);
		Object result = ocl.evaluate(name);
		if (ocl.hasErrors()) {
			return null;
		}
		return translateOCLResult(result, name);
	}

	/**
	 * Changes OCL result into ValueSpecification for evaluation.
	 * 
	 * @param oclResult
	 * @param name
	 *            of navigation (for error only)
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private ValueSpecification translateOCLResult(Object oclResult, String name) {
		if (oclResult instanceof ValueSpecification) {
			return (ValueSpecification) oclResult;
		}
		if (oclResult instanceof Collection) {
			Collection collection = (Collection) oclResult;
			if (collection.isEmpty()) {
				trc.addProblem(Diagnostic.ERROR, "[SAL] Naigation through empty collection ('" + name + "') is not possible.");
				return null;
			} else if (collection.size() == 1) {
				oclResult = collection.toArray()[0];
				if (oclResult instanceof ValueSpecification) {
					return (ValueSpecification) oclResult;
				}
			} else {
				trc.addProblem(Diagnostic.ERROR, "[SAL] Naigation through multiplicity many ('" + name + "') is not possible, use OCL expression instead.");
				return null;
			}
		}
		if (UMLTypesUtil.canBeConverted(oclResult)) {
			return UMLTypesUtil.getObjectValue(oclResult, checker, checker.getCheckedObject());
		}
		if(null == oclResult) {
			return UMLTypesUtil.getNullValue();
		}
		return null;
	}
}
