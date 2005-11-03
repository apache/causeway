package org.nakedobjects.object.defaults;


import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.User;
import org.nakedobjects.object.UserContext;

import java.util.Vector;

import org.apache.log4j.Logger;


public abstract class ApplicationContext implements UserContext {
    private static final Logger LOG = Logger.getLogger(ApplicationContext.class);

    public static String fieldOrder() {
        return "user, classes, objects";
    }

    private final Vector classes = new Vector();
    private final Vector objects = new Vector();
    private User user = new SimpleUser("user name", "id");

    protected NakedClass addClass(Class cls) {
        return addClass(cls.getName());
    }

    protected NakedClass addClass(String className) {
        LOG.info("added class " + className + " to " + this);
        NakedObjectSpecification nc = NakedObjects.getSpecificationLoader().loadSpecification(className);
        NakedClass nakedClass = NakedObjects.getPersistenceManager().getNakedClass(nc);
        classes.addElement(nakedClass);
        return nakedClass;
    }

    public void addToClasses(NakedClass cls) {
        classes.addElement(cls);
        objectChanged();
    }

    public String name() {
        return "Naked Object Application";
    }

    public void addToObjects(NakedObject cls) {
        objects.addElement(cls);
        objectChanged();
    }

    public void associateUser(SimpleUser user) {
        user.setRootObject(NakedObjects.getObjectLoader().getAdapterForElseCreateAdapterForTransient(this));
        this.setUser(user);
    }

    public void dissociateUser(SimpleUser user) {
        user.setRootObject(null);
        this.setUser(null);
    }

    public Vector getClasses() {
        return classes;
    }

    public Vector getObjects() {
        return objects;
    }

    public User getUser() {
        return user;
    }

    private void objectChanged() {}

    public void removeFromClasses(NakedClass cls) {
        classes.addElement(cls);
        objectChanged();
    }

    public void removeFromObjects(NakedObject cls) {
        objects.addElement(cls);
        objectChanged();
    }

    public void setUser(User user) {
        this.user = user;
    }
}