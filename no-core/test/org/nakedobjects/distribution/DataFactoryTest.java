package org.nakedobjects.distribution;

import org.nakedobjects.TestSystem;
import org.nakedobjects.distribution.dummy.DummyObjectDataFactory;
import org.nakedobjects.object.DummyNakedObjectSpecification;
import org.nakedobjects.object.DummyNakedValue;
import org.nakedobjects.object.DummyObjectLoader;
import org.nakedobjects.object.MockOid;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.ResolveState;
import org.nakedobjects.object.reflect.DummyField;
import org.nakedobjects.object.reflect.DummyInternalCollection;
import org.nakedobjects.object.reflect.DummyNakedObject;
import org.nakedobjects.object.reflect.NakedObjectField;

import java.util.Date;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;


public class DataFactoryTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(DataFactoryTest.class);
    }

    private DummyNakedObjectSpecification emtpyFieldSpecification;
    private DummyObjectDataFactory factory;
    private DummyObjectLoader objectLoader;
    private DummyNakedObject referencedObjectField;
    private DummyNakedObject referencedReferencedObjectField;
    private DummyNakedObjectSpecification referencedReferencedSpecification;
    private DummyNakedObjectSpecification referencedSpecification;
    private DummyNakedObject rootObject;
    private DummyNakedObjectSpecification rootSpecification;
    private TestSystem system;
    private Date value;
    private DummyNakedValue valueField;
    private NakedObjectSpecification valueSpecification;

    protected void setUp() throws Exception {
        LogManager.getRootLogger().setLevel(Level.OFF);

        system = new TestSystem();
        objectLoader = new DummyObjectLoader();
        system.setObjectLoader(objectLoader);
        system.init();

        factory = new DummyObjectDataFactory();

        rootSpecification = new DummyNakedObjectSpecification();
        rootSpecification.fields = new NakedObjectField[3];

        rootObject = new DummyNakedObject();
        rootObject.setupVersion(11);
        rootObject.setupResolveState(ResolveState.RESOLVED);
        rootObject.setupSpecification(rootSpecification);

        referencedSpecification = new DummyNakedObjectSpecification();
        referencedSpecification.fields = new NakedObjectField[1];
        rootSpecification.fields[0] = new DummyField("one", referencedSpecification);

        referencedObjectField = new DummyNakedObject();
        referencedObjectField.setupVersion(12);
        referencedObjectField.setupResolveState(ResolveState.RESOLVED);
        referencedObjectField.setupSpecification(referencedSpecification);
        rootObject.setupFieldValue("one", referencedObjectField);

        valueField = new DummyNakedValue();
        value = new Date();
        valueField.setupObject(value);
        valueSpecification = valueField.getSpecification();
        rootSpecification.fields[1] = new DummyField("two", valueSpecification);
        rootObject.setupFieldValue("two", valueField);

        emtpyFieldSpecification = new DummyNakedObjectSpecification();
        emtpyFieldSpecification.fields = new NakedObjectField[0];
        rootSpecification.fields[2] = new DummyField("three", emtpyFieldSpecification);

        referencedReferencedSpecification = new DummyNakedObjectSpecification();
        referencedReferencedSpecification.fields = new NakedObjectField[0];
        referencedSpecification.fields[0] = new DummyField("four", referencedSpecification);

        referencedReferencedObjectField = new DummyNakedObject();
        referencedReferencedObjectField.setupVersion(13);
        referencedReferencedObjectField.setupResolveState(ResolveState.RESOLVED);
        referencedReferencedObjectField.setupSpecification(referencedReferencedSpecification);
        referencedObjectField.setupFieldValue("four", referencedReferencedObjectField);

    }

    protected void tearDown() throws Exception {
        system.shutdown();
    }

    public void testCreateActionResultWithNull() {
        Data data = factory.createActionResult(null);
        assertTrue(data instanceof NullData);
    }

    public void testCreateActionResultWithCollection() {
        DummyInternalCollection collection = new DummyInternalCollection();
        DummyNakedObjectSpecification type = new DummyNakedObjectSpecification();
        collection.setupSpecification(type);
        Data data = factory.createActionResult(collection);
        assertTrue(data instanceof CollectionData);
        assertEquals(type.getFullName(), data.getType());
   }

    public void testCreateActionResultWithObject() {
        DummyNakedObject object = new DummyNakedObject();
        object.setupResolveState(ResolveState.PART_RESOLVED);
        DummyNakedObjectSpecification type = new DummyNakedObjectSpecification();
        object.setupSpecification(type);
        Data data = factory.createActionResult(object);
        assertTrue(data instanceof ObjectData);
        assertEquals(type.getFullName(), data.getType());
    }

    public void testCreateCompletePersistentGraph() {
        MockOid rootOid = new MockOid(1);
        rootObject.setupOid(rootOid);

        MockOid referencedOid = new MockOid(1);
        referencedObjectField.setupOid(referencedOid);

        MockOid referencedOid2 = new MockOid(1);
        referencedReferencedObjectField.setupOid(referencedOid2);

        ObjectData rootData = (ObjectData) factory.createCompletePersistentGraph(rootObject);
        assertEquals(ResolveState.SERIALIZING_RESOLVED, rootObject.getResolveState());

        assertEquals(rootOid, rootData.getOid());
        assertEquals(rootSpecification.getFullName(), rootData.getType());
        assertEquals(11, rootData.getVersion());
        assertEquals(3, rootData.getFieldContent().length);

        ObjectData referencedFieldData = (ObjectData) rootData.getFieldContent()[0];
        assertEquals(referencedOid, referencedFieldData.getOid());
        assertEquals(1, referencedFieldData.getFieldContent().length);
        assertEquals(12, referencedFieldData.getVersion());
        assertEquals(referencedSpecification.getFullName(), referencedFieldData.getType());

        ObjectData referencedFieldData2 = (ObjectData) referencedFieldData.getFieldContent()[0];
        assertEquals(referencedOid2, referencedFieldData2.getOid());
        assertEquals(0, referencedFieldData2.getFieldContent().length);
        assertEquals(13, referencedFieldData2.getVersion());
        assertEquals(referencedSpecification.getFullName(), referencedFieldData.getType());

        ValueData valueData = (ValueData) rootData.getFieldContent()[1];
        assertEquals(valueSpecification.getFullName(), valueData.getType());
        assertEquals(value, valueData.getValue());

        NullData emptyFieldData = (NullData) rootData.getFieldContent()[2];
        assertNotNull(emptyFieldData);

    }

    public void testCreateCompletePersistentGraphWithGhosts() {
        MockOid rootOid = new MockOid(1);
        rootObject.setupOid(rootOid);

        MockOid referencedOid = new MockOid(1);
        referencedObjectField.setupOid(referencedOid);

        referencedObjectField.setupResolveState(ResolveState.GHOST);

        ObjectData rootData = (ObjectData) factory.createCompletePersistentGraph(rootObject);
        assertEquals(ResolveState.SERIALIZING_RESOLVED, rootObject.getResolveState());

        assertEquals(rootOid, rootData.getOid());
        assertEquals(rootSpecification.getFullName(), rootData.getType());
        assertEquals(11, rootData.getVersion());
        assertEquals(3, rootData.getFieldContent().length);

        ObjectData referencedFieldData = (ObjectData) rootData.getFieldContent()[0];
        assertEquals(referencedOid, referencedFieldData.getOid());
        assertEquals(null, referencedFieldData.getFieldContent());
        assertEquals(12, referencedFieldData.getVersion());
        assertEquals(referencedSpecification.getFullName(), referencedFieldData.getType());

        ValueData valueData = (ValueData) rootData.getFieldContent()[1];
        assertEquals(valueSpecification.getFullName(), valueData.getType());
        assertEquals(value, valueData.getValue());

        NullData emptyFieldData = (NullData) rootData.getFieldContent()[2];
        assertNotNull(emptyFieldData);
    }

    /**
     * For updates the whole object is passed across, but not any of its children.
     */
    public void testCreateForUpdate() {
        ObjectData rootData = (ObjectData) factory.createForUpdate(rootObject);
        assertEquals(ResolveState.SERIALIZING_RESOLVED, rootObject.getResolveState());

        assertEquals(null, rootData.getOid());
        assertEquals(rootSpecification.getFullName(), rootData.getType());
        assertEquals(11, rootData.getVersion());
        assertEquals(3, rootData.getFieldContent().length);

        ObjectData referencedFieldData = (ObjectData) rootData.getFieldContent()[0];
        assertEquals(null, referencedFieldData.getFieldContent());
        assertEquals(12, referencedFieldData.getVersion());
        assertEquals(referencedSpecification.getFullName(), referencedFieldData.getType());

        ValueData valueData = (ValueData) rootData.getFieldContent()[1];
        assertEquals(valueSpecification.getFullName(), valueData.getType());
        assertEquals(value, valueData.getValue());

        NullData emptyFieldData = (NullData) rootData.getFieldContent()[2];
        assertNotNull(emptyFieldData);

    }

    public void testCreateMakePersistentGraphWereAllReferencesArePersistent() {
        rootObject.setupResolveState(ResolveState.TRANSIENT);

        ObjectData rootData = (ObjectData) factory.createMakePersistentGraph(rootObject);
        assertEquals(ResolveState.SERIALIZING_TRANSIENT, rootObject.getResolveState());

        assertEquals(null, rootData.getOid());
        assertEquals(rootSpecification.getFullName(), rootData.getType());
        assertEquals(11, rootData.getVersion());
        assertEquals(3, rootData.getFieldContent().length);

        ObjectData referencedFieldData = (ObjectData) rootData.getFieldContent()[0];
        assertEquals(null, referencedFieldData.getFieldContent());
        assertEquals(12, referencedFieldData.getVersion());
        assertEquals(referencedSpecification.getFullName(), referencedFieldData.getType());

        ValueData valueData = (ValueData) rootData.getFieldContent()[1];
        assertEquals(valueSpecification.getFullName(), valueData.getType());
        assertEquals(value, valueData.getValue());

        NullData emptyFieldData = (NullData) rootData.getFieldContent()[2];
        assertNotNull(emptyFieldData);
    }

    public void testCreateMakePersistentGraphWereAllTransient() {
        rootObject.setupResolveState(ResolveState.TRANSIENT);
        referencedObjectField.setupResolveState(ResolveState.TRANSIENT);
        referencedReferencedObjectField.setupResolveState(ResolveState.TRANSIENT);

        ObjectData rootData = (ObjectData) factory.createMakePersistentGraph(rootObject);
        assertEquals(ResolveState.SERIALIZING_TRANSIENT, rootObject.getResolveState());

        assertEquals(null, rootData.getOid());
        assertEquals(rootSpecification.getFullName(), rootData.getType());
        assertEquals(11, rootData.getVersion());
        assertEquals(3, rootData.getFieldContent().length);

        ObjectData referencedFieldData = (ObjectData) rootData.getFieldContent()[0];
        assertEquals(1, referencedFieldData.getFieldContent().length);
        assertEquals(12, referencedFieldData.getVersion());
        assertEquals(referencedSpecification.getFullName(), referencedFieldData.getType());

        ObjectData referencedFieldData2 = (ObjectData) referencedFieldData.getFieldContent()[0];
        assertEquals(0, referencedFieldData2.getFieldContent().length);
        assertEquals(13, referencedFieldData2.getVersion());
        assertEquals(referencedSpecification.getFullName(), referencedFieldData.getType());

        ValueData valueData = (ValueData) rootData.getFieldContent()[1];
        assertEquals(valueSpecification.getFullName(), valueData.getType());
        assertEquals(value, valueData.getValue());

        NullData emptyFieldData = (NullData) rootData.getFieldContent()[2];
        assertNotNull(emptyFieldData);
    }

    public void testPersistentObjectParameter() {
        assertEquals(ResolveState.RESOLVED, rootObject.getResolveState());

        MockOid rootOid = new MockOid(1);
        rootObject.setupOid(rootOid);

        ObjectData rootData = (ObjectData) factory.createDataForParameter("", rootObject);
        assertEquals(ResolveState.RESOLVED, rootObject.getResolveState());

        assertEquals(rootOid, rootData.getOid());
        assertEquals(rootSpecification.getFullName(), rootData.getType());
        assertEquals(11, rootData.getVersion());
        assertNull(rootData.getFieldContent());

    }

    public void testResolveStateIsMirroredInObjectData() {
        rootObject.setupResolveState(ResolveState.GHOST);
        assertEquals(ResolveState.GHOST, rootObject.getResolveState());

        ObjectData od = factory.createObjectData(rootObject, false, 10);

        assertEquals(false, od.hasCompleteData());
        assertEquals(ResolveState.GHOST, rootObject.getResolveState());

        rootObject.setupResolveState(ResolveState.RESOLVED);
        assertEquals(ResolveState.RESOLVED, rootObject.getResolveState());

        od = factory.createObjectData(rootObject, false, 10);

        assertEquals(true, od.hasCompleteData());
        assertEquals(ResolveState.RESOLVED, rootObject.getResolveState());
    }

    public void testTransientObjectParameter() {
        rootObject.setupResolveState(ResolveState.TRANSIENT);
        referencedObjectField.setupResolveState(ResolveState.TRANSIENT);

        ObjectData rootData = (ObjectData) factory.createDataForParameter("", rootObject);
        assertEquals(ResolveState.SERIALIZING_TRANSIENT, rootObject.getResolveState());

        assertEquals(null, rootData.getOid());
        assertEquals(rootSpecification.getFullName(), rootData.getType());
        assertEquals(11, rootData.getVersion());
        assertEquals(3, rootData.getFieldContent().length);

        ObjectData referencedFieldData = (ObjectData) rootData.getFieldContent()[0];
        assertEquals(1, referencedFieldData.getFieldContent().length);
        assertEquals(12, referencedFieldData.getVersion());
        assertEquals(referencedSpecification.getFullName(), referencedFieldData.getType());

        ObjectData referencedFieldData2 = (ObjectData) referencedFieldData.getFieldContent()[0];
        assertEquals(null, referencedFieldData2.getFieldContent());
        assertEquals(13, referencedFieldData2.getVersion());
        assertEquals(referencedSpecification.getFullName(), referencedFieldData.getType());

        ValueData valueData = (ValueData) rootData.getFieldContent()[1];
        assertEquals(valueSpecification.getFullName(), valueData.getType());
        assertEquals(value, valueData.getValue());

        NullData emptyFieldData = (NullData) rootData.getFieldContent()[2];
        assertNotNull(emptyFieldData);
    }

    public void testValueParameter() {
        DummyNakedValue dummyNakedValue = new DummyNakedValue();
        dummyNakedValue.setupObject(new Integer(123));

        ValueData data = (ValueData) factory.createDataForParameter("", dummyNakedValue);

        assertEquals(dummyNakedValue.getSpecification().getFullName(), data.getType());
        assertEquals(new Integer(123), data.getValue());
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