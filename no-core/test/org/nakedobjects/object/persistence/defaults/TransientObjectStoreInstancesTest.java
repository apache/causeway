package org.nakedobjects.object.persistence.defaults;

import org.nakedobjects.object.DummyOid;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.object.reflect.DummyNakedObject;

import java.util.Vector;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;


public class TransientObjectStoreInstancesTest extends TestCase {
    private MockTransientObjectStoreInstances instances;
    private int nextId = 0;
    private Oid oid;

    private Oid addInstance(String title) {
        Oid oid = new DummyOid(nextId++);
        instances.addElement(oid, "one");
        return oid;
    }

    protected void setUp() throws Exception {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.OFF);

        instances = new MockTransientObjectStoreInstances();

        addInstance("one");
        oid = addInstance("two");
        addInstance("three");
    }

    public void testAdd() {
        Oid oid = new DummyOid(2);
        DummyNakedObject object = new DummyNakedObject();
        object.setupOid(oid);

        instances.add(object);

        assertEquals(4, instances.size());
        assertEquals(oid, instances.objectInstances.elementAt(3));
    }

    public void testElements() {
        Vector v = new Vector();
        instances.instances(v);
        assertEquals(3, v.size());
        assertEquals(oid, v.elementAt(1));
    }

    public void testHasInstances() throws Exception {
        assertTrue(instances.hasInstances());
        assertEquals(3, instances.numberOfInstances());
    }

    public void testHasNoInstances() throws Exception {
        instances.objectInstances.clear();

        assertFalse(instances.hasInstances());
        assertEquals(0, instances.numberOfInstances());
    }

    public void testRemoveObject() throws Exception {
        instances.remove(oid);

        assertFalse(instances.contains(oid));
        assertEquals(2, instances.size());
    }

    public void testShutdownClearsAllInstances() {
        instances.shutdown();

        assertEquals(0, instances.size());
    }

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