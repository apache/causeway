package org.nakedobjects.object;

import org.nakedobjects.container.configuration.ComponentException;
import org.nakedobjects.container.configuration.ConfigurationException;
import org.nakedobjects.object.defaults.SimpleNakedClass;


public abstract class NakedObjectStoreInstancesTestCase extends NakedObjectStoreTestCase {
    private Person people[];
    private NakedObjectSpecification personClass;
    private Person personPattern;
    private String personClassName;
 
    public NakedObjectStoreInstancesTestCase(String name) {
        super(name);
    }

    protected abstract NakedObjectStore installObjectStore() throws Exception;

    
    protected void initialiseObjects() throws Exception {
 
        // classes
        personClassName = Person.class.getName();
        personClass = NakedObjectSpecification.getSpecification(personClassName);

        // patterns
        personPattern = new Person();
        personPattern.makeFinder();

        // objects
        people = new Person[4];
        for (int i = 0; i < 4; i++) {
            people[i] = new Person();
            people[i].setOid(nextOid());
            objectStore.createObject(people[i]);

            assertNotNull(people[i].getOid());
        }
    }

    public void testDestroyObject() throws Exception {
        Object oid = people[2].getOid();
        objectStore.destroyObject(people[2]);
    
        restartObjectStore();
        
        NakedObject[] v = objectStore.getInstances(personClass, false);
        assertEquals(people.length - 1, v.length);
        // all instances should not be the one we destroyed
        for (int i = 0; i < v.length; i++) {
            assertFalse(people[2].equals(v[i]));
        }
    }

    public void testGetObject() throws Exception {
        restartObjectStore();
        
        for (int i = 0; i < people.length; i++) {
            Person person = (Person) objectStore.getObject(people[i].getOid(), personClass);
            assertEquals(people[i], person);
        }
    }

    public void testInstancesForClass() throws Exception {
        restartObjectStore();
        
        try {
            assertTrue(objectStore.hasInstances(personClass, false));

            assertEquals(people.length, objectStore.numberOfInstances(personClass, false));

            NakedObject[] v = objectStore.getInstances(personPattern, false);
            assertEquals(people.length, v.length);
            for (int i = 0; i < v.length; i++) {
                NakedObject element = (NakedObject) v[i];
                assertEquals(people[i], element);
            }
        } catch (UnsupportedFindException e) {}
    }

    public void testGetSameInstance() throws Exception {
        restartObjectStore();
        NakedObject p1 = objectStore.getObject(people[2].getOid(), personClass);
        NakedObject p2 = objectStore.getObject(people[2].getOid(), personClass);
        assertEquals(p1, p2);
     }
    
    public void testInstancesRepeatability() throws ConfigurationException, ComponentException, ObjectStoreException, Exception {
        restartObjectStore();

        try {
            NakedObject[] v1 = objectStore.getInstances(personPattern, false);
            NakedObject[] v2 = objectStore.getInstances(personPattern, false);
            
            for (int i = 0; i < v1.length; i++) {
                NakedObject element1 =(NakedObject) v1[i];
                NakedObject element2 = (NakedObject)v2[i];
                
                assertEquals(element1, element2);
            }
        } catch (UnsupportedFindException e) {}
    }
    
    public void testClassNotFound() throws Exception {
        try {
	        String name = "org.company.Test";
            objectStore.getNakedClass(name);
            fail();
        } catch (ObjectNotFoundException expected) {}
    }
    
    public void testClass() throws Exception {
        NakedClass nc = new SimpleNakedClass(personClassName);
        nc.setContext(context);
        nc.setOid(nextOid());
        objectStore.createNakedClass(nc);
        
        NakedClass nc2 = objectStore.getNakedClass(personClassName);
        assertEquals(nc, nc2);
        assertEquals(personClassName, nc2.getName());
    }
    
    public void testClassAsObjects() throws Exception {
       NakedClass nc = new SimpleNakedClass(personClassName);
        nc.setOid(nextOid());
        objectStore.createNakedClass(nc);
        
        NakedClass nc2 = (NakedClass) objectStore.getObject(nc.getOid(), nc.getSpecification());
        assertEquals(nc, nc2);
        assertEquals(personClassName, nc2.getName());
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2003 Naked Objects Group
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