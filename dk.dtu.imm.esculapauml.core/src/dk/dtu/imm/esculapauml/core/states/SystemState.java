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
package dk.dtu.imm.esculapauml.core.states;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.uml2.common.util.UML2Util;
import org.eclipse.uml2.uml.BehavioredClassifier;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.InstanceSpecification;
import org.eclipse.uml2.uml.Model;

import dk.dtu.imm.esculapauml.core.checkers.AbstractChecker;
import dk.dtu.imm.esculapauml.core.checkers.BehaviorChecker;
import dk.dtu.imm.esculapauml.core.executors.InstanceExecutor;
import dk.dtu.imm.esculapauml.core.executors.coordination.ExecutionCoordinator;

/**
 * State of the whole system. Stores the checkers responsible for statefull
 * checks.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class SystemState {
	private List<InstanceExecutor> instanceExecutors = new LinkedList<InstanceExecutor>();
	private HashMap<BehavioredClassifier, BehaviorChecker> behaviorCheckers = new HashMap<BehavioredClassifier, BehaviorChecker>();
	private org.eclipse.uml2.uml.Package instancePackage = null;
	private Set<Element> generatedElements = new HashSet<Element>();
	private int stateId = -1;
	private ExecutionCoordinator coordinator;

	/**
	 * Initialize state
	 */
	public SystemState() {
		super();
	}

	/**
	 * Prepares the state in case of subsequent execution.
	 * 
	 * @param model
	 */
	public void prepare(String name, Model model) {
		generatedElements.clear();
		instancePackage = model.createNestedPackage(name + " Instance(" + ++stateId + ")");
		addGeneratedElement(instancePackage);
		coordinator = new ExecutionCoordinator();
	}

	public InstanceExecutor getInstanceExecutor(InstanceSpecification instanceSpecification) {
		for (InstanceExecutor instanceExecutor : instanceExecutors) {
			if (instanceExecutor.getInstanceSpecification() == instanceSpecification) {
				return instanceExecutor;
			}
		}
		return null;
	}

	public InstanceExecutor getInstanceExecutor(String instanceName, Class clazz) {
		for (InstanceExecutor instanceExecutor : instanceExecutors) {
			if (instanceExecutor.getInstanceName().equals(instanceName) && instanceExecutor.getOriginalClass() == clazz) {
				return instanceExecutor;
			}
		}
		return null;
	}
	
	public InstanceExecutor getInstanceExecutor(Class clazz) {
		for (InstanceExecutor instanceExecutor : instanceExecutors) {
			if (instanceExecutor.getOriginalClass() == clazz) {
				return instanceExecutor;
			}
		}
		return null;
	}

	public void registerInstanceExecutor(InstanceExecutor instanceExecutor) {
		instanceExecutors.add(instanceExecutor);
	}

	public void addGeneratedElement(Element element) {
		annotateAsGenerated(element);
		generatedElements.add(element);
	}

	public BehaviorChecker getBehaviorChecker(BehavioredClassifier type) {
		return behaviorCheckers.get(type);
	}

	public void registerBehaviorChecker(BehavioredClassifier type, BehaviorChecker checker) {
		behaviorCheckers.put(type, checker);
	}

	/**
	 * @return the generatedElements
	 */
	public Set<Element> getGeneratedElements() {
		return generatedElements;
	}

	/**
	 * @return the instancePackage
	 */
	public org.eclipse.uml2.uml.Package getInstancePackage() {
		return instancePackage;
	}

	/**
	 * Adds an annotation for new generated elements. The annotations might be
	 * used later by other plug-ins, e.g. to draw new elements.
	 * 
	 * @param element
	 */

	protected void annotateAsGenerated(Element element) {
		EAnnotation annotation = UML2Util.getEAnnotation(element, AbstractChecker.ESCULAPA_NAMESPACE, true);
		annotation.getDetails().put("generated", "true");
	}

	/**
	 * @return the coordinator
	 */
	public ExecutionCoordinator getCoordinator() {
		return coordinator;
	}
}
