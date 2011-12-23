package dk.dtu.imm.esculapauml.gui.topcased.handlers;

import java.util.List;
import java.util.Iterator;
import dk.dtu.imm.esculapauml.gui.topcased.utils.GuiUtils;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.topcased.modeler.DuplicationAdapter;
import org.topcased.modeler.actions.DuplicateSubTreeAction;
import org.topcased.modeler.editor.Modeler;

public class CheckUseCase extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		/*List<?> elements = GuiUtils.getSelectionModelSubtreeContents(event);
		System.out.println(elements.get(0).toString());
		Iterator<EObject> itChildren = ((EObject) elements.get(0)).eAllContents();
		while (itChildren.hasNext()) {
			EObject o = itChildren.next();
			System.out.println(o.toString());
		}*/
		Modeler modeler = GuiUtils.getModeler(event);
		System.out.println(modeler.getActiveDiagram().getName());
		modeler.getActiveDiagram().setName("siema");
		
		return null;
		
	}

}
