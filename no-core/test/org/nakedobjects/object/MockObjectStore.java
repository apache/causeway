package org.nakedobjects.object;

import org.nakedobjects.object.reflect.simple.JavaReflector;
import org.nakedobjects.utility.NotImplementedException;

import java.util.Vector;

import junit.framework.Assert;


public class MockObjectStore implements NakedObjectStore {
    private Vector actions = new Vector();
    private NakedClass expectedClass;
    private NakedObject getObject;
    private int instanceCount;
    private Vector instances = null;
    private MockLoadedObjects mockLoadedObjects = new MockLoadedObjects();
    private boolean hasInstances;

    public MockObjectStore() {
        super();
    }

    public void abortTransaction() {
        actions.addElement("abortTransaction");
    }

    void setupGetObject(NakedObject object) {
        getObject = object;
    }

    public void createNakedClass(NakedClass cls) throws ObjectStoreException {}

    public void createObject(NakedObject object) throws ObjectStoreException {
        actions.addElement("createObject " + object);
    }

    public void destroyObject(NakedObject object) throws ObjectStoreException {
        actions.addElement("destroyObject " + object.getOid());
    }

    public void endTransaction() {
        actions.addElement("abortTransaction");
    }

    public Vector getActions() {
        return actions;
    }

    public String getDebugData() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getDebugTitle() {
        // TODO Auto-generated method stub
        return null;
    }

    public Vector getInstances(NakedClass cls, boolean includeSubclasses) {
        if (instances == null) {
            Assert.fail("no predefined instances");
        }
        Assert.assertEquals(expectedClass, cls);
        actions.addElement("getInstances " + cls);

        return instances;
    }

    public Vector getInstances(NakedClass cls, String pattern, boolean includeSubclasses) throws ObjectStoreException, UnsupportedFindException {
        throw new NotImplementedException();
    }

    public Vector getInstances(NakedObject pattern, boolean includeSubclasses) {
        actions.addElement("getInstances " + pattern);

        return instances;
    }

    public LoadedObjects getLoadedObjects() {
        return mockLoadedObjects;
    }

    public NakedClass getNakedClass(String name) throws ObjectNotFoundException, ObjectStoreException {
        if (expectedClass == null) {
            throw new NotImplementedException("Getting naked class " + name);
        } else {
            return expectedClass;
        }
    }

    public NakedObject getObject(Object oid, NakedClass hint) throws ObjectNotFoundException, ObjectStoreException {
        if(getObject == null) {
            Assert.fail("no object expected");
        }
        Assert.assertEquals(getObject.getOid(), oid);
        return getObject;
    }

    public boolean hasInstances(NakedClass cls, boolean includeSubclasses) {
        return hasInstances;
    }

    public void init() throws ObjectStoreException {
    // TODO Auto-generated method stub
    }

    public String name() {
        // TODO Auto-generated method stub
        return null;
    }

    public int numberOfInstances(NakedClass cls, boolean includedSubclasses) {
        return instanceCount;
    }

    public void reset() {
        instanceCount = 0;
        actions.clear();
    }

    public void resolve(NakedObject object) {
    // TODO Auto-generated method stub
    }

    public void save(NakedObject object) throws ObjectStoreException {
        actions.addElement("saveObject " + object);
    }

    public void setupInstancesCount(int i) {
        instanceCount = i;
    }

    public void setupInstances(Vector instances, NakedClass cls) {
        this.instances = instances;
        this.expectedClass = cls;
    }

    public void setupIsLoaded(boolean flag) {
        mockLoadedObjects.setupIsLoaded(flag);
    }

    public void setupHasInstances(boolean flag) {
        hasInstances = flag;
    }

    public void setupLoaded(NakedObject[] objects) {
        mockLoadedObjects.setupLoadedObjects(objects);
    }

    public void setupNakedClass(Class cls) {
        setupNakedClass(NakedClass.createNakedClass(cls.getName(), JavaReflector.class.getName()));
    }

    public void setupNakedClass(NakedClass nakedClass) {
        this.expectedClass = nakedClass;
    }

    public void shutdown() throws ObjectStoreException {
    // TODO Auto-generated method stub
    }

    public void startTransaction() {
        actions.addElement("abortTransaction");
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2004 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects
 * Group is Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */