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

package dk.dtu.imm.esculapauml.gui.topcased.utils;

import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.uml2.uml.Interaction;
import org.topcased.modeler.editor.Modeler;

/**
 * The helper services for converting ExecutionEvent to context information
 * 
 * @author Piotr. J. Puczynski (piotr.puczynski)
 * 
 */
public final class GuiUtils {

	/**
	 * Gets the selected subtree in the model
	 * 
	 * @param event
	 *            The event occurred
	 * @return the list of objects of selected subtree (including selected node
	 *         at 0)
	 */
	public static final List<?> getSelectionModelSubtreeContents(ExecutionEvent event) {
		if (((EvaluationContext) event.getApplicationContext()).getDefaultVariable() instanceof List<?>) {
			// Get the current selection
			return ((List<?>) ((EvaluationContext) event.getApplicationContext()).getDefaultVariable());
		} else {
			throw new IllegalArgumentException("Passed argument cannot be casted to model subtree");
		}
	}

	/**
	 * Gets the selected resource that gives the access to UML2 model
	 * 
	 * @param event
	 *            The event occurred
	 * @return the resource
	 */
	public static final Resource getSelectedResource(ExecutionEvent event) {
		List<?> elements = getSelectionModelSubtreeContents(event);
		if (elements.get(0) instanceof EObject) {
			return ((EObject) elements.get(0)).eResource();
		} else {
			throw new IllegalArgumentException("Passed argument cannot be casted to Resource");
		}

	}

	/**
	 * Gets EditingDomain of the selected model
	 * 
	 * @param event
	 *            The event occurred
	 * @return EditingDomain of the selected model node
	 */
	public static final EditingDomain getEditingDomain(ExecutionEvent event) {
		// Get the IWorkbenchPart
		IWorkbenchPart targetPart = HandlerUtil.getActivePart(event);
		IEditorPart editorPart = HandlerUtil.getActiveEditor(event);

		if (targetPart.getAdapter(EditingDomain.class) != null) {
			return (EditingDomain) targetPart.getAdapter(EditingDomain.class);
		}

		if (editorPart instanceof IEditingDomainProvider) {
			return ((IEditingDomainProvider) editorPart).getEditingDomain();
		}

		throw new IllegalArgumentException("Passed argument cannot be casted to editing domain");
	}

	/**
	 * Gets topcased modeler for diagrams manipulation from the event
	 * 
	 * @param event
	 *            The event occurred
	 * @return modeler
	 */
	public static final Modeler getModeler(ExecutionEvent event) {
		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		if (editor instanceof Modeler) {
			return (Modeler) editor;
		}

		throw new IllegalArgumentException("Passed argument cannot be casted to topcased modeler");
	}

	/**
	 * Gets command stack for execution of commands in GUI environment of
	 * Graphical Editing Framework
	 * 
	 * @param event
	 *            The event occurred
	 * @return stack
	 */
	public static final CommandStack getCommandStack(ExecutionEvent event) {
		return (CommandStack) getModeler(event).getAdapter(CommandStack.class);
	}

	/**
	 * Finds all interactions in given nodes.
	 * 
	 * @param someObject
	 *            interaction, use case or collaboration
	 * @return uml2 interaction
	 */
	public static final EList<Interaction> getUMLInteractionArgument(List<?> elements) {
		EList<Interaction> result = new BasicEList<Interaction>();
		for (Object object : elements) {
			if (object instanceof EObject) {
				if (object instanceof Interaction) {
					result.add((Interaction) object);
				} else {
					TreeIterator<EObject> contents = ((EObject) object).eAllContents();
					while (contents.hasNext()) {
						EObject o = contents.next();
						if (o instanceof Interaction) {
							result.add((Interaction) o);
						}
					}
				}
			}
		}

		return result;
	}

	/**
	 * Displays an error.
	 * 
	 * @param string
	 * @param event
	 */
	public static void showError(final String msg, final Modeler modeler) {
		modeler.getSite().getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				MessageDialog.openError(modeler.getSite().getShell(), "EsculapaUML error", msg);
			}
		});
	}
	
	/**
	 * Displays info.
	 * 
	 * @param string
	 * @param event
	 */
	public static void showInfo(final String msg, final Modeler modeler) {
		modeler.getSite().getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				MessageDialog.openInformation(modeler.getSite().getShell(), "EsculapaUML info", msg);
			}
		});
	}

}
