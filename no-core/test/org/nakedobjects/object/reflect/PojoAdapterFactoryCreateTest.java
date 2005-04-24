package org.nakedobjects.object.reflect;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.reflect.valueadapter.AbstractNakedValue;

import junit.framework.Assert;
import junit.framework.TestCase;


public class PojoAdapterFactoryCreateTest extends TestCase {

    private PojoAdapterFactoryImpl factory;
    private Cache cache;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(PojoAdapterFactoryCreateTest.class);
    }

    protected void setUp() throws Exception {
        cache = new Cache();
        factory = new PojoAdapterFactoryImpl();
        factory.setPojoAdapterHash(cache);
        factory.setReflectorFactory(new DummyReflectorFactory());
    }

    public void testValueCreatesAValueAdapter() {
        Naked naked = factory.createAdapter("test java object");
        assertTrue(naked instanceof AbstractNakedValue);
    }

    public void testValuesAreNotCached() {
        Naked naked = factory.createAdapter("test java object");
        assertNull(cache.action);

        Naked naked2 = factory.createAdapter("test java object");
        assertNotSame("Values are not cached, so will get new adapter each time", naked, naked2);
    }

    public void testGenericObectCreatesAPojoAdapter() {
        ExampleBusinessObject businessObject = new ExampleBusinessObject();
        Naked adapter = factory.createAdapter(businessObject);
        assertTrue(adapter instanceof PojoAdapter);
        assertEquals(businessObject, adapter.getObject());
    }

    public void testAdapterIsAddedToCache() {
        ExampleBusinessObject businessObject = new ExampleBusinessObject();
        factory.createAdapter(businessObject);
        assertEquals("add " + businessObject, cache.action);
    }

    public void testAdapterIsReturnedFromCache() {
        cache.adapter = new DummyNakedObject();
        cache.containsPojo = true;

        ExampleBusinessObject businessObject = new ExampleBusinessObject();
        cache.pojo = businessObject;

        Naked adapter = factory.createAdapter(businessObject);
        assertEquals(adapter, cache.adapter);
    }

    //   public void testReflectorFactoryGetsToCreateAdapters() {}

    public void testCantCreateAdapterForAnAdapter() {
        try {
            factory.createAdapter(new DummyNakedObject());
            fail();
        } catch (NakedObjectRuntimeException expected) {}
    }

    public void testResetClearsCache() {
        factory.reset();
        assertEquals("reset", cache.action);
    }

    public void testShutdownShutsCache() {
        factory.shutdown();
        assertEquals("shutdown", cache.action);

    }
}

class Cache implements PojoAdapterHash {
    ExampleBusinessObject pojo;
    String action = null;
    Naked adapter = null;
    boolean containsPojo = false;

    public void add(Object pojo, Naked adapter) {
        action = "add " + pojo;
    }

    public boolean containsPojo(Object pojo) {
        //        Assert.assertEquals(this.pojo, pojo);
        return containsPojo;
    }

    public Naked getPojo(Object pojo) {
        Assert.assertEquals(this.pojo, pojo);
        return adapter;
    }

    public void reset() {
        action = "reset";
    }

    public void shutdown() {
        action = "shutdown";
    }

    public String getDebugData() {
        return null;
    }

    public String getDebugTitle() {
        return null;
    }

}

class ExampleBusinessObject {}

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