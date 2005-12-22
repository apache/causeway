package org.nakedobjects.distribution;

import org.nakedobjects.distribution.dummy.DummyCollectionData;
import org.nakedobjects.distribution.dummy.DummyObjectData;
import org.nakedobjects.distribution.dummy.DummyObjectDataFactory;
import org.nakedobjects.distribution.dummy.DummyReferenceData;
import org.nakedobjects.distribution.dummy.DummyValueData;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.ResolveState;
import org.nakedobjects.object.Version;

import java.util.Date;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

import test.org.nakedobjects.object.DummyNakedObjectSpecification;
import test.org.nakedobjects.object.DummyNakedValue;
import test.org.nakedobjects.object.DummyOid;
import test.org.nakedobjects.object.TestSystem;
import test.org.nakedobjects.object.reflect.DummyField;
import test.org.nakedobjects.object.reflect.DummyNakedCollection;
import test.org.nakedobjects.object.reflect.DummyNakedObject;
import test.org.nakedobjects.object.reflect.DummyVersion;


public class ObjectEncoderTest extends TestCase {

    private static final boolean NOT_RESOLVED = false;
    private static final boolean RESOLVED = true;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ObjectEncoderTest.class);
    }

    private DummyNakedObjectSpecification field4Specification;
    private ObjectEncoder encoder;
    private DummyNakedObject field1;
    private DummyNakedObject field1_1;
    private DummyNakedObjectSpecification field1_1Specification;
    private DummyNakedObjectSpecification field1Specification;
    private DummyNakedObject rootObject;
    private DummyNakedObjectSpecification rootSpecification;
    private TestSystem system;
    private Date value;
    private DummyNakedValue field2;
    private NakedObjectSpecification valueSpecification;
    private DummyNakedObjectSpecification field3Specification;
    private DummyNakedCollection field3;
    private DummyNakedObject element1;
    private DummyNakedObject element2;

    /**
     * Sets up an object graph
     * 
     *      rootObject - v11
     *          |
     *          +--- referencedObjectField - v12
     *          |       |
     *          |       +-- referencedReferencedObjectField - v13
     *          |              |
     *          |              +-- root object (cyclical reference)
     *          |
     *          +--- valueField
     *          |
     *          +--- collection - v27
     *          |       |
     *          |       +-- element1 - v17
     *          |       |     |
     *          |       |     +-- root object (cyclical reference)
     *          |       |
     *          |       +-- element2 - v19
     *          |
     *          +--- null (empty)
     */
    protected void setUp() throws Exception {
        LogManager.getRootLogger().setLevel(Level.OFF);

        system = new TestSystem();
        system.init();

        encoder = new ObjectEncoder();
        encoder.setDataFactory(new DummyObjectDataFactory());

        // field 1 - reference to another object
        field1Specification = new DummyNakedObjectSpecification();
        field1Specification.fields = new NakedObjectField[1];

        field1 = new DummyNakedObject();
        field1.setupVersion(new DummyVersion(12));
        field1.setupResolveState(ResolveState.RESOLVED);
        field1.setupSpecification(field1Specification);

        field1_1Specification = new DummyNakedObjectSpecification();
        field1_1Specification.fields = new NakedObjectField[1];
        field1Specification.fields[0] = new DummyField("field5", field1Specification);

        field1_1 = new DummyNakedObject();
        field1_1.setupVersion(new DummyVersion(13));
        field1_1.setupResolveState(ResolveState.RESOLVED);
        field1_1.setupSpecification(field1_1Specification);
        field1.setupFieldValue("field5", field1_1);

        // value field
        field2 = new DummyNakedValue();
        value = new Date();
        field2.setupObject(value);
        valueSpecification = field2.getSpecification();

        // collection field - with two elements
        DummyNakedObjectSpecification element1Specification = new DummyNakedObjectSpecification();
        element1Specification.fields = new NakedObjectField[1];
        element1 = new DummyNakedObject();
        element1.setupVersion(new DummyVersion(17));
        element1.setupResolveState(ResolveState.RESOLVED);
        element1.setupSpecification(element1Specification);

        DummyNakedObjectSpecification elementSpecification = new DummyNakedObjectSpecification();
        element2 = new DummyNakedObject();
        element2.setupVersion(new DummyVersion(19));
        element2.setupResolveState(ResolveState.RESOLVED);
        element2.setupSpecification(elementSpecification);

        field3Specification = new DummyNakedObjectSpecification();
        field3Specification.setupIsCollection();
        field3 = new DummyNakedCollection();
        field3.setupResolveState(ResolveState.RESOLVED);
        field3.setupSpecification(field3Specification);
        field3.setupVersion(new DummyVersion(27));
        field3.init(new Object[] { element1, element2 });

        // empty reference field
        field4Specification = new DummyNakedObjectSpecification();
        field4Specification.fields = new NakedObjectField[0];

        // root object
        rootSpecification = new DummyNakedObjectSpecification();
        rootSpecification.fields = new NakedObjectField[4];
        rootSpecification.fields[0] = new DummyField("field1", field1Specification);
        rootSpecification.fields[1] = new DummyField("field2", valueSpecification);
        rootSpecification.fields[2] = new DummyField("field3", field3Specification);
        rootSpecification.fields[3] = new DummyField("field4", field4Specification);

        rootObject = new DummyNakedObject();
        rootObject.setupVersion(new DummyVersion(11));
        rootObject.setupResolveState(ResolveState.RESOLVED);
        rootObject.setupSpecification(rootSpecification);
        rootObject.setupFieldValue("field1", field1);
        rootObject.setupFieldValue("field2", field2);
        rootObject.setupFieldValue("field3", field3);

        // cyclic references
        DummyNakedObject field1_1_1 = rootObject;
        field1_1Specification.fields[0] = new DummyField("backref1", rootSpecification);
        field1_1.setupFieldValue("backref1", field1_1_1);

        DummyNakedObject element1_field1 = rootObject;
        element1Specification.fields[0] = new DummyField("backref2", rootSpecification);
        element1.setupFieldValue("backref2", element1_field1);
    }

    protected void tearDown() throws Exception {
        system.shutdown();
    }

    public void testCreateServerActionResultWithNull() {
        ServerActionResultData data = encoder.createServerActionResult(null, null, null, null, new String[0], new String[0]);
        assertNullData(data.getReturn());
    }

    public void testCreateServerActionResultWithCollection() {
        DummyNakedCollection collection = new DummyNakedCollection();
        DummyNakedObjectSpecification type = new DummyNakedObjectSpecification();
        collection.setupSpecification(type);
        
        ServerActionResultData data = encoder.createServerActionResult(collection, null, null, null, new String[0], new String[0]);
        assertTrue(data.getReturn() instanceof CollectionData);
        assertEquals(type.getFullName(), data.getReturn().getType());
    }

    public void testCreateServerActionResultWithObject() {
        DummyNakedObject object = new DummyNakedObject();
        object.setupResolveState(ResolveState.PART_RESOLVED);
        DummyNakedObjectSpecification type = new DummyNakedObjectSpecification();
        object.setupSpecification(type);
        
        ServerActionResultData data = encoder.createServerActionResult(object, null, null, null, new String[0], new String[0]);
        
        assertObjectData(object, NOT_RESOLVED, data.getReturn());
        
        assertTrue(data.getReturn() instanceof ObjectData);
        assertEquals(type.getFullName(), data.getReturn().getType());
    }

    public void testCreateClientActionResultWithObject() {
        ClientActionResultData data = encoder.createClientActionResult(new ObjectData[] {}, new Version[] {});
        
        assertEquals(0, data.getChanged().length);
        assertEquals(0, data.getPersisted().length);
    }


    public void testCreateCompletePersistentGraph() {
        DummyOid rootOid = new DummyOid(1);
        rootObject.setupOid(rootOid);
        DummyOid referencedOid = new DummyOid(3);
        field1.setupOid(referencedOid);
        DummyOid referencedOid2 = new DummyOid(5);
        field1_1.setupOid(referencedOid2);

        ObjectData rootData = (ObjectData) encoder.createCompletePersistentGraph(rootObject);
        assertObjectData(rootObject, RESOLVED, rootData);
        assertHasFields(4, rootData);

        ObjectData referencedFieldData = (ObjectData) rootData.getFieldContent()[0];
        assertObjectData(field1, RESOLVED, referencedFieldData);
        assertHasFields(1, referencedFieldData);

        ObjectData referencedFieldData2 = (ObjectData) referencedFieldData.getFieldContent()[0];
        assertObjectData(field1_1, RESOLVED, referencedFieldData2);
        assertHasFields(1, referencedFieldData2);

        assertValueData(field2, rootData.getFieldContent()[1]);
        assertNullData(rootData.getFieldContent()[3]);
        
        CollectionData collectionData = (CollectionData) rootData.getFieldContent()[2];
        assertEquals(2, collectionData.getElements().length);

        // check back references
        ReferenceData fieldOfField = (ReferenceData) referencedFieldData2.getFieldContent()[0];
        assertReferenceData(rootObject, fieldOfField);
        assertEquals(fieldOfField, rootData);

        ObjectData referenceData = (ObjectData) collectionData.getElements()[0];
        ReferenceData fieldOfElement = (ReferenceData) referenceData.getFieldContent()[0];
        assertReferenceData(rootObject, fieldOfElement);
    }

    public void testCreateCompletePersistentGraphWithGhosts() {
        rootObject.setupOid(new DummyOid(9));
        field1.setupOid(new DummyOid(11));
        field1.setupResolveState(ResolveState.GHOST);
        element1.setupOid(new DummyOid(13));
        element1.setupResolveState(ResolveState.GHOST);
        element2.setupOid(new DummyOid(15));
        element2.setupResolveState(ResolveState.GHOST);
        
        ObjectData rootData = (ObjectData) encoder.createCompletePersistentGraph(rootObject);

        assertObjectData(rootObject, NOT_RESOLVED, rootData);
        assertHasFields(4, rootData);
        
        assertReferenceData(field1, rootData.getFieldContent()[0]);
        assertValueData(field2, rootData.getFieldContent()[1]);
        assertNullData(rootData.getFieldContent()[3]);
        
        CollectionData collectionData = (CollectionData) rootData.getFieldContent()[2];
        assertReferenceData(element1, collectionData.getElements()[0]);
        assertReferenceData(element2, collectionData.getElements()[1]);
       
    }

    /**
     * For updates the whole object is passed across, but not any of its children.
     */
    public void testCreateForUpdate() {
        field1.setupOid(new DummyOid(3));
        element1.setupOid(new DummyOid(3));
        element2.setupOid(new DummyOid(3));
               
        ObjectData rootData = (ObjectData) encoder.createForUpdate(rootObject);
        assertEquals(ResolveState.RESOLVED, rootObject.getResolveState());

        assertObjectData(rootObject, RESOLVED, rootData);
        assertHasFields(4, rootData);
        
        assertReferenceData(field1, rootData.getFieldContent()[0]);
        assertValueData(field2, rootData.getFieldContent()[1]);
        assertNullData(rootData.getFieldContent()[3]);
        
        CollectionData collectionData = (CollectionData) rootData.getFieldContent()[2];
        assertReferenceData(element1, collectionData.getElements()[0]);
        assertReferenceData(element2, collectionData.getElements()[1]);
    }
    
       public void testCreateForChangedObject() {
        field1.setupOid(new DummyOid(3));
        element1.setupOid(new DummyOid(3));
        element2.setupOid(new DummyOid(3));
               
        ObjectData rootData = (ObjectData) encoder.createGraphForChangedObject(rootObject, ObjectEncoder.createKnownTransients());
        assertEquals(ResolveState.RESOLVED, rootObject.getResolveState());

        assertObjectData(rootObject, RESOLVED, rootData);
        assertHasFields(4, rootData);
        
        assertReferenceData(field1, rootData.getFieldContent()[0]);
        assertValueData(field2, rootData.getFieldContent()[1]);
        assertNullData(rootData.getFieldContent()[3]);
        
        CollectionData collectionData = (CollectionData) rootData.getFieldContent()[2];
        assertReferenceData(element1, collectionData.getElements()[0]);
        assertReferenceData(element2, collectionData.getElements()[1]);
    }

    public void testCreateMakePersistentGraphWhereAllReferencesArePersistent() {
        rootObject.setupResolveState(ResolveState.TRANSIENT);
        field1.setupOid(new DummyOid(194));
        element1.setupOid(new DummyOid(198));
        element2.setupOid(new DummyOid(196));

        ObjectData rootData = (ObjectData) encoder.createMakePersistentGraph(rootObject, ObjectEncoder.createKnownTransients());
        assertObjectData(rootObject, RESOLVED, rootData);

        assertReferenceData(field1, rootData.getFieldContent()[0]);
        assertValueData(field2, rootData.getFieldContent()[1]);
        assertNullData(rootData.getFieldContent()[3]);
        
        CollectionData collectionData = (CollectionData) rootData.getFieldContent()[2];
        assertReferenceData(element1, collectionData.getElements()[0]);
        assertReferenceData(element2, collectionData.getElements()[1]);
    }

    public void testCreateMakePersistentGraphWhereAllTransient() {
        rootObject.setupResolveState(ResolveState.TRANSIENT);
        field1.setupResolveState(ResolveState.TRANSIENT);
        field1_1.setupResolveState(ResolveState.TRANSIENT);
        element1.setupResolveState(ResolveState.TRANSIENT);
        element2.setupResolveState(ResolveState.TRANSIENT);

        ObjectData rootData = (ObjectData) encoder.createMakePersistentGraph(rootObject, ObjectEncoder.createKnownTransients());
        assertObjectData(rootObject, RESOLVED, rootData);
        assertHasFields(4, rootData);

        ObjectData referencedFieldData = (ObjectData) rootData.getFieldContent()[0];
        assertObjectData(field1, RESOLVED, referencedFieldData);
        assertHasFields(1, referencedFieldData);

        ObjectData referencedFieldData2 = (ObjectData) referencedFieldData.getFieldContent()[0];
        assertObjectData(field1_1, RESOLVED, referencedFieldData2);
        assertHasFields(1, referencedFieldData2);

        assertValueData(field2, rootData.getFieldContent()[1]);
        
        assertNullData(rootData.getFieldContent()[3]);
        
        CollectionData collectionData = (CollectionData) rootData.getFieldContent()[2];
        assertEquals(2, collectionData.getElements().length);
        
        assertObjectData(element1, RESOLVED, collectionData.getElements()[0]);
        assertObjectData(element2, RESOLVED, collectionData.getElements()[1]);

        // check back references
        ObjectData fieldOfField = (ObjectData) referencedFieldData2.getFieldContent()[0];
        assertSame(fieldOfField, rootData);

        ObjectData referenceData = (ObjectData) collectionData.getElements()[0];
        ObjectData fieldOfElement = (ObjectData) referenceData.getFieldContent()[0];
        assertSame(fieldOfElement, rootData);
    }

    public void testCreateMadePersistentGraphWhereAllTransient() {
        // complete setup
        DummyObjectData reference2 = new DummyObjectData(null, "ref2", false, null);
        field1_1.setupResolveState(ResolveState.TRANSIENT);
        field1_1.setupOid(new DummyOid(192));

        DummyValueData value = new DummyValueData(null, "val");

        DummyObjectData el1 = new DummyObjectData(null, "element1", false, null);
        element1.setupResolveState(ResolveState.TRANSIENT);
        element1.setupOid(new DummyOid(34));

        DummyObjectData el2 = new DummyObjectData(null, "element2", false, null);
        element2.setupResolveState(ResolveState.TRANSIENT);
        element2.setupOid(new DummyOid(29));

        DummyObjectData reference1 = new DummyObjectData(null, "ref1", false, null);
        reference1.setFieldContent(new Data[] { reference2 });
        field1.setupResolveState(ResolveState.TRANSIENT);
        field1.setupFields(field1Specification.getFields());
        field1.setupOid(new DummyOid(583));

        DummyCollectionData coll = new DummyCollectionData(null, "coll", new ObjectData[] { el1, el2 }, null);

        ObjectData rootData = new DummyObjectData(null, "root", false, null);
        rootData.setFieldContent(new Data[] { reference1, value, coll, null });
        rootObject.setupResolveState(ResolveState.TRANSIENT);
        rootObject.setupFields(rootSpecification.getFields());
        rootObject.setupOid(new DummyOid(712));

        reference2.setFieldContent(new Data[] { rootData });

        el1.setFieldContent(new Data[] { rootData });

        // test
        ObjectData transientGraph = encoder.createMadePersistentGraph(rootData, rootObject, new SingleResponseUpdateNotifier());
        assertEquals(new DummyOid(712), transientGraph.getOid());
        assertEquals(new DummyVersion(11), transientGraph.getVersion());
        assertEquals(4, transientGraph.getFieldContent().length);

        // check root's 3 fields
        ObjectData field1 = (ObjectData) transientGraph.getFieldContent()[0];
        assertEquals("ref1", field1.getType());
        assertEquals(new DummyOid(583), field1.getOid());
        assertEquals(new DummyVersion(12), field1.getVersion());
        assertEquals(1, field1.getFieldContent().length);

        Data field2 = transientGraph.getFieldContent()[1];
        assertEquals(null, field2);

        CollectionData field3 = (CollectionData) transientGraph.getFieldContent()[2];
        assertNotNull(field3);
        assertEquals("coll", field3.getType());
        assertEquals(null, field3.getOid());
        assertEquals(new DummyVersion(27), field3.getVersion());
        assertEquals(2, field3.getElements().length);

        Data field4 = transientGraph.getFieldContent()[3];
        assertEquals(null, field4);

        // check the 3rd level object
        ObjectData field = (ObjectData) field1.getFieldContent()[0];
        assertEquals("ref2", field.getType());
        assertEquals(new DummyOid(192), field.getOid());
        assertEquals(new DummyVersion(13), field.getVersion());

        // check elements of collection
        ObjectData element1 = (ObjectData) field3.getElements()[0];
        assertEquals("element1", element1.getType());
        assertEquals(new DummyOid(34), element1.getOid());
        assertEquals(new DummyVersion(17), element1.getVersion());

        ObjectData element2 = (ObjectData) field3.getElements()[1];
        assertEquals("element2", element2.getType());
        assertEquals(new DummyOid(29), element2.getOid());
        assertEquals(new DummyVersion(19), element2.getVersion());

    }

    public void testCreateMadePersistentGraphWhereSomePersistent() {
        // complete setup
        DummyObjectData reference2 = new DummyObjectData(null, "ref2", false, null);
        field1_1.setupResolveState(ResolveState.TRANSIENT);
        field1_1.setupOid(new DummyOid(192));

        DummyValueData value = new DummyValueData(null, "val");

        DummyObjectData el1 = new DummyObjectData(new DummyOid(34), "element1", false, null);
        element1.setupResolveState(ResolveState.TRANSIENT);
        element1.setupOid(new DummyOid(34));

        DummyObjectData el2 = new DummyObjectData(null, "element2", false, null);
        element2.setupResolveState(ResolveState.TRANSIENT);
        element2.setupOid(new DummyOid(29));

        DummyObjectData reference1 = new DummyObjectData(new DummyOid(583), "ref1", false, null);
        reference1.setFieldContent(new Data[] { reference2 });
        field1.setupResolveState(ResolveState.RESOLVED);
        field1.setupFields(field1Specification.getFields());
        field1.setupOid(new DummyOid(583));

        DummyCollectionData coll = new DummyCollectionData(null, "coll", new ObjectData[] { el1, el2 }, null);

        ObjectData data = new DummyObjectData(null, "root", false, null);
        data.setFieldContent(new Data[] { reference1, value, coll, null });
        rootObject.setupResolveState(ResolveState.TRANSIENT);
        rootObject.setupFields(rootSpecification.getFields());
        rootObject.setupOid(new DummyOid(712));

        // test
        ObjectData updates = encoder.createMadePersistentGraph(data, rootObject, new SingleResponseUpdateNotifier());
        assertEquals(new DummyOid(712), updates.getOid());
        assertEquals(new DummyVersion(11), updates.getVersion());
        assertEquals(4, updates.getFieldContent().length);

        // check root's 3 fields
        ObjectData field1 = (ObjectData) updates.getFieldContent()[0];
        assertEquals("first field was persistent, no data sent back", null, field1);

        Data field2 = updates.getFieldContent()[1];
        assertEquals(null, field2);

        CollectionData field3 = (CollectionData) updates.getFieldContent()[2];
        assertNotNull(field3);
        assertEquals("coll", field3.getType());
        assertEquals(null, field3.getOid());
        assertEquals(new DummyVersion(27), field3.getVersion());
        assertEquals(2, field3.getElements().length);

        Data field4 = updates.getFieldContent()[3];
        assertEquals(null, field4);

        // check elements of collection
        ObjectData element1 = (ObjectData) field3.getElements()[0];
        assertEquals("first element was persistent, no data sent back", null, element1);

        ObjectData element2 = (ObjectData) field3.getElements()[1];
        assertEquals("element2", element2.getType());
        assertEquals(new DummyOid(29), element2.getOid());
        assertEquals(new DummyVersion(19), element2.getVersion());
    }

    public void testPersistentObjectParameter() {
        assertEquals(ResolveState.RESOLVED, rootObject.getResolveState());

        DummyOid rootOid = new DummyOid(1);
        rootObject.setupOid(rootOid);

        Data[] results = encoder.createParameters(new NakedObjectSpecification[] { new DummyNakedObjectSpecification(), new DummyNakedObjectSpecification() },
                new Naked[] { rootObject, null });
        
        assertEquals(2, results.length);
        assertReferenceData(rootObject, results[0]);
        assertNullData(results[1]);
    }

    public void testResolvedCreatesObjectData() {
        rootObject.setupResolveState(ResolveState.RESOLVED);
        rootObject.setupOid(new DummyOid(2));
        
        Data od = encoder.createActionTarget(rootObject);
        assertReferenceData(rootObject, od);
    }

    private void assertReferenceData(DummyNakedObject rootObject, Data od) {
        ReferenceData data = (ReferenceData) od;
        assertFalse("must be data for a reference, not an object", od instanceof ObjectData);
        assertEquals(rootObject.getOid(), data.getOid());
        assertEquals(rootObject.getVersion(), data.getVersion());
        assertEquals(rootObject.getSpecification().getFullName(), data.getType());

    }

    public void testGhostsCreateReferencetData() {
        rootObject.setupResolveState(ResolveState.GHOST);
        rootObject.setupOid(new DummyOid(2));

        ReferenceData rd = encoder.createActionTarget(rootObject);
        assertEquals(ResolveState.GHOST, rootObject.getResolveState());
        assertEquals(new DummyOid(2), rd.getOid());
        assertEquals(DummyReferenceData.class, rd.getClass());
    }

    public void testTransientObjectParameter() {
        rootObject.setupResolveState(ResolveState.TRANSIENT);
        field1.setupResolveState(ResolveState.TRANSIENT);
        assertEquals(ResolveState.TRANSIENT, rootObject.getResolveState());

        field1_1.setupOid(new DummyOid(123));
        element1.setupOid(new DummyOid(125));
        element2.setupOid(new DummyOid(127));
        
        Data[] data = encoder.createParameters(new NakedObjectSpecification[] { new DummyNakedObjectSpecification() },
                new Naked[] { rootObject });

        
        ObjectData rootData = (ObjectData) data[0];
        assertObjectData(rootObject, RESOLVED, rootData);
        assertHasFields(4, rootData);
        
        ObjectData referencedFieldData = (ObjectData) rootData.getFieldContent()[0];
        assertObjectData(field1, RESOLVED, referencedFieldData);
        assertHasFields(1, referencedFieldData);
       
        assertReferenceData(field1_1, referencedFieldData.getFieldContent()[0]);

        assertValueData(field2, rootData.getFieldContent()[1]);

        NullData emptyFieldData = (NullData) rootData.getFieldContent()[3];
        assertNullData(emptyFieldData);
    }

    private void assertNullData(Data ref) {
        assertTrue("instance of NullData", ref instanceof NullData);
    }

    private void assertValueData(DummyNakedValue value, Data data) {
        ValueData valueData = (ValueData) data;
        assertEquals(value.getSpecification().getFullName(), valueData.getType());
        assertEquals(value.getObject(), valueData.getValue());

    }

    private void assertHasFields(int noOfFields, ObjectData rootData) {
        assertEquals(noOfFields, rootData.getFieldContent().length);
    }

    private void assertObjectData(DummyNakedObject rootObject2, boolean b, Data data) {
        assertTrue("must be data for an object", data instanceof ObjectData);
        ObjectData objectData = (ObjectData) data;
        assertEquals(rootObject2.getOid(), objectData.getOid());
        assertEquals(rootObject2.getSpecification().getFullName(), objectData.getType());
        assertEquals(rootObject2.getVersion(), objectData.getVersion());
    }

    public void testValueParameter() {
        DummyNakedValue dummyNakedValue = new DummyNakedValue();
        dummyNakedValue.setupObject(new Integer(123));

        Data[] data = (Data[]) encoder.createParameters(
                new NakedObjectSpecification[] { new DummyNakedObjectSpecification() }, new Naked[] { dummyNakedValue });

        DummyValueData expected = new DummyValueData(new Integer(123), dummyNakedValue.getSpecification().getFullName());
        assertEquals(expected, data[0]);
    }
    
    public void testForResovleField() {
        field1.setupOid(new DummyOid(7));
        field1_1.setupOid(new DummyOid(11));
        rootObject.setupOid(new DummyOid(13));
        
        encoder.createForResolveField(rootObject, "field1");
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is
 * Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */