package org.nakedobjects.exploration;

import org.nakedobjects.ObjectViewingMechanism;
import org.nakedobjects.ObjectViewingMechanismListener;
import org.nakedobjects.configuration.ComponentException;
import org.nakedobjects.configuration.ComponentLoader;
import org.nakedobjects.configuration.Configuration;
import org.nakedobjects.configuration.ConfigurationException;
import org.nakedobjects.object.DefaultUserContext;
import org.nakedobjects.object.LocalObjectManager;
import org.nakedobjects.object.LocalReflectionFactory;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectContext;
import org.nakedobjects.object.NakedObjectManager;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectStore;
import org.nakedobjects.object.OidGenerator;
import org.nakedobjects.object.SimpleOidGenerator;
import org.nakedobjects.object.TransientObjectStore;
import org.nakedobjects.object.UpdateNotifier;
import org.nakedobjects.security.Role;
import org.nakedobjects.security.Session;
import org.nakedobjects.security.User;
import org.nakedobjects.utility.SplashWindow;
import org.nakedobjects.utility.StartupException;

import java.util.Locale;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


public abstract class Exploration implements ObjectViewingMechanismListener {
    private static final String DEFAULT_CONFIG = "nakedobjects.properties";
    private static final Logger LOG = Logger.getLogger(Exploration.class);
    public static final String OBJECT_STORE = "object-store";
    private static final String SHOW_EXPLORATION_OPTIONS = "viewer.lightweight.show-exploration";
    private final static String VIEWING_MECHANISM = "viewer";

    private NakedObjectContext context;
    private ExplorationSetUp explorationSetUp;
  
    protected Exploration() {
        try {
            Properties p = Configuration.loadProperties("log4j.properties");
            PropertyConfigurator.configure(p);
        } catch (ConfigurationException e) {
            BasicConfigurator.configure();
        }
        Logger.getRootLogger().setLevel(Level.WARN);

        NakedObjectManager objectManager = null;
        try {
            String name = this.getClass().getName();
            name = name.substring(name.lastIndexOf('.') + 1);
            
            loadConfiguration();
            showSplash();
            setUpLocale();
            ObjectViewingMechanism viewer = installViewer();
            NakedObjectSpecification.setReflectionFactory(new LocalReflectionFactory());
            objectManager = installObjectManager(viewer.getUpdateNotifier());
            
            NakedObjectContext context = new NakedObjectContext(objectManager);
            explorationSetUp = new ExplorationSetUp(context);
            setUpFixtures();
            setUpFixture();

            Session.getSession().setSecurityContext(context);

            ExplorationContext applicationContext;
            User user;
            NakedObjectSpecification userClass = NakedObjectSpecification.getNakedClass(User.class.getName());
            if (objectManager.hasInstances(userClass)) {
                NakedCollection users = objectManager.findInstances(userClass, name);
                if(users.size() == 0) {
                    throw new NakedObjectRuntimeException("No users found: " + name);
                }
                user = (User) users.elements().nextElement();
                applicationContext = (ExplorationContext) user.getRootObject();
             } else {
                user = new User(name);
                user.setContext(context);
                user.getRoles().add(new Role("explorer"));
                //user.makePersistent();

                applicationContext = new ExplorationContext();
                applicationContext.setContext(context);
                applicationContext.getName().setValue(name);
                applicationContext.associateUser(user);

                objectManager.makePersistent(applicationContext);
            }

            explorationSetUp.installFixtures();
            
            String[] classes = explorationSetUp.getClasses();
            for (int i = 0; i < classes.length; i++) {
                applicationContext.addClass(classes[i]);
             }            
            
            NakedCollection coll = objectManager.allInstances(userClass);
            applicationContext.setUpUsers(coll);

            
            
            
            // this is already part of ExplorationSetup??
            context.setUser(user);
            
            //User user = context.getUser();
            NakedObject rootObject = user.getRootObject();
            if (rootObject == null) {
                LOG.warn("User had no root context, a default one has been assigned");
                // TODO create a ExplorationContext that list the users, and allow
                // them to be changed between
                rootObject = new DefaultUserContext();
                rootObject.created();
                objectManager.makePersistent(rootObject);
                user.setRootObject(rootObject);
                ((DefaultUserContext) rootObject).setUser(user);
            }
            
            viewer.setTitle(name);
            viewer.init(rootObject, this);
            viewer.start();

        } catch (ConfigurationException e) {
            throw new NakedObjectRuntimeException(e);
        } catch( StartupException e) {
            if(objectManager == null) {
                objectManager.shutdown();
            }
            
            throw new NakedObjectRuntimeException(e);
        }
    }

    private NakedObjectManager installObjectManager(UpdateNotifier updateNotifier) throws StartupException {
        NakedObjectStore objectStore = (NakedObjectStore) ComponentLoader.loadComponent(OBJECT_STORE, TransientObjectStore.class,
                NakedObjectStore.class);
        OidGenerator oidGenerator = (OidGenerator) ComponentLoader.loadComponent("oidgenerator", SimpleOidGenerator.class, OidGenerator.class);
        LocalObjectManager objectManager = new LocalObjectManager(objectStore, updateNotifier, oidGenerator);
        objectManager.init();
        return objectManager;
    }

    private ObjectViewingMechanism installViewer() throws ConfigurationException, ComponentException {
        return  (ObjectViewingMechanism) ComponentLoader.loadComponent(VIEWING_MECHANISM,
                ObjectViewingMechanism.class);
    }

    private void loadConfiguration() throws ConfigurationException {
        Configuration.getInstance().load(DEFAULT_CONFIG);
        if (Configuration.getInstance().getString(SHOW_EXPLORATION_OPTIONS) == null) {
            Configuration.getInstance().add(SHOW_EXPLORATION_OPTIONS, "yes");
        }
        PropertyConfigurator.configure(Configuration.getInstance().getProperties("log4j"));
    }

    public void addFixture(ExplorationFixture fixture) {
        explorationSetUp.addFixture(fixture);
    }
    
    protected abstract void setUpFixtures();

    /** @deprecated use plural version */
    protected void setUpFixture(){}

     private void setUpLocale() {
        String localeSpec = Configuration.getInstance().getString("locale");
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

    private void showSplash() {
        boolean noSplash = Configuration.getInstance().getBoolean("nosplash", false);
        if (!noSplash) {
            SplashWindow splash = new SplashWindow();
            splash.removeAfterDelay(4);
        }
    }

    public void viewerClosing() {
        context.getObjectManager().shutdown();
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2003 Naked Objects Group
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