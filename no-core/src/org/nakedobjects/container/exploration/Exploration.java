package org.nakedobjects.container.exploration;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.container.configuration.Configuration;
import org.nakedobjects.container.configuration.ConfigurationException;
import org.nakedobjects.container.configuration.ConfigurationFactory;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.exploration.ExplorationFixture;
import org.nakedobjects.object.exploration.ExplorationSetUp;
import org.nakedobjects.system.AboutNakedObjects;
import org.nakedobjects.system.SplashWindow;
import org.nakedobjects.viewer.ObjectViewingMechanism;
import org.nakedobjects.viewer.ObjectViewingMechanismListener;

import java.util.Locale;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


public abstract class Exploration implements ObjectViewingMechanismListener {
    private static final String DEFAULT_CONFIG = "nakedobjects.properties";
    private static final Logger LOG = Logger.getLogger(Exploration.class);
    private static final String SHOW_EXPLORATION_OPTIONS = "viewer.lightweight.show-exploration";
    protected ExplorationSetUp explorationSetUp;
  
    protected Exploration() {
        try {
            Properties p = Configuration.loadProperties("log4j.properties");
            PropertyConfigurator.configure(p);
        } catch (ConfigurationException e) {
            BasicConfigurator.configure();
        }
        Logger.getRootLogger().setLevel(Level.WARN);
        
 
        
        SplashWindow splash = null;
        try {
            String name = this.getClass().getName();
            name = name.substring(name.lastIndexOf('.') + 1);
            
            ConfigurationFactory.setConfiguration(new Configuration(DEFAULT_CONFIG));
            if (ConfigurationFactory.getConfiguration().getString(SHOW_EXPLORATION_OPTIONS) == null) {
                ConfigurationFactory.getConfiguration().add(SHOW_EXPLORATION_OPTIONS, "yes");
            }
            PropertyConfigurator.configure(ConfigurationFactory.getConfiguration().getProperties("log4j"));

            Logger log = Logger.getLogger("Naked Objects");
            log.info(AboutNakedObjects.getName());
            log.info(AboutNakedObjects.getVersion());
            log.info(AboutNakedObjects.getBuildId());

           
            boolean noSplash = ConfigurationFactory.getConfiguration().getBoolean("nosplash", false);
            if (!noSplash) {
                splash = new SplashWindow();
            } 

            setUpLocale();
            
            explorationSetUp = explorationSetup();
            setUpFixtures();
            explorationSetUp.installFixtures();
         /*   
            ExplorationContext explorationContext = new ExplorationContext();
            String[] classes = explorationSetUp.getClasses();
            for (int i = 0; i < classes.length; i++) {
                explorationContext.addClass(classes[i]);
             }            
            */
            ObjectViewingMechanism viewer = setupViewer();
   /*         UpdateNotifier updateNotifier = viewer.getUpdateNotifier();
            //explorationSetUp.
            viewer.setTitle(name);
            NakedObject rootObject = PojoAdapter.createAdapter(explorationContext);
            viewer.init(rootObject, this);
 */
            viewer.start();
            
            splash.toFront();
            
        } catch (ConfigurationException e) {
            throw new NakedObjectRuntimeException(e);
/*
 *        } catch( StartupException e) {
            if(splash != null) {
                splash.removeAfterDelay(4);
            }
            throw new NakedObjectRuntimeException(e);
   */         
        } finally {
            if(splash != null) {
                splash.removeAfterDelay(4);
            }
        }
    }

    protected abstract ObjectViewingMechanism setupViewer();

    protected abstract ExplorationSetUp explorationSetup();

    public void addFixture(ExplorationFixture fixture) {
        explorationSetUp.addFixture(fixture);
    }
    
    protected abstract void setUpFixtures();

     private void setUpLocale() {
        String localeSpec = ConfigurationFactory.getConfiguration().getString("locale");
        if (localeSpec != null) {
            int pos = localeSpec.indexOf('_');
            Locale locale;
            if (pos == -1) {
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
    }

    public void viewerClosing() {
        NakedObjects.getObjectManager().shutdown();
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */