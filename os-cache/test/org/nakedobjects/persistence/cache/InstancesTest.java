package org.nakedobjects.persistence.cache;

import org.nakedobjects.object.LoadedObjects;
import org.nakedobjects.object.MockLoadedObjects;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.Person;
import org.nakedobjects.object.Role;
import org.nakedobjects.object.defaults.LocalReflectionFactory;
import org.nakedobjects.object.defaults.MockObjectManager;
import org.nakedobjects.object.defaults.NakedObjectSpecificationImpl;
import org.nakedobjects.object.defaults.NakedObjectSpecificationLoaderImpl;
import org.nakedobjects.object.persistence.defaults.SerialOid;
import org.nakedobjects.object.reflect.defaults.JavaReflectorFactory;
import org.nakedobjects.object.system.TestClock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import junit.framework.TestCase;


public class InstancesTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(InstancesTest.class);
    }

    private Instances ins;
    private Person p1;
    private SerialOid p1Oid;
    private Person p2;
    private SerialOid p2Oid;

    public void setUp() {
       Logger.getRootLogger().setLevel(Level.OFF);
        
        new TestClock();
        new NakedObjectSpecificationLoaderImpl();
        NakedObjectSpecificationImpl.setReflectionFactory(new LocalReflectionFactory());
        NakedObjectSpecificationLoaderImpl.setReflectorFactory(new InternalReflectorFactory());
        
        
        NakedObjectSpecification nc = NakedObjectSpecificationLoader.getInstance().loadSpecification(Person.class.getName());
        LoadedObjects loaded = new MockLoadedObjects();
        ins = new Instances(nc, loaded);
        assertEquals(0, ins.numberInstances());

        p1 = new Person();
        p1.getName().setValue("Harry");
        p1.getSalary().setValue(1234);
        p1Oid = new SerialOid(1);
        p1.setOid(p1Oid);
        ins.create(p1);
        
        p2 = new Person();
        p2.getName().setValue("Fred");
        p1.getSalary().setValue(2345);
        p2Oid = new SerialOid(2);
        p2.setOid(p2Oid);
        ins.create(p2);
      }

    public void testCount() {
        assertEquals(2, ins.numberInstances());
    }

    public void testInstances() {
        Enumeration e = ins.instances();
        assertEquals(p1, e.nextElement());
        assertEquals(p2, e.nextElement());
    }

    public void testPersistOids() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        ins.saveIdentities(oos);
        oos.close();
        
        byte[] buffer = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
        ObjectInputStream ois = new ObjectInputStream(bais);
        
        NakedObjectSpecification personClass = NakedObjectSpecificationLoader.getInstance().loadSpecification(Person.class);
        Instances ins2 = new Instances(personClass, new MockLoadedObjects());
        ins2.loadIdentities(ois);
        ois.close();
        
        assertEquals(2, ins2.numberInstances());
        Enumeration e = ins2.instances();
        Object p1r = e.nextElement();
        assertEquals(p1, p1r);
        assertFalse(p1 == p1r);
        Object p2r = e.nextElement();
        assertEquals(p2, p2r);
        assertFalse(p2 == p2r);

    }

    public void testPersistData() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        ins.saveData(oos);
        oos.close();
        
        byte[] buffer = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
        ObjectInputStream ois = new ObjectInputStream(bais);
        
        MockObjectManager manager = MockObjectManager.setup();
        
        
        NakedObjectSpecification personClass = NakedObjectSpecificationLoader.getInstance().loadSpecification(Person.class);
        MockLoadedObjects loadedWithOids = new MockLoadedObjects();
        
        // skeleton objects - loaded objects
        Person p1r = new Person();
        p1r.setOid(p1.getOid());
        Person p2r = new Person();
        p2r.setOid(p2.getOid());
        loadedWithOids.setupLoadedObjects(new NakedObject[] {p1r, p2r});
        
        Instances ins2 = new Instances(personClass, loadedWithOids);
        assertEquals(2,  ins2.loadData(ois, manager.getContext()));
        ois.close();
        
        assertEquals("this method does not load up the instances", 0, ins2.numberInstances());
        
        assertEquals(p1, p1r);
        assertFalse(p1 == p1r);
        assertEquals(p1.getName(), p1r.getName());
        assertEquals(p1.getSalary(), p1r.getSalary());
        
        assertEquals(p2, p2r);
        assertFalse(p2 == p2r);
        assertEquals(p2.getName(), p2r.getName());
        assertEquals(p2.getSalary(), p2r.getSalary());
    }

    public void testPersistData2() throws Exception {
        LoadedObjects loaded = new MockLoadedObjects();
        NakedObjectSpecification nc = NakedObjectSpecificationLoader.getInstance().loadSpecification(Person.class);
        ins = new Instances(nc, loaded);
       
        Role r = new Role();
        r.setReferencedObject(p1);
        r.setOid(new SerialOid(6));
        ins.create(r);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        ins.saveData(oos);
        oos.close();
        
        byte[] buffer = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
        ObjectInputStream ois = new ObjectInputStream(bais);
        
        MockObjectManager manager = MockObjectManager.setup();
        
        
        NakedObjectSpecification roleClass = NakedObjectSpecificationLoader.getInstance().loadSpecification(Person.class);
        MockLoadedObjects loadedWithOids = new MockLoadedObjects();
        
        // skeleton objects - loaded objects
        Person p1r = new Person();
        p1r.setOid(p1.getOid());
        Person p2r = new Person();
        p2r.setOid(p2.getOid());
        Role r1r = new Role();
        r1r.setOid(r.getOid());
        loadedWithOids.setupLoadedObjects(new NakedObject[] {r1r, p1r, p2r});
        
        Instances ins2 = new Instances(roleClass, loadedWithOids);
        assertEquals(1,  ins2.loadData(ois, manager.getContext()));
        ois.close();
        /*
        assertEquals("this method does not load up the instances", 0, ins2.numberInstances());
        
        assertEquals(p1, p1r);
        assertFalse(p1 == p1r);
        assertEquals(p1.getName(), p1r.getName());
        assertEquals(p1.getSalary(), p1r.getSalary());
        
        assertEquals(p2, p2r);
        assertFalse(p2 == p2r);
        assertEquals(p2.getName(), p2r.getName());
        assertEquals(p2.getSalary(), p2r.getSalary());
        */
    }

   public void testRead() {
        assertEquals(p2, ins.read(p2Oid));
        assertEquals(p1, ins.read(p1Oid));
    }

    public void testRemove() {
        ins.remove(p1);
        assertEquals(1, ins.numberInstances());
        Enumeration e = ins.instances();
        assertEquals(p2, e.nextElement());
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