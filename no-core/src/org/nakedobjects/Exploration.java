package org.nakedobjects;

import org.nakedobjects.object.DefaultUserContext;
import org.nakedobjects.object.LocalObjectManager;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedClassManager;
import org.nakedobjects.object.NakedError;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectManager;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectStore;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.TransientObjectStore;
import org.nakedobjects.object.collection.InstanceCollection;
import org.nakedobjects.object.value.Date;
import org.nakedobjects.object.value.Time;
import org.nakedobjects.object.value.TimeStamp;
import org.nakedobjects.security.Role;
import org.nakedobjects.security.SecurityContext;
import org.nakedobjects.security.Session;
import org.nakedobjects.security.User;
import org.nakedobjects.utility.ComponentLoader;
import org.nakedobjects.utility.Configuration;
import org.nakedobjects.utility.ConfigurationException;
import org.nakedobjects.utility.NotImplementedException;
import org.nakedobjects.utility.SplashWindow;
import org.nakedobjects.utility.StartupException;

import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


public abstract class Exploration {
    private static final String DEFAULT_CONFIG = "nakedobjects.properties";
    private static final Logger LOG = Logger.getLogger(Exploration.class);
    public static final String OBJECT_STORE = "object-store";
    private static final String SHOW_EXPLORATION_OPTIONS = "viewer.lightweight.show-exploration";
    private final static String VIEWING_MECHANISM = "viewer";
    private ExplorationContext context;
    private Vector newInstances = new Vector();
    protected NakedObjectManager objectManager;
    private Vector fixtures = new Vector();
    private ExplorationClock clock;
    
    protected Exploration() {
        try {
            Properties p = Configuration.loadProperties("log4j.properties");
            PropertyConfigurator.configure(p);
        } catch (ConfigurationException e) {
            BasicConfigurator.configure();
        }

        Logger.getRootLogger().setLevel(Level.WARN);

        try {
            start();
        } catch (ConfigurationException e) {
            throw new NakedObjectRuntimeException(e);
        } catch (StartupException e) {
            throw new NakedObjectRuntimeException(e);
        }
    }

    /** @deprecated */
    private void addInstance(NakedObject object) {
        LOG.info("Adding " + object);
        newInstances.addElement(object);
    }

    /** @deprecated */
    private NakedClassManager classManager() {
        return NakedClassManager.getInstance();
    }

    /**
     * hook method which the subclass must implement to add any classes into the
     * supplied <code>NakedClassList</code>
     * 
     * @deprecated
     */
    public void classSet(NakedClassList classes) {}

    /**
     * Helper method to create an instance of the given type. Provided for
     * exploration programs that need to set up instances.
     * 
     *@deprecated
     */
    protected final NakedObject createInstance(Class type) {
        NakedClass nc = classManager().getNakedClass(type.getName());
        if (nc == null) { return new NakedError("Could not create an object of class " + type); }
        NakedObject object = (NakedObject) nc.acquireInstance();
        object.created();
        addInstance(object);
        return object;
    }

    /**
     * Helper method to create an instance of the given type. Provided for
     * exploration programs that need to set up instances.
     * 
     * @deprecated
     */
    protected final NakedObject createInstance(String className) {
        NakedClass nc = classManager().getNakedClass(className);
        if (nc == null) { return new NakedError("Could not create an object of class " + className); }
        NakedObject object = (NakedObject) nc.acquireInstance();
        object.created();
        addInstance(object);
        return object;
    }

    /**
     * @deprecated use needsInstances
     */
    protected final boolean hasNoInstances(Class cls) {
        return needsInstances(cls);
    }

    /**
     * @deprecated use needsInstances
     */
    protected final boolean hasNoInstances(String className) {
        return needsInstances(className);
    }

    private void loadConfiguration() throws ConfigurationException {
        Configuration.getInstance().load(DEFAULT_CONFIG);
        if (Configuration.getInstance().getString(SHOW_EXPLORATION_OPTIONS) == null) {
            Configuration.getInstance().add(SHOW_EXPLORATION_OPTIONS, "yes");
        }
        PropertyConfigurator.configure(Configuration.getInstance().getProperties("log4j"));
    }

    /**
     * Convenience method provided for subclasses, indicating whether there are
     * any instances of the specified class
     * 
     * @deprecated
     */
    protected final boolean needsInstances(Class cls) {
        return needsInstances(cls.getName());
    }

    /**
     * Convenience method provided for subclasses, indicating whether there are
     * any instances of the specified class
     * 
     * @deprecated
     */
    protected final boolean needsInstances(String className) {
        return !objectManager.hasInstances(classManager().getNakedClass(className));
    }

    /** @deprecated */
	public void registerClass(Class cls) {
        registerClass(cls.getName());
    }

        /** @deprecated */
    public void registerClass(String className) {
        NakedClass nc = NakedClassManager.getInstance().getNakedClass(className);
        context.getClasses().add(nc);
    }

    public void addFixture(ExplorationFixture fixture) {
        fixtures.addElement(fixture);
    }
    
    protected abstract void setUpFixture();

    private void setUpExploration() {
         setUpFixture();
        ExplorationSetUp fs = new ExplorationSetUp();
        fs.init(fixtures, NakedClassManager.getInstance(), objectManager, clock);
        
        String[] classes = fs.getClasses();
        for (int i = 0; i < classes.length; i++) {
            NakedClass nc = NakedClassManager.getInstance().getNakedClass(classes[i]);
            context.getClasses().add(nc);
        }
        
        InstanceCollection coll = InstanceCollection.allInstances(User.class.getName());
        context.setUpUsers(coll);

        // make all new objects persistent
        for (int i = 0; i < newInstances.size(); i++) {
            NakedObject object = (NakedObject) newInstances.elementAt(i);
            LOG.info("Persisting " + object);

            if (!object.isPersistent()) {
                objectManager.makePersistent(object);
            }
        }

    }

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

    /**
     * Set up 
     */
    private User setUpUsers() {
        NakedClass userClass = classManager().getNakedClass(User.class.getName());

        String name = this.getClass().getName();
        name = name.substring(name.lastIndexOf('.') + 1);

        User user;

        if (!objectManager.hasInstances(userClass)) {

            user = new User(name);
            user.getRoles().add(new Role("explorer"));
            //user.makePersistent();

            context = new ExplorationContext();
            context.getName().setValue(name);
            context.associateUser(user);

            objectManager.makePersistent(context);

            NakedClassList set = new NakedClassList();
            classSet(set);
            set.setContext(context);
        } else {
            InstanceCollection users = InstanceCollection.findInstances(User.class.getName(), name);
            user = (User) users.elements().nextElement();
            context = (ExplorationContext) user.getRootObject();
        }

        return user;
    }

    private void showSplash() {
        boolean noSplash = Configuration.getInstance().getBoolean("nosplash", false);
        if (!noSplash) {
            SplashWindow splash = new SplashWindow();
            splash.removeAfterDelay(4);
        }
    }

    private void start() throws StartupException {
        loadConfiguration();
        showSplash();
        setUpLocale();

        clock = new ExplorationClock();
        Date.setClock(clock);
        Time.setClock(clock);
        TimeStamp.setClock(clock);
        
        NakedObjectStore objectStore = (NakedObjectStore) ComponentLoader.loadComponent(OBJECT_STORE, TransientObjectStore.class,
                NakedObjectStore.class);

        ObjectViewingMechanism viewer = (ObjectViewingMechanism) ComponentLoader.loadComponent(VIEWING_MECHANISM,
                ObjectViewingMechanism.class);

        objectManager = new LocalObjectManager(objectStore, viewer.getUpdateNotifier());
        
        try {
            objectManager.init();
            
            User user = setUpUsers();
            
            setUpExploration();
            
            //        Session.initSession();
            SecurityContext context = new SecurityContext(null, user);
            
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
            
            String name = this.getClass().getName();
            name = name.substring(name.lastIndexOf('.') + 1);
            
            viewer.setTitle(name);
            viewer.init(rootObject);
            
            viewer.start();
        } catch( StartupException e) {
            if(objectStore == null) {
                try {
                    objectStore.shutdown();
                } catch (ObjectStoreException e1) {
                    e1.printStackTrace();
                }
            }
            
            if(objectManager == null) {
                objectManager.shutdown();
            }
            
            throw e;
        }
    }

    
    public NakedObjectManager getObjectManager() {
        return objectManager;
        }

    public NakedClassManager getClassManager() {
        throw new NotImplementedException();
        }

    public Locale getLocale() {
        throw new NotImplementedException();
    }

    public Session getSession() {
        throw new NotImplementedException();
        }
    
    public ExplorationClock getClock() {
      return clock;  
    }
    
     public void setUser(User user) {
        throw new NotImplementedException();        
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