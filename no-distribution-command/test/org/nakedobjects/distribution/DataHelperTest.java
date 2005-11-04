package org.nakedobjects.distribution;

import org.nakedobjects.distribution.dummy.DummyObjectData;
import org.nakedobjects.distribution.dummy.DummyValueData;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.ResolveState;
import org.nakedobjects.object.defaults.NullDirtyObjectSet;
import org.nakedobjects.object.persistence.NullVersion;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

import test.org.nakedobjects.object.DummyNakedValue;
import test.org.nakedobjects.object.DummyOid;
import test.org.nakedobjects.object.TestObjectBuilder;
import test.org.nakedobjects.object.TestSystem;
import test.org.nakedobjects.object.reflect.DummyNakedObject;
import test.org.nakedobjects.object.reflect.DummyVersion;
import test.org.nakedobjects.object.reflect.TestPojo;
import test.org.nakedobjects.object.reflect.TestPojoValuePeer;
import test.org.nakedobjects.object.reflect.defaults.TestValue;


public class DataHelperTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(DataHelperTest.class);
    }

    private TestSystem system;
    private TestObjectBuilder rootObject;

    protected void setUp() throws Exception {
        LogManager.getRootLogger().setLevel(Level.OFF);
        DataHelper.setUpdateNotifer(new NullDirtyObjectSet());

        system = new TestSystem();
        system.init();
        
        
        TestObjectBuilder referencedObject;
        referencedObject = new TestObjectBuilder(new TestPojo());
//        referencedObject.setOid(new DummyOid(345));
        referencedObject.setResolveState(ResolveState.GHOST);
        
//        TestValue value = new TestValue(new TestPojoValuePeer());
        
        TestObjectBuilder obj;
        obj = new TestObjectBuilder(new TestPojo());
        rootObject = obj;
        obj.setOid(new DummyOid(123));
        obj.setResolveState(ResolveState.GHOST);

        obj.setValueField("value", new TestValue(new TestPojoValuePeer()));
//        obj.setValueField("value", value);
 //       obj.setReferenceField("reference", referencedObject);

        obj.init(system);
    }

    protected void tearDown() throws Exception {
        system.shutdown();
    }

    public void testRecreatedObjectIsPartResolved() {
        DummyOid oid = new DummyOid(123);
        Data fields[] = new Data[0];
        Data data = new DummyObjectData(oid, TestPojo.class.getName(), fields, false, new DummyVersion());

        NakedObject naked = (NakedObject) DataHelper.restore(data);
        assertEquals(ResolveState.PART_RESOLVED, ((NakedObject) naked).getResolveState());
    }

    public void testRecreatedObjectIsResolved() {
        DummyOid oid = new DummyOid(123);
        Data fields[] = new Data[0];
        Data data = new DummyObjectData(oid, TestPojo.class.getName(), fields, true, new DummyVersion());

        NakedObject naked = (NakedObject) DataHelper.restore(data);
        assertEquals(ResolveState.RESOLVED, ((NakedObject) naked).getResolveState());
    }

    public void testRecreateObjectWithFieldData() {
        Data fields[] = new Data[2];
        fields[0] = new DummyValueData(new Integer(13), "");
        DummyOid fieldOid = new DummyOid(345);
        fields[1] = new DummyObjectData(fieldOid, TestPojo.class.getName(), null, false, new DummyVersion());
        
        // TODO test the one-to-many collection aswell
        
        DummyOid rootOid = new DummyOid(123);
        Data data = new DummyObjectData(rootOid, TestPojo.class.getName(), fields, false, new DummyVersion(4));

        DummyNakedObject restored = (DummyNakedObject) DataHelper.restore(data);
        assertEquals(new DummyVersion(4), restored.getVersion());
        assertEquals(rootOid, restored.getOid());

        TestPojo pojo = (TestPojo) restored.getObject();
        assertEquals(rootObject.getPojo(), pojo);

        restored.assertFieldContains("value", new Integer(13));
    }

    public void testRecreateObjectWithNoFieldData() {
        DummyOid oid = new DummyOid(123);

        Data data = new DummyObjectData(oid, TestPojo.class.getName(), null, false, new NullVersion());

        NakedObject naked = (NakedObject) DataHelper.restore(data);
        assertEquals(rootObject.getPojo(), naked.getObject());
        assertEquals("no field data therefore only passing across reference", null, naked.getVersion());
        assertEquals(oid, naked.getOid());
        assertEquals(ResolveState.GHOST, ((NakedObject) naked).getResolveState());
    }

    public void testRecreateTransientObjectGivenDataObject() {
        Data data = new DummyObjectData(null, TestPojo.class.getName(), null, false, new DummyVersion());

        DummyNakedObject adapter = new DummyNakedObject();
        adapter.setupObject(rootObject);
        system.addRecreatedTransient(adapter);
        
        NakedObject naked = (NakedObject) DataHelper.restore(data);
        assertEquals(rootObject, naked.getObject());
        assertEquals("no field data therefore only passing across reference", null, naked.getVersion());
        assertNull(naked.getOid());
    }

    public void testRecreateTransientObjectWithFieldData() {
        Data data = new DummyObjectData(null, TestPojo.class.getName(), new Data[0], false, new DummyVersion());

        DummyNakedObject adapter = new DummyNakedObject();
        adapter.setupObject(rootObject);
        system.addRecreatedTransient(adapter);
        
        NakedObject naked = (NakedObject) DataHelper.restore(data);
        assertEquals(rootObject, naked.getObject());
        assertEquals("transient objects have no version number", null, naked.getVersion());
        assertNull(naked.getOid());
    }

    public void testRecreateValue() {
        Data data = new DummyValueData(new Integer(11), "");
        DummyNakedValue nakedValue = new DummyNakedValue();
        system.addValue(new Integer(11), nakedValue);

        Naked naked = DataHelper.restore(data);
        assertEquals(nakedValue, naked);
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