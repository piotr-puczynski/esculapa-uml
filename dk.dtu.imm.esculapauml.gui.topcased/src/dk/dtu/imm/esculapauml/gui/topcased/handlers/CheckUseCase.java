package dk.dtu.imm.esculapauml.gui.topcased.handlers;

import java.util.List;
import java.util.Iterator;
import dk.dtu.imm.esculapauml.gui.topcased.utils.GuiUtils;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.emf.ecore.EObject;

public class CheckUseCase extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		List<?> elements = GuiUtils.getSelectionModelSubtreeContents(event);
		System.out.println(elements.get(0).toString());
		Iterator<EObject> itChildren = ((EObject) elements.get(0)).eAllContents();
		while (itChildren.hasNext()) {
			EObject o = itChildren.next();
			System.out.println(o.toString());
		}
		return null;
	}

}
