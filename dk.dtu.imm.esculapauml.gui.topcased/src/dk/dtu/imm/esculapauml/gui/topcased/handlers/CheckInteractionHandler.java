package dk.dtu.imm.esculapauml.gui.topcased.handlers;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.emf.ecore.EObject;

import dk.dtu.imm.esculapauml.core.ConsistencyCheckingService;
import dk.dtu.imm.esculapauml.gui.topcased.utils.GuiUtils;

public class CheckInteractionHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		List<?> elements = GuiUtils.getSelectionModelSubtreeContents(event);
		if((elements.size() > 0) && (elements.get(0) instanceof EObject)) {
			ConsistencyCheckingService.getInstance().checkInteraction((EObject) elements.get(0));
		} else {
			throw new IllegalArgumentException("Passed argument is empty or of a wrong type (required EMF model element argument)");
		}
		return null;
	}

}
