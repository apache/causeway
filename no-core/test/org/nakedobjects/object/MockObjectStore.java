package org.nakedobjects.object;

import org.nakedobjects.object.persistence.CreateObjectCommand;
import org.nakedobjects.object.persistence.DestroyObjectCommand;
import org.nakedobjects.object.persistence.NakedObjectStore;
import org.nakedobjects.object.persistence.ObjectNotFoundException;
import org.nakedobjects.object.persistence.ObjectStoreException;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.object.persistence.PersistenceCommand;
import org.nakedobjects.object.persistence.SaveObjectCommand;
import org.nakedobjects.object.persistence.UnsupportedFindException;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.utility.NotImplementedException;

import java.util.Vector;

import junit.framework.Assert;


public class MockObjectStore implements NakedObjectStore {
    private Vector actions = new Vector();
    private NakedObjectSpecification expectedClass;
    private NakedObject getObject;
    private int instanceCount;
    private NakedObject[] instances = null;
    private MockLoadedObjects mockLoadedObjects = new MockLoadedObjects();
    private boolean hasInstances;

    public MockObjectStore() {
        super();
    }

    public void assertAction(int i, String expected) {
        Assert.assertTrue("invalid action number " + i, actions.size() > i);
        String actual = (String) actions.elementAt(i);
        
		if (expected == null && actual == null)
			return;
		if (expected != null && actual.startsWith(expected))
			return;
		Assert.fail("action " + i + " expected: <" + expected + "> but was: <" +actual + ">");
    }

    public void abortTransaction() {
        actions.addElement("abortTransaction");
    }

    public void setupGetObject(NakedObject object) {
        getObject = object;
    }

    public void createNakedClass(NakedObject cls) throws ObjectStoreException {}


    public void endTransaction() {
        actions.addElement("endTransaction");
    }

    public Vector getActions() {
        return actions;
    }

    public String getDebugData() {
        return null;
    }

    public String getDebugTitle() {
        return null;
    }

    public NakedObject[] getInstances(NakedObjectSpecification cls, boolean includeSubclasses) {
        if (instances == null) {
            Assert.fail("no predefined instances");
        }
        Assert.assertEquals(expectedClass, cls);
        actions.addElement("getInstances " + cls);

        return instances;
    }

    public NakedObject[] getInstances(NakedObjectSpecification cls, String pattern, boolean includeSubclasses) throws ObjectStoreException, UnsupportedFindException {
        return getInstances(cls, includeSubclasses);
    }

    public NakedObject[] getInstances(NakedObject pattern, boolean includeSubclasses) {
        actions.addElement("getInstances " + pattern);
        return instances;
    }

    public LoadedObjects getLoadedObjects() {
        return mockLoadedObjects;
    }

    public NakedClass getNakedClass(String name) throws ObjectNotFoundException, ObjectStoreException {
        throw new NotImplementedException("Getting naked class " + name);
    }

    public NakedObject getObject(Oid oid, NakedObjectSpecification hint) throws ObjectNotFoundException, ObjectStoreException {
        if(getObject == null) {
            Assert.fail("no object expected");
        }
        Assert.assertEquals(getObject.getOid(), oid);
        return getObject;
    }

    public boolean hasInstances(NakedObjectSpecification cls, boolean includeSubclasses) {
        return hasInstances;
    }

    public void init() throws ObjectStoreException {
    }

    public String name() {
        return null;
    }

    public int numberOfInstances(NakedObjectSpecification cls, boolean includedSubclasses) {
        return instanceCount;
    }

    public void reset() {
        instanceCount = 0;
        actions.clear();
    }

    public void resolveImmediately(NakedObject object) {
    }

    public void resolveEagerly(NakedObject object, NakedObjectField field) throws ObjectStoreException {}
    
    public void setupInstancesCount(int i) {
        instanceCount = i;
    }

    public void setupInstances(NakedObject[] instances, NakedObjectSpecification cls) {
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

    public void shutdown() throws ObjectStoreException {
    }

    public void startTransaction() {
        actions.addElement("startTransaction");
    }

    public NakedObject[] getInstances(InstancesCriteria criteria, boolean includeSubclasses) throws ObjectStoreException, UnsupportedFindException {
        actions.addElement("getInstances " + criteria);
        return instances;
    }

    public CreateObjectCommand createCreateObjectCommand(NakedObject object) {
        actions.addElement("createObject " + object);
        return null;
    }

    public DestroyObjectCommand createDestroyObjectCommand(final NakedObject object) {
        actions.addElement("destroyObject " + object);
        return new DestroyObjectCommand() {

            public void execute() throws ObjectStoreException {}
        
            public String toString() {
                return "DestroyObjectCommand " + object.toString();
            }
        };
    }

    public SaveObjectCommand createSaveObjectCommand(NakedObject object) {
        actions.addElement("saveObject " + object);
        return null;
    }

    public void runTransaction(PersistenceCommand[] commands) throws ObjectStoreException {
        for (int i = 0; i < commands.length; i++) {
            actions.addElement("run " + commands[i]);
        }
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2005 Naked Objects Group Ltd
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