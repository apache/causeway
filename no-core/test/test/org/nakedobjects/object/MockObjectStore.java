package test.org.nakedobjects.object;

import org.nakedobjects.object.InstancesCriteria;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.ObjectNotFoundException;
import org.nakedobjects.object.ObjectPerstsistenceException;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.UnsupportedFindException;
import org.nakedobjects.object.persistence.objectstore.NakedObjectStore;
import org.nakedobjects.object.transaction.CreateObjectCommand;
import org.nakedobjects.object.transaction.DestroyObjectCommand;
import org.nakedobjects.object.transaction.PersistenceCommand;
import org.nakedobjects.object.transaction.SaveObjectCommand;
import org.nakedobjects.utility.NotImplementedException;

import java.util.Vector;

import junit.framework.Assert;


public class MockObjectStore implements NakedObjectStore {
    private Vector actions = new Vector();
    private NakedObjectSpecification expectedClass;
    private NakedObject getObject;
    private int instanceCount;
    private NakedObject[] instances = null;
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
    
    public void assertLastAction(int expectedLastAction) {
        int actualLastAction = actions.size() - 1;
        Assert.assertEquals(expectedLastAction, actualLastAction);
    }

    public void abortTransaction() {
        actions.addElement("abortTransaction");
    }

    public void setupGetObject(NakedObject object) {
        getObject = object;
    }

    public void createNakedClass(NakedObject cls) throws ObjectPerstsistenceException {}


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

    public NakedClass getNakedClass(String name) throws ObjectNotFoundException, ObjectPerstsistenceException {
        throw new NotImplementedException("Getting naked class " + name);
    }

    public NakedObject getObject(Oid oid, NakedObjectSpecification hint) throws ObjectNotFoundException, ObjectPerstsistenceException {
        if(getObject == null) {
            Assert.fail("no object expected");
        }
        Assert.assertEquals(getObject.getOid(), oid);
        return getObject;
    }

    public boolean hasInstances(NakedObjectSpecification cls, boolean includeSubclasses) {
        return hasInstances;
    }

    public void init() throws ObjectPerstsistenceException {
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

    public void resolveField(NakedObject object, NakedObjectField field) throws ObjectPerstsistenceException {}
    
    public void setupInstancesCount(int i) {
        instanceCount = i;
    }

    public void setupInstances(NakedObject[] instances, NakedObjectSpecification cls) {
        this.instances = instances;
        this.expectedClass = cls;
    }
    public void setupHasInstances(boolean flag) {
        hasInstances = flag;
    }

    public void shutdown() throws ObjectPerstsistenceException {
    }

    public void startTransaction() {
        actions.addElement("startTransaction");
    }

    public NakedObject[] getInstances(InstancesCriteria criteria) throws ObjectPerstsistenceException, UnsupportedFindException {
        actions.addElement("getInstances " + criteria);
        return instances;
    }

    public CreateObjectCommand createCreateObjectCommand(final NakedObject object) {
        actions.addElement("createObject " + object);
        return new CreateObjectCommand() {

            public void execute() throws ObjectPerstsistenceException {}

            public String toString() {
                return "CreateObjectCommand " + object.toString();
            }
            
            public NakedObject onObject() {
                return null;
            }};
    }

    public DestroyObjectCommand createDestroyObjectCommand(final NakedObject object) {
        actions.addElement("destroyObject " + object);
        return new DestroyObjectCommand() {

            public void execute() throws ObjectPerstsistenceException {}
        
            public String toString() {
                return "DestroyObjectCommand " + object.toString();
            }

            public NakedObject onObject() {
                return null;
            }
        };
    }

    public SaveObjectCommand createSaveObjectCommand(NakedObject object) {
        actions.addElement("saveObject " + object);
        return null;
    }

    public void runTransaction(PersistenceCommand[] commands) throws ObjectPerstsistenceException {
        for (int i = 0; i < commands.length; i++) {
            actions.addElement("run " + commands[i]);
            
            commands[i].execute();
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