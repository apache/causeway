package org.nakedobjects.distribution;

import org.nakedobjects.TestSystem;
import org.nakedobjects.distribution.dummy.DummyCollectionData;
import org.nakedobjects.distribution.dummy.DummyObjectData;
import org.nakedobjects.object.DummyNakedObjectSpecification;
import org.nakedobjects.object.MockOid;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.persistence.defaults.TestObject;

import java.util.Vector;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;


public class DataHelperCollectionTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(DataHelperCollectionTest.class);
    }

    private TestSystem system;
    private TestObject testObject1;
    private TestObject testObject2;

    protected void setUp() throws Exception {
        LogManager.getRootLogger().setLevel(Level.OFF);
        system = new TestSystem();

        system.addCreatedObject(new Vector());

        system.addSpecification(new DummyNakedObjectSpecification("type.1"));
        DummyNakedObjectSpecification collectionType = new DummyNakedObjectSpecification("type.2");
        collectionType.setupIsCollection();
        system.addSpecification(collectionType);

        system.addCreatedObject(testObject1 = new TestObject());
        system.addCreatedObject(testObject2 = new TestObject());

        system.init();
    }

    protected void tearDown() throws Exception {
        system.shutdown();
    }

    public void testRecreateCollection() {
        ObjectData elements[] = new ObjectData[2];
        MockOid fieldOid = new MockOid(345);
        elements[0] = new DummyObjectData(fieldOid, "type.1", null, false, 2);
        fieldOid = new MockOid(678);
        elements[1] = new DummyObjectData(fieldOid, "type.1", null, false, 2);

        MockOid collectionOid = new MockOid(123);
        CollectionData data = new DummyCollectionData(collectionOid, "type.2", elements, 4);

        NakedCollection naked = (NakedCollection) DataHelper.restore(data);
        //		assertEquals(4, naked.getVersion());
        assertEquals(collectionOid, naked.getOid());

        Vector restoredCollection = (Vector) naked.getObject();
        assertEquals(2, restoredCollection.size());

        assertEquals(testObject1, restoredCollection.elementAt(0));
        assertEquals(testObject2, restoredCollection.elementAt(1));
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