package org.nakedobjects;

import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedClassManager;
import org.nakedobjects.object.NakedError;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectManager;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.collection.InstanceCollection;
import org.nakedobjects.security.SecurityContext;
import org.nakedobjects.security.Session;
import org.nakedobjects.security.User;

import java.util.Vector;

import org.apache.log4j.Logger;

public class ExplorationSetUp {
    private static final Logger LOG = Logger.getLogger(Exploration.class);
     private ExplorationFixture fixture;
    private Vector newInstances = new Vector();
    
    private ExplorationClock clock;
    private NakedClassManager classManager;
    private NakedObjectManager objectManager;
    private Vector classes;
    private String user;
    
    public void init(Vector fixtures, NakedClassManager classManager, NakedObjectManager objectManager, ExplorationClock clock) {
        this.classManager = classManager;
        this.objectManager = objectManager;
        this.clock = clock;
        
        classes = new Vector();
        
        for (int i = 0, last = fixtures.size(); i < last; i++) {
            ExplorationFixture fixture = (ExplorationFixture) fixtures.elementAt(i);
            fixture.setContainer(this);
            fixture.install();
        }
       
        // make all new objects persistent
        for (int i = 0; i < newInstances.size(); i++) {
            NakedObject object = (NakedObject) newInstances.elementAt(i);
            LOG.info("Persisting " + object);

            if (!object.isPersistent()) {
                objectManager.makePersistent(object);
            }
        }
        
        loginUser();
    }

    private void addInstance(NakedObject object) {
        LOG.info("Adding " + object);
        newInstances.addElement(object);
    }

    /**
     * Helper method to create an instance of the given type. Provided for
     * exploration programs that need to set up instances.
     */
    protected final NakedObject createInstance(Class type) {
        NakedClass nc = classManager.getNakedClass(type.getName());
        if (nc == null) { return new NakedError("Could not create an object of class " + type); }
        NakedObject object = (NakedObject) nc.acquireInstance();
        object.created();
        addInstance(object);
        return object;
    }

    /**
     * Helper method to create an instance of the given type. Provided for
     * exploration programs that need to set up instances.
     */
    protected final NakedObject createInstance(String className) {
        NakedClass nc = classManager.getNakedClass(className);
        if (nc == null) { return new NakedError("Could not create an object of class " + className); }
        NakedObject object = (NakedObject) nc.acquireInstance();
        object.created();
        addInstance(object);
        return object;
    }

    /**
     * Convenience method provided for subclasses, indicating whether there are
     * any instances of the specified class
     */
    protected final boolean needsInstances(Class cls) {
        return needsInstances(cls.getName());
    }

    /**
     * Convenience method provided for subclasses, indicating whether there are
     * any instances of the specified class
     */
    protected final boolean needsInstances(String className) {
        return !objectManager.hasInstances(classManager.getNakedClass(className));
    }

    
    public void resetClock() {
        clock.reset();
    }

    public void setTime(int hour, int minute) {
        clock.setTime(hour, minute);
    }

    public void setDate(int year, int month, int day) {
        clock.setDate(year, month, day);
    }

    public void registerClass(String className) {
        classes.addElement(className);
    }
    
    
    public String[] getClasses() {
        String[] classNames = new String[classes.size()];
        classes.copyInto(classNames);
        return classNames;
    }
    
    public void setUser(String name) {
        this.user = name;
    }

    private void loginUser() {
        Session.initSession();
        if(user != null) {
	        NakedClass cls = NakedClassManager.getInstance().getNakedClass(User.class.getName());
	        InstanceCollection coll = InstanceCollection.findInstances(cls, user);
	        if(coll.size() == 0) {
	            throw new NakedObjectRuntimeException("No user " + user);
	        }
	        User user = (User) coll.elements().nextElement();
	        Session.setLoggedOn(new SecurityContext(null, user));
        }
    }
    

}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2004  Naked Objects Group Ltd

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