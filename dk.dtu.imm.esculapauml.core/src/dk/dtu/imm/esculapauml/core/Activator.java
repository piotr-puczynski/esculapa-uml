package dk.dtu.imm.esculapauml.core;

import java.net.URL;

import org.apache.log4j.PropertyConfigurator;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import dk.dtu.imm.esculapauml.core.logging.EsculapaUMLLog4JLoger;

public class Activator implements BundleActivator {
	private static final String LOG_PROPERTIES_FILE = "logging.properties";

	@Override
	public void start(BundleContext context) throws Exception {
		System.setProperty("org.apache.commons.logging.log", EsculapaUMLLog4JLoger.class.getName());
		URL url = context.getBundle().getEntry(LOG_PROPERTIES_FILE);
		PropertyConfigurator.configure(url);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// TODO Auto-generated method stub

	}

}
