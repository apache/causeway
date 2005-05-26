package org.nakedobjects.object.defaults;

import org.nakedobjects.object.DirtyObjectSet;
import org.nakedobjects.object.MockNakedObject;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedError;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.ObjectFactory;
import org.nakedobjects.object.persistence.InstancesCriteria;
import org.nakedobjects.object.persistence.ObjectStoreException;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.utility.NotImplementedException;

import java.util.Vector;

import junit.framework.Assert;


public class MockObjectManager extends AbstractNakedObjectManager {
    
    public MockObjectManager(ObjectFactory factory) {
        super(factory);
    }

    public MockObjectManager() {
        super(new MockObjectFactory());
    }


    public static MockObjectManager setup() {
        MockObjectManager manager;
        manager = new MockObjectManager(new MockObjectFactory());
        manager.reset();
        return manager;
    }

    private Vector actions = new Vector();
    
    
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

    public Oid createOid(Naked object) {
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

    public NakedObject[] getInstances(NakedObjectSpecification cls, boolean includeSubclasses) {
        return new NakedObject[] {
               new MockNakedObject(),
               new MockNakedObject(),
        };
    }

    public NakedObject[] getInstances(NakedObjectSpecification cls, String term, boolean includeSubclasses) {
        throw new NotImplementedException();
    }

    public NakedObject[] getInstances(NakedObject pattern, boolean includeSubclasses) {
        throw new NotImplementedException();
    }

    public NakedObject getObject(Oid oid, NakedObjectSpecification hint) {
        throw new NotImplementedException();
    }

    public NakedClass getNakedClass(NakedObjectSpecification nakedClass) {
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

    public void resolveImmediately(NakedObject object) {}

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

    public NakedError generatorError(String message, Exception e) {
        return null;
    }

    protected NakedObject[] getInstances(InstancesCriteria criteria) {
        return null;
    }

    public void saveChanges() {}

    public void resolveLazily(NakedObject object, NakedObjectField field) {}

    public void addObjectChangedListener(DirtyObjectSet listener) {}
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
