package org.nakedobjects.object;

import org.nakedobjects.utility.NotImplementedException;

import java.util.Vector;

import junit.framework.Assert;


public class MockObjectManager extends NakedObjectManager {

    private static MockClassManager classManager;

    public static MockObjectManager setup() {
        MockObjectManager manager;
        manager = new MockObjectManager();
        classManager = MockClassManager.setup();
        manager.reset();
        return manager;
    }

    private Vector actions = new Vector();
 
    private MockObjectManager() {
    }

    public void abortTransaction() {
        throw new NotImplementedException();
    }

    protected boolean accessRemotely() {
        return false;
    }

    public void setupAddClass(Class cls) {
        classManager.setupAddNakedClass(cls);
    }

    public void assertAction(int i, String action) {
        if (i >= actions.size()) {
            Assert.fail("No such action " + action);
        }
        Assert.assertEquals(action, actions.elementAt(i));
    }

    protected void createClass(NakedClass nc) throws ObjectStoreException {}

    public Object createOid(NakedObject object) {
        throw new NotImplementedException();
    }

    public void destroyObject(NakedObject object) {
        throw new NotImplementedException();
    }

    public void endTransaction() {
        actions.addElement("end transaction");
    }

    public String getDebugData() {
        throw new NotImplementedException();
    }

    public String getDebugTitle() {
        throw new NotImplementedException();
    }

    public Vector getInstances(NakedClass cls) {
        throw new NotImplementedException();
    }

    public Vector getInstances(NakedClass cls, String term) {
        throw new NotImplementedException();
    }

    public Vector getInstances(NakedObject pattern) {
        throw new NotImplementedException();
    }

    public NakedObject getObject(Object oid, NakedClass hint) {
        throw new NotImplementedException();
    }

    public NakedObjectStore getObjectStore() {
        throw new NotImplementedException();
    }

    public boolean hasInstances(NakedClass cls) {
        throw new NotImplementedException();
    }

    public void init() {}

    public void makePersistent(NakedObject object) {
        actions.addElement("make persistent " + object);
    }

    public int numberOfInstances(NakedClass cls) {
        throw new NotImplementedException();
    }

    public void objectChanged(NakedObject object) {
        actions.addElement("object changed " + object);
    }

    public void reset() {
        actions.removeAllElements();
    }

    public void resolve(NakedObject object) {}

    public long serialNumber(String sequence) {
        throw new NotImplementedException();
    }

    public void shutdown() {
        classManager.shutdown();
        super.shutdown();
    }

    public void startTransaction() {
        actions.addElement("start transaction");
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
