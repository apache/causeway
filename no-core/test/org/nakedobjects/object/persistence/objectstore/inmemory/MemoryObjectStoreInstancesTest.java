package org.nakedobjects.object.persistence.objectstore.inmemory;

import org.nakedobjects.object.NakedObjects;

import java.util.Enumeration;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import test.org.nakedobjects.object.DummyNakedObjectSpecification;
import test.org.nakedobjects.object.DummyOid;
import test.org.nakedobjects.object.MockNakedObject;
import test.org.nakedobjects.object.TestSystem;
import test.org.nakedobjects.object.persistence.defaults.TestObject;


public class MemoryObjectStoreInstancesTest extends TestCase {
    private DummyOid oid;
    private MockMemoryObjectStoreInstances instances;
    private TestObject object;
    private TestSystem system;

    protected void setUp() throws Exception {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.OFF);

        system = new TestSystem();
        system.init();
        
        system.addSpecification(new DummyNakedObjectSpecification());
        instances = new MockMemoryObjectStoreInstances(NakedObjects.getObjectLoader());

        instances.addElement(new DummyOid(1), new TestObject(), "one");

        oid = new DummyOid(2);
        object = new TestObject();
        instances.addElement(oid, object, "two");

        instances.addElement(new DummyOid(3), new TestObject(), "three");
    }

    protected void tearDown() throws Exception {
        system.shutdown();
    }
    /*
    public void testGetObjectWhichIsAlreadyLoaded() {
 //       mockPojoAdapterFactory.setupLoaded(true);
        DummyNakedObject object = new DummyNakedObject();
   //     mockPojoAdapterFactory.setupLoadedObject(object);
   //     mockPojoAdapterFactory.setupExpectedOid(oid);

        assertEquals(object, instances.getObject(oid));
    }
/*
    public void testGetNoObject() throws Exception {
        mockPojoAdapterFactory.setupLoaded(false);
        mockPojoAdapterFactory.setupExpectedOid(new MockOid(0));

        assertEquals(null, instances.getObject(new MockOid(0)));
    }

    public void testGetObjectWhichIsNotYetLoaded() throws Exception {
        mockPojoAdapterFactory.setupLoaded(false);
        mockPojoAdapterFactory.setupExpectedPojo(object);
        DummyNakedObject nakedObject = new DummyNakedObject();
        mockPojoAdapterFactory.setupCreatedAdapter(nakedObject);
        mockPojoAdapterFactory.setupExpectedOid(oid);

        assertEquals(nakedObject, instances.getObject(oid));
    }

    public void testRemoveObject() throws Exception {
        mockPojoAdapterFactory.setupLoaded(true);
        DummyNakedObject object = new DummyNakedObject();
        mockPojoAdapterFactory.setupLoadedObject(object);
        mockPojoAdapterFactory.setupExpectedOid(oid);

        instances.remove(oid);

        assertFalse(instances.contains(oid));
        assertEquals(2, instances.size());
    }
*/
    public void testHasInstances() throws Exception {
        assertTrue(instances.hasInstances());
        assertEquals(3, instances.numberOfInstances());
    }

    public void testHasNoInstances() throws Exception {
        instances.objectInstances.clear();

        assertFalse(instances.hasInstances());
        assertEquals(0, instances.numberOfInstances());
    }

    public void testElements() {
    /*
     * mockPojoAdapterFactory.setupLoaded(false);
     * mockPojoAdapterFactory.setupExpectedPojo(object); DummyNakedObject
     * nakedObject = new DummyNakedObject();
     * mockPojoAdapterFactory.setupCreatedAdapter(nakedObject);
     * 
     * Enumeration e = instances.elements();
     * 
     * e.nextElement(); e.nextElement(); e.nextElement();
     * assertFalse(e.hasMoreElements());
     */
    }

    public void testNoElements() {
        instances.objectInstances.clear();

        Enumeration e = instances.elements();
        assertFalse(e.hasMoreElements());
    }

    public void testOidForObject() {
        assertEquals(oid, instances.getOidFor(object));
    }

    public void testSave() {
        MockNakedObject mockNakedObject = new MockNakedObject();
        DummyOid oid = new DummyOid(0);
        mockNakedObject.setupOid(oid);
        TestObject object = new TestObject();
        mockNakedObject.setupObject(object);
        mockNakedObject.setupTitleString("four");

        instances.save(mockNakedObject);

        assertTrue(instances.objectInstances.containsKey(oid));
        assertTrue(instances.objectInstances.contains(object));
        assertTrue(instances.titleIndex.containsKey("four"));
        assertTrue(instances.titleIndex.contains(oid));
    }

    /*
    public void testInstancesByCriteria() {
        mockPojoAdapterFactory.setupExpectedOid(new MockOid(2));
        mockPojoAdapterFactory.setupExpectedPojo(object);
        DummyNakedObject nakedObject = new DummyNakedObject();
        mockPojoAdapterFactory.setupCreatedAdapter(nakedObject);
        
        Vector vector = new Vector();
        TitleCriteria criteria = new TitleCriteria(new DummyNakedObjectSpecification(), "two", false);
        instances.instances(criteria, vector);
        assertEquals(1, vector.size());
        assertEquals(nakedObject, vector.elementAt(0));
    }
    
    */
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