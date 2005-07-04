package org.nakedobjects.object;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.defaults.InternalNakedObject;

import java.util.Vector;

import org.apache.log4j.Logger;


public abstract class ApplicationContext implements InternalNakedObject {
    private static final Logger LOG = Logger.getLogger(ApplicationContext.class);

    public static String fieldOrder() {
        return "user, classes, objects";
    }

    private final Vector classes = new Vector();
    private final Vector objects = new Vector();
    private UserId user = new UserId("user name");

    protected NakedClass addClass(Class cls) {
        return addClass(cls.getName());
    }

    protected NakedClass addClass(String className) {
        LOG.info("Added class " + className + " to " + this);
        NakedObjectSpecification nc = NakedObjects.getSpecificationLoader().loadSpecification(className);
        NakedClass nakedClass = NakedObjects.getObjectManager().getNakedClass(nc);
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

    public void associateUser(UserId user) {
        user.setRootObject(NakedObjects.getObjectLoader().getAdapterOrCreateTransientFor(this));
        this.setUser(user);
    }

    public void dissociateUser(UserId user) {
        user.setRootObject(null);
        this.setUser(null);
    }

    public Vector getClasses() {
        return classes;
    }

    public Vector getObjects() {
        return objects;
    }

    public UserId getUser() {
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

    public void setUser(UserId user) {
        this.user = user;
    }
}