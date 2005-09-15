package org.nakedobjects.distribution;

import org.nakedobjects.TestSystem;
import org.nakedobjects.distribution.dummy.DummyObjectData;
import org.nakedobjects.distribution.dummy.DummyValueData;
import org.nakedobjects.object.DummyNakedObjectSpecification;
import org.nakedobjects.object.MockOid;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.ResolveState;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.object.reflect.OneToOneAssociation;
import org.nakedobjects.object.reflect.TestPojoReferencePeer;
import org.nakedobjects.object.reflect.TestPojoValuePeer;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;


public class DataHelperTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(DataHelperTest.class);
    }

    private TestPojo object;
    private TestPojo referencedObject;
    private TestSystem system;

    protected void setUp() throws Exception {
        LogManager.getRootLogger().setLevel(Level.OFF);
        system = new TestSystem();

        object = new TestPojo();
        system.addCreatedObject(object);

        system.addSpecification(new DummyNakedObjectSpecification("type.1"));

        DummyNakedObjectSpecification spec = new DummyNakedObjectSpecification(TestPojo.class.getName());

        DummyNakedObjectSpecification valueFieldSpec = new DummyNakedObjectSpecification();
        valueFieldSpec.setupIsValue();
        OneToOneAssociation field1 = new OneToOneAssociation("cls", "one", valueFieldSpec, new TestPojoValuePeer());
        DummyNakedObjectSpecification referenceFieldSpec = new DummyNakedObjectSpecification();
        referencedObject = new TestPojo();
        system.addCreatedObject(referencedObject);
        OneToOneAssociation field2 = new OneToOneAssociation("cls", "two", referenceFieldSpec, new TestPojoReferencePeer());
        NakedObjectField[] fields = new NakedObjectField[] { field1, field2 };

        spec.setupFields(fields);
        system.addSpecification(spec);

        system.init();
    }

    protected void tearDown() throws Exception {
        system.shutdown();
    }

    public void testRecreatedObjectIsPartResolved() {
        MockOid oid = new MockOid(123);
        Object fields[] = new Object[0];
        Data data = new DummyObjectData(oid, "type.1", fields, false, 4);

        NakedObject naked = (NakedObject) DataHelper.restore(data);
        assertEquals(ResolveState.PART_RESOLVED, ((NakedObject) naked).getResolveState());
    }

    public void testRecreatedObjectIsResolved() {
        MockOid oid = new MockOid(123);
        Object fields[] = new Object[0];
        Data data = new DummyObjectData(oid, "type.1", fields, true, 4);

        NakedObject naked = (NakedObject) DataHelper.restore(data);
        assertEquals(ResolveState.RESOLVED, ((NakedObject) naked).getResolveState());
    }

    public void testRecreateObjectWithFieldData() {
        Object fields[] = new Object[3];
        fields[0] = new DummyValueData(new Integer(13), "");
        MockOid fieldOid = new MockOid(345);
        fields[1] = new DummyObjectData(fieldOid, "type.1", null, false, 2);
        
        // TODO test the one-to-many collection aswell
   //     fields[2] = new DummyCollectionData();
        
        MockOid rootOid = new MockOid(123);
        Data data = new DummyObjectData(rootOid, "type.1", fields, false, 4);

        NakedObject naked = (NakedObject) DataHelper.restore(data);
        assertEquals(4, naked.getVersion());
        assertEquals(rootOid, naked.getOid());

        TestPojo pojo = (TestPojo) naked.getObject();
        assertEquals(object, pojo);

        assertEquals(13, pojo.getValue());
        assertEquals(referencedObject, pojo.getReference());
    }

    public void testRecreateObjectWithNoFieldData() {
        MockOid oid = new MockOid(123);

        Data data = new DummyObjectData(oid, "type.1", null, false, 4);

        NakedObject naked = (NakedObject) DataHelper.restore(data);
        assertEquals(object, naked.getObject());
        assertEquals(4, naked.getVersion());
        assertEquals(oid, naked.getOid());
        assertEquals(ResolveState.GHOST, ((NakedObject) naked).getResolveState());
    }

    public void testRecreateTransientObjectGivenDataObject() {
        Data data = new DummyObjectData(null, "type.1", null, false, 4);

        NakedObject naked = (NakedObject) DataHelper.restore(data);
        assertEquals(object, naked.getObject());
        assertEquals(4, naked.getVersion());
        assertNull(naked.getOid());
    }

    public void testRecreateTransientObjectWithFieldData() {
        Data data = new DummyObjectData(null, "type.1", null, false, 4);

        NakedObject naked = (NakedObject) DataHelper.restore(data);
        assertEquals(object, naked.getObject());
        assertEquals(4, naked.getVersion());
        assertNull(naked.getOid());
    }

    public void testRecreateValue() {
        Data data = new DummyValueData(new Integer(11), "");

        Naked naked = DataHelper.restore(data);
        assertEquals(new Integer(11), naked.getObject());
        assertNull(naked.getOid());
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