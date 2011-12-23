package dk.dtu.imm.esculapauml.gui.topcased.utils;

import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

public final class GuiUtils {
	public static final List<?> getSelectionModelSubtreeContents(ExecutionEvent event) {
		if (((EvaluationContext) event.getApplicationContext()).getDefaultVariable() instanceof List<?>) {
			// Get the current selection
			return ((List<?>) ((EvaluationContext) event.getApplicationContext()).getDefaultVariable());
		} else {
			throw new IllegalArgumentException("Passed argument cannot be casted to model subtree");
		}
	}
	
	public static final Resource getSelectedResource(ExecutionEvent event) {
		List<?> elements = getSelectionModelSubtreeContents(event);
		if (elements.get(0) instanceof EObject) {
			return ((EObject)elements.get(0)).eContainer().eResource();
		} else {
			throw new IllegalArgumentException("Passed argument cannot be casted to Resource");
		}
		
	}
	
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
}
