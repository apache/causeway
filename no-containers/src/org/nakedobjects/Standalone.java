package org.nakedobjects;

import org.nakedobjects.container.configuration.ComponentException;
import org.nakedobjects.container.configuration.ComponentLoader;
import org.nakedobjects.container.configuration.ConfigurationException;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectManager;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.NakedObjectStore;
import org.nakedobjects.object.ObjectFactory;
import org.nakedobjects.object.OidGenerator;
import org.nakedobjects.object.ReflectorFactory;
import org.nakedobjects.object.UpdateNotifier;
import org.nakedobjects.object.defaults.AbstractUserContext;
import org.nakedobjects.object.defaults.LocalObjectManager;
import org.nakedobjects.object.defaults.LocalReflectionFactory;
import org.nakedobjects.object.defaults.NakedObjectSpecificationImpl;
import org.nakedobjects.object.defaults.NakedObjectSpecificationLoaderImpl;
import org.nakedobjects.object.defaults.SimpleOidGenerator;
import org.nakedobjects.object.defaults.TransientObjectStore;
import org.nakedobjects.object.reflect.PojoAdapter;
import org.nakedobjects.utility.StartupException;
import org.nakedobjects.viewer.ObjectViewingMechanism;
import org.nakedobjects.viewer.ObjectViewingMechanismListener;

import java.util.Vector;


public class Standalone extends NakedObjectsContainer implements ObjectViewingMechanismListener {
    public static final String OBJECT_STORE = "object-store";
    private static final String REFLECTOR_FACTORY = "reflector";
    private static final String USER_CONTEXT = "context";
    private static final String VIEWING_MECHANISM = "viewer";

    public static void main(String[] args) throws ConfigurationException {
        new Standalone().start();
    }

    private NakedObjectManager objectManager;

    public Standalone() {
        super();
    }

     /**
      * TODO The following 3 methods where taken from ExplorationSetup.  Need to work these together into 1.
      * see also addINstance()
      * 
     * Helper method to create an instance of the given type. Provided for
     * exploration programs that need to set up instances.
     */
    protected final NakedObject createInstance(Class type) {
        NakedObjectSpecification nc = NakedObjectSpecificationLoader.getInstance().loadSpecification(type.getName());
        if (nc == null) { return PojoAdapter.createNOAdapter(objectManager.generatorError("Could not create an object of class " + type, null)); }
        return createInstance(nc);
    }

    /**
     * Helper method to create an instance of the given type. Provided for
     * exploration programs that need to set up instances.
     */
    protected final NakedObject createInstance(String className) {
        NakedObjectSpecification nc = NakedObjectSpecificationLoader.getInstance().loadSpecification(className);
        if (nc == null) { return PojoAdapter.createNOAdapter(objectManager.generatorError("Could not create an object of class " + className, null)); }
        return createInstance(nc);
    }

    private NakedObject createInstance(NakedObjectSpecification nc) {
        NakedObject object = (NakedObject) nc.acquireInstance();
  //      object.setContext(context);
        object.created();
        addInstance(object);        
        return object;
    }

    private void addInstance(NakedObject object) {
        newInstances.addElement(object);
    }

    private Vector newInstances = new Vector();


    private NakedObjectManager installObjectManager(UpdateNotifier updateNotifier) throws StartupException {
        NakedObjectStore objectStore = (NakedObjectStore) ComponentLoader.loadComponent(OBJECT_STORE, TransientObjectStore.class,
                NakedObjectStore.class);
        OidGenerator oidGenerator = (OidGenerator) ComponentLoader.loadComponent("oidgenerator", SimpleOidGenerator.class,
                OidGenerator.class);
        ObjectFactory objectFactory = (ObjectFactory) ComponentLoader.loadComponent("object-factory", ObjectFactory.class);;
        LocalObjectManager objectManager = new LocalObjectManager(objectStore, updateNotifier, oidGenerator, objectFactory);
        objectManager.init();
        return objectManager;
    }

    protected void installObjects() {}

    private ReflectorFactory installReflectorFactory() throws ConfigurationException, ComponentException {
        return (ReflectorFactory) ComponentLoader.loadComponent(REFLECTOR_FACTORY, ReflectorFactory.class);
    }

    private AbstractUserContext installUserContext() throws ConfigurationException, ComponentException {
        return (AbstractUserContext) ComponentLoader.loadComponent(USER_CONTEXT, AbstractUserContext.class);
    }

    private ObjectViewingMechanism installViewer() throws ConfigurationException, ComponentException {
        return (ObjectViewingMechanism) ComponentLoader.loadComponent(VIEWING_MECHANISM, ObjectViewingMechanism.class);
    }

    protected void run() throws StartupException {
        new NakedObjectSpecificationLoaderImpl();
        NakedObjectSpecificationImpl.setReflectionFactory(new LocalReflectionFactory());
        NakedObjectSpecificationLoaderImpl.setReflectorFactory(installReflectorFactory());
        ObjectViewingMechanism viewer = installViewer();
        objectManager = installObjectManager(viewer.getUpdateNotifier());
/*
        String userName = Configuration.getInstance().getString("user", "standalone");
        NakedObjectSpecification userSpec = NakedObjectSpecificationLoader.getInstance().loadSpecification(User.class);
        NakedCollection users = objectManager.findInstances(userSpec, userName, true);
        AbstractUserContext rootObject;
        User user;
        if (users.size() == 0) {
            user = new User("standalone");
            objectManager.makePersistent(user);
            rootObject = installUserContext();
            rootObject.created();
 //           rootObject.setUser(user);
            objectManager.makePersistent(rootObject);
            user.setRootObject(rootObject);

            installObjects();
            
            // make all new objects persistent
            for (int i = 0; i < newInstances.size(); i++) {
                NakedObject object = (NakedObject) newInstances.elementAt(i);
                boolean notPersistent = object.getOid() == null;
                if (notPersistent) {
                    objectManager.makePersistent(object);
                }
            }
            

        } else if (users.size() > 1) {
            throw new StartupException("More that one user found based on the name " + userName);
        } else {
            user = (User) users.elementAt(0);
            rootObject = (AbstractUserContext) user.getRootObject();
            if (rootObject == null) {
                throw new StartupException("User " + user + " does not have a root object");
            }
        }
        ClientSession.getSession().setUser(user);

        viewer.setTitle("Naked Objects - standalone");
        viewer.init(rootObject, this);
        viewer.start();
*/
    }

    public void viewerClosing() {}

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