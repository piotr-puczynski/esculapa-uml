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

import org.eclipse.uml2.uml.OpaqueBehavior;

/**
 * Class to extract string effects from OpaqueBehaviors.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public class EffectExtractor {
	protected OpaqueBehavior behavior;
	public static final String SAL_LANG_ID = "SAL";
	protected String langId = SAL_LANG_ID;

	/**
	 * @param effect
	 */
	public EffectExtractor(OpaqueBehavior behavior) {
		this.behavior = behavior;
	}

	/**
	 * Extracts effect as a string.
	 * 
	 * @return
	 */
	public String extract() {
		if (isInAdvancedMode()) {
			return calculateBodyAdvanced();
		} else {
			return calculateBodySimple();
		}
	}

	/**
	 * If the mode is advanced the meta-model elements are used to get bodies of
	 * behavior.
	 * 
	 * @return
	 */
	public boolean isInAdvancedMode() {
		return !behavior.getLanguages().isEmpty() || !behavior.getBodies().isEmpty();
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
			if (lang.equalsIgnoreCase(langId)) {
				if (behavior.getBodies().size() > i) {
					bodies.add(behavior.getBodies().get(i));
				}
			}
			++i;
		}
		return join(bodies, ";");
	}

	/**
	 * @return the langId
	 */
	public String getLangId() {
		return langId;
	}

	/**
	 * @param langId
	 *            the langId to set
	 */
	public void setLangId(String langId) {
		this.langId = langId;
	}

}
