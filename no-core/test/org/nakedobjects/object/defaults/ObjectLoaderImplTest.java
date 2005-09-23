package org.nakedobjects.object.defaults;

import org.nakedobjects.object.DummyNakedObjectSpecification;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.ResolveState;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.object.persistence.defaults.DummyOid;
import org.nakedobjects.object.reflect.PojoAdapter;

import java.util.Date;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;


public class ObjectLoaderImplTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ObjectLoaderImplTest.class);
    }

    private PojoAdapter adapter1;
    private IdentityAdapterMap identityAdapterMap;
    private ObjectLoaderImpl objectLoader;
    private DummyOid oid1;
    private Object pojo1;
    private PojoAdapterHash pojoAdapterMap;
    private Date pojo2;
    private DummyOid oid2;
    private PojoAdapter adapter2;
    private DummyNakedObjectSpecification specification;
    private DummyObjectFactory objectFactory;

    protected void setUp() throws Exception {
        LogManager.getLoggerRepository().setThreshold(Level.OFF);

        objectLoader = new ObjectLoaderImpl();
        pojoAdapterMap = new PojoAdapterHashImpl();
        objectLoader.setPojoAdapterMap(pojoAdapterMap);
        identityAdapterMap = new IdentityAdapterMapImpl();
        objectLoader.setIdentityAdapterMap(identityAdapterMap);
        objectFactory = new DummyObjectFactory();
        objectLoader.setObjectFactory(objectFactory);
        
        specification = new DummyNakedObjectSpecification();
                
        pojo1 = new Date();
        oid1 = new DummyOid();
        adapter1 = new PojoAdapter(pojo1) {
            public NakedObjectSpecification getSpecification() {
                return specification;
            }
        };
        
        pojo2 = new Date();
        oid2 = new DummyOid();
        adapter2 = new PojoAdapter(pojo2) {
            public Oid getOid() {
                return oid2;
            }
            public NakedObjectSpecification getSpecification() {
                return specification;
            }
        };
    }

    public void testCreateAdapterForTransient() {
        PojoAdapter adapter = (PojoAdapter) objectLoader.createAdapterForTransient(pojo1);
        assertEquals(ResolveState.TRANSIENT, adapter.getResolveState());
        assertEquals(pojo1, adapter.getObject());
        assertTrue(pojoAdapterMap.containsPojo(pojo1));
    }

    public void testGetAdapterForPojo() {
        assertNull(objectLoader.getAdapterFor(pojo1));
        pojoAdapterMap.add(pojo1, adapter1);
        assertEquals(adapter1, objectLoader.getAdapterFor(pojo1));
    }

    public void testCreateAdapterForStringValue() {
        NakedValue adapter = objectLoader.createAdapterForValue("fred");
        assertEquals("fred", adapter.getObject());
        assertFalse(pojoAdapterMap.containsPojo("fred"));
    }

    public void testCreateAdapterForCharValue() {
        NakedValue adapter = objectLoader.createAdapterForValue(new Character('g'));
        assertEquals(new Character('g'), adapter.getObject());
        assertFalse(pojoAdapterMap.containsPojo(new Character('g')));
    }

    public void testCreateAdapterForByteValue() {
        NakedValue adapter = objectLoader.createAdapterForValue(new Byte((byte) 123));
        assertEquals(new Byte((byte) 123), adapter.getObject());
        assertFalse(pojoAdapterMap.containsPojo(new Byte((byte) 123)));
    }

    public void testCreateAdapterForShortValue() {
        NakedValue adapter = objectLoader.createAdapterForValue(new Short((short) 123));
        assertEquals((new Short((short) 123)), adapter.getObject());
        assertFalse(pojoAdapterMap.containsPojo((new Short((short) 123))));
    }

    public void testCreateAdapterForIntegerValue() {
        NakedValue adapter = objectLoader.createAdapterForValue(new Integer(123));
        assertEquals(new Integer(123), adapter.getObject());
        assertFalse(pojoAdapterMap.containsPojo(new Integer(123)));
    }

    public void testCreateAdapterForLongValue() {
        NakedValue adapter = objectLoader.createAdapterForValue(new Long(123));
        assertEquals(new Long(123), adapter.getObject());
        assertFalse(pojoAdapterMap.containsPojo(new Long(123)));
    }

    public void testCreateAdapterForFloatValue() {
        NakedValue adapter = objectLoader.createAdapterForValue(new Float(123));
        assertEquals(new Float(123), adapter.getObject());
        assertFalse(pojoAdapterMap.containsPojo(new Float(123)));
    }

    public void testCreateAdapterForDoubleValue() {
        NakedValue adapter = objectLoader.createAdapterForValue(new Double(123));
        assertEquals(new Double(123), adapter.getObject());
        assertFalse(pojoAdapterMap.containsPojo(new Double(123)));
    }

    public void testCreateAdapterForBooleanValue() {
        NakedValue adapter = objectLoader.createAdapterForValue(new Boolean(true));
        assertEquals(new Boolean(true), adapter.getObject());
        assertFalse(pojoAdapterMap.containsPojo(new Boolean(true)));
    }

    public void testGetAdapterByOid() {
        assertEquals(null, objectLoader.getAdapterFor(oid1));

        identityAdapterMap.put(oid1, adapter1);
        assertEquals(adapter1, objectLoader.getAdapterFor(oid1));
    }

    public void testIdentityIsKnownForAnOid() {
        assertFalse(objectLoader.isIdentityKnown(oid1));

        identityAdapterMap.put(oid1, adapter1);
        assertTrue(objectLoader.isIdentityKnown(oid1));
    }

    public void testUnloadRemovesObjectFromIdentityMap() {
        identityAdapterMap.put(oid2, adapter2);
        pojoAdapterMap.add(pojo2, adapter2);

        objectLoader.unloaded(adapter2);
 
        assertFalse(identityAdapterMap.containsKey(oid1));
        assertFalse(identityAdapterMap.contains(adapter2));

        assertFalse(pojoAdapterMap.containsPojo(adapter2));
    }

    public void testMadePersistentSetsOidAndAddsToIdentityMap() {
        adapter1.changeState(ResolveState.TRANSIENT);
        pojoAdapterMap.add(pojo1, adapter1);

        assertEquals(null, adapter1.getOid());
        assertFalse(identityAdapterMap.containsKey(oid1));

        
        objectLoader.madePersistent(adapter1, oid1);

        assertEquals(oid1, adapter1.getOid());
        assertTrue(identityAdapterMap.containsKey(oid1));
        assertEquals(adapter1, objectLoader.getAdapterFor(oid1));
    }
    
    public void testRecreateAdapterForPersistent() {
        objectFactory.setupCreateObject(pojo1);

        PojoAdapter adapter = (PojoAdapter) objectLoader.recreateAdapterForPersistent(oid1, specification);
        
        assertEquals(pojo1, adapter.getObject());
        assertEquals(ResolveState.GHOST, adapter.getResolveState());
        assertTrue(adapter.getResolveState().isPersistent());
        assertEquals(oid1, adapter.getOid());
        assertTrue(identityAdapterMap.containsKey(oid1));
    }

    
    public void testLoadingThenLoaded() {
        assertEquals(ResolveState.NEW, adapter1.getResolveState());
        adapter1.changeState(ResolveState.GHOST);
    
        objectLoader.start(adapter1, ResolveState.RESOLVING);
        assertEquals(ResolveState.RESOLVING, adapter1.getResolveState());
        objectLoader.end(adapter1);
        assertEquals(ResolveState.RESOLVED, adapter1.getResolveState());
    }
    
    
    public void testPartLoadingThenLoaded() {
        assertEquals(ResolveState.NEW, adapter1.getResolveState());
        adapter1.changeState(ResolveState.GHOST);
    
        objectLoader.start(adapter1, ResolveState.RESOLVING_PART);
        assertEquals(ResolveState.RESOLVING_PART, adapter1.getResolveState());
        objectLoader.end(adapter1);
        assertEquals(ResolveState.PART_RESOLVED, adapter1.getResolveState());
    }

    public void testLoadingAfterPartLoaded() {
        assertEquals(ResolveState.NEW, adapter1.getResolveState());
        adapter1.changeState(ResolveState.GHOST);
        adapter1.changeState(ResolveState.RESOLVING_PART);
        adapter1.changeState(ResolveState.PART_RESOLVED);

        objectLoader.start(adapter1, ResolveState.RESOLVING);
        assertEquals(ResolveState.RESOLVING, adapter1.getResolveState());
        objectLoader.end(adapter1);
        assertEquals(ResolveState.RESOLVED, adapter1.getResolveState());
    }


    public void testUpdating() {
        assertEquals(ResolveState.NEW, adapter1.getResolveState());
        adapter1.changeState(ResolveState.GHOST);
        adapter1.changeState(ResolveState.RESOLVING);
        adapter1.changeState(ResolveState.RESOLVED);

        objectLoader.start(adapter1, ResolveState.UPDATING);
        assertEquals(ResolveState.UPDATING, adapter1.getResolveState());
        objectLoader.end(adapter1);
        assertEquals(ResolveState.RESOLVED, adapter1.getResolveState());
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user. Copyright (C) 2000 -
 * 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is Kingsway House, 123
 * Goldworth Road, Woking GU21 1NR, UK).
 */