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
package dk.dtu.imm.esculapauml.core.tests.simpleTestCases;

import java.util.Enumeration;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import dk.dtu.imm.esculapauml.core.Activator;
import dk.dtu.imm.esculapauml.core.logging.EsculapaUMLLog4JLoger;

/**
 * Abstract class that helps prepare logger for some use cases that need it.
 * 
 * @author Piotr J. Puczynski
 * 
 */
public abstract class LoggingTest {
	private static boolean configured = isConfigured();
	public LoggingTest() {
		if(!configured) {
			System.setProperty("org.apache.commons.logging.log", EsculapaUMLLog4JLoger.class.getName());
			PropertyConfigurator.configure(Activator.LOG_PROPERTIES_FILE);
			configured = true;
		}
	}

	/**
	 * Returns true if it appears that log4j have been previously configured.
	 * This code checks to see if there are any appenders defined for log4j
	 * which is the definitive way to tell if log4j is already initialized
	 * 
	 * @return
	 */
	private static boolean isConfigured() {
		@SuppressWarnings("rawtypes")
		Enumeration appenders = Logger.getLogger("dk.dtu.imm.esculapauml").getAllAppenders();
		return appenders.hasMoreElements();
	}

}
