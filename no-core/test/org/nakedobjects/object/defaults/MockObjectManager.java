package org.nakedobjects.object.defaults;

import org.nakedobjects.object.MockNakedObject;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedError;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectContext;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectStore;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.Oid;
import org.nakedobjects.utility.NotImplementedException;

import java.util.Vector;

import junit.framework.Assert;


public class MockObjectManager extends AbstractNakedObjectManager {
    public static MockObjectManager setup() {
        MockObjectManager manager;
        manager = new MockObjectManager();
        manager.reset();
        return manager;
    }

    private Vector actions = new Vector();
    
    public MockObjectManager() {
    }

    public void abortTransaction() {
        throw new NotImplementedException();
    }

    protected boolean accessRemotely() {
        return false;
    }

   /** @deprecated */
    public void setupAddClass(Class cls) {
//        classManager.setupAddNakedClass(cls);
    }
   
    public void assertAction(int i, String action) {
        if (i >= actions.size()) {
            Assert.fail("No such action " + action);
        }
        Assert.assertEquals(action, actions.elementAt(i));
    }

    protected void createClass(NakedObjectSpecification nc) throws ObjectStoreException {}

    public Oid createOid(NakedObject object) {
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

    public NakedObject[] getInstances(NakedObjectSpecification cls) {
        return new NakedObject[] {
               new MockNakedObject(),
               new MockNakedObject(),
        };
    }

    public NakedObject[] getInstances(NakedObjectSpecification cls, String term) {
        throw new NotImplementedException();
    }

    public NakedObject[] getInstances(NakedObject pattern) {
        throw new NotImplementedException();
    }

    public NakedObject getObject(Oid oid, NakedObjectSpecification hint) {
        throw new NotImplementedException();
    }

    public NakedClass getNakedClass(NakedObjectSpecification nakedClass) {
        throw new NotImplementedException();
    }
    
    public NakedObjectStore getObjectStore() {
        throw new NotImplementedException();
    }

    public boolean hasInstances(NakedObjectSpecification cls) {
        throw new NotImplementedException();
    }

    public void init() {}

    public void makePersistent(NakedObject object) {
        actions.addElement("make persistent " + object);
    }

    public int numberOfInstances(NakedObjectSpecification cls) {
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
        actions.addElement("serial number");
        return 108;
    }

    public void shutdown() {
//        classManager.shutdown();
        super.shutdown();
    }

    public void startTransaction() {
        actions.addElement("start transaction");
    }

    public NakedObjectContext getContext() {
        return new NakedObjectContext(this);
    }

    public NakedError generatorError(String message, Exception e) {
        return null;
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
