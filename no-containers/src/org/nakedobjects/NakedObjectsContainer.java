package org.nakedobjects;

import org.nakedobjects.container.configuration.Configuration;
import org.nakedobjects.container.configuration.ConfigurationFactory;
import org.nakedobjects.container.configuration.ConfigurationException;
import org.nakedobjects.security.LoginDialog;
import org.nakedobjects.system.SplashWindow;
import org.nakedobjects.utility.StartupException;

import java.util.Locale;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


public abstract class NakedObjectsContainer {
    private static final Logger LOG = Logger.getLogger(NakedObjectsContainer.class);
    private static final String DEFAULT_CONFIG = "nakedobjects.properties";
    private static SplashWindow splash;
	private static String configurationFile;

    protected NakedObjectsContainer() {
        try {
        	Properties p = Configuration.loadProperties("log4j.properties", false);
            PropertyConfigurator.configure(p);
        } catch(ConfigurationException e) {
            BasicConfigurator.configure();
        }

        Logger.getRootLogger().setLevel(Level.WARN);
    }
    
    
    protected void start() throws ConfigurationException {
        String configurationFile = configurationFile();
        loadConfiguration(configurationFile, DEFAULT_CONFIG);
        
        String localeSpec = ConfigurationFactory.getConfiguration().getString("locale");
        if(localeSpec != null) {
        	int pos = localeSpec.indexOf('_');
        	Locale locale;
        	if(pos == -1) {
            	locale = new Locale(localeSpec, "");
        	} else {
	            String language = localeSpec.substring(0, pos);
	        	String country = localeSpec.substring(pos + 1);
	        	locale = new Locale(language, country);
        	}
			Locale.setDefault(locale);
        	LOG.info("Locale set to " + locale);
        }
        
        LOG.debug("locale is " + Locale.getDefault());

        displaySplash();
        try {
            run();
         } catch (StartupException e) {
            System.err.println("Fatal startup failure, exiting");
            e.printStackTrace(System.err);
            e.getCause().printStackTrace(System.err);
            System.exit(0);

        } finally {
            removeSplashImmediately();
        }
    }

 	protected abstract void run() throws StartupException;

	protected static void setConfiguration(String configurationFile) {
    	NakedObjectsContainer.configurationFile = configurationFile;
    }
    
    /**
     * hook method to return the name of the configuration file.  Called by
     * <code>init</code> method.
     * <p>
     * The default implementation simply returns null, meaning that the
     * default configuration file will be used.
     */
    protected String configurationFile() {
    	return configurationFile;
    }


    /**
     * Returns a SplashWindow that will be displayed at startup.  Override this method
     * to provide a tailored spash screen.
     */
    static void displaySplash() {
    	boolean noSplash = ConfigurationFactory.getConfiguration().getBoolean("nosplash", false);
		if(!noSplash) {
    		splash = new SplashWindow();
    	}
    }

    /**
     * Indicates whether the named configuration property exists.
     */
    protected static boolean hasProperty(String key) {
        return ConfigurationFactory.getConfiguration().hasProperty(key);
    }

    protected static void loadConfiguration(String configFile) throws ConfigurationException {
    	loadConfiguration(null, configFile);
    }

    /**
     * Loads up the specified configuration file.  If none is specified, then the default
     * configuration file is loaded.  If no file can be found a ConfigurationException is 
     * thrown and the system should exit.
     */
    protected static void loadConfiguration(String configFile, String defaultConfigFile) throws ConfigurationException {
		String loadedFrom;
		
		if (configFile != null) {
			ConfigurationFactory.getConfiguration().load(configFile);
			loadedFrom = configFile;
			PropertyConfigurator.configure(
					ConfigurationFactory.getConfiguration().getProperties("log4j"));
		} else {
			ConfigurationFactory.getConfiguration().load(defaultConfigFile);
			loadedFrom = defaultConfigFile;
			PropertyConfigurator.configure(
					ConfigurationFactory.getConfiguration().getProperties("log4j"));
		}
		Logger.getLogger(NakedObjectsContainer.class).info("Loaded configuration details from " + loadedFrom);
    }

    /**
     * Allows a configuration property to be programmatically set, or overriden.  This can be used to 
     * ensure a property is set in code, rather than relying on it being provided within
     * the configuration file.  If this method is called with a property having the same 
     * name as one in the configuration file then that user defined value will be replaced.
     *  
     * @param key  name of the property
     * @param value  the value of the property
     */
    protected static void set(String key, String value) {
        ConfigurationFactory.getConfiguration().add(key, value);
    }

    /**
     * Retrieves the named property value.
     * 
     * @throws ConfigurationException  when the specfied property does not exist.
     */
    protected static String setting(String key) throws ConfigurationException {
        String value = ConfigurationFactory.getConfiguration().getString(key);

        if (value == null) {
            throw new ConfigurationException("No " + key + " key in properties");
        }

        return value;
    }

	/**
       Configures Apache LOG4J to default logging.
     */
    protected void configureSystemLogging() throws ConfigurationException {
        PropertyConfigurator.configure("log4j.testing.properties");
    }
    
    LoginDialog displayLoginDialog() {
    	return new LoginDialog();
    }
    
    static void removeSplashAfterDelay(int delay) {
    	if(splash != null) {
    		splash.removeAfterDelay(delay);
    	}
    }
    
    static void removeSplashImmediately() {
    	if(splash != null) {
    		splash.removeImmediately();
    	}
    }
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

The authors can be contacted via www.nakedobjects.org (the
registered address of Naked Objects Group is Kingsway House, 123 Goldworth
Road, Woking GU21 1NR, UK).
*/