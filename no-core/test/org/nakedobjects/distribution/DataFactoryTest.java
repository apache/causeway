package org.nakedobjects.distribution;

import org.nakedobjects.TestSystem;
import org.nakedobjects.distribution.dummy.DummyCollectionData;
import org.nakedobjects.distribution.dummy.DummyObjectData;
import org.nakedobjects.distribution.dummy.DummyObjectDataFactory;
import org.nakedobjects.distribution.dummy.DummyValueData;
import org.nakedobjects.object.DummyNakedObjectSpecification;
import org.nakedobjects.object.DummyNakedValue;
import org.nakedobjects.object.DummyOid;
import org.nakedobjects.object.DummyVersion;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.ResolveState;
import org.nakedobjects.object.reflect.DummyField;
import org.nakedobjects.object.reflect.DummyNakedCollection;
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

    private DummyNakedObjectSpecification field4Specification;
    private DummyObjectDataFactory factory;
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
     *          |
     *          +--- valueField
     *          |
     *          +--- collection - v27
     *          |       |
     *          |       +-- element1 - v17
     *          |       |
     *          |       +-- element2 - v19
     *          |
     *          +--- null (empty)
     */
    protected void setUp() throws Exception {
        LogManager.getRootLogger().setLevel(Level.OFF);

        system = new TestSystem();
        system.init();

        factory = new DummyObjectDataFactory();

        // field 1 - reference to another object
        field1Specification = new DummyNakedObjectSpecification();
        field1Specification.fields = new NakedObjectField[1];

        field1 = new DummyNakedObject();
        field1.setupVersion(new DummyVersion(12));
        field1.setupResolveState(ResolveState.RESOLVED);
        field1.setupSpecification(field1Specification);

        field1_1Specification = new DummyNakedObjectSpecification();
        field1_1Specification.fields = new NakedObjectField[0];
        field1Specification.fields[0] = new DummyField("four", field1Specification);

        field1_1 = new DummyNakedObject();
        field1_1.setupVersion(new DummyVersion(13));
        field1_1.setupResolveState(ResolveState.RESOLVED);
        field1_1.setupSpecification(field1_1Specification);
        field1.setupFieldValue("four", field1_1);

        
        // value field
        field2 = new DummyNakedValue();
        value = new Date();
        field2.setupObject(value);
        valueSpecification = field2.getSpecification();

        
        // collection field - with two elements
        DummyNakedObjectSpecification elementSpecification = new DummyNakedObjectSpecification();
        element1 = new DummyNakedObject();
        element1.setupVersion(new DummyVersion(17));
        element1.setupResolveState(ResolveState.RESOLVED);
        element1.setupSpecification(elementSpecification);
        
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
        field3.init(new Object[] {element1, element2});
        
        // empty reference field
        field4Specification = new DummyNakedObjectSpecification();
        field4Specification.fields = new NakedObjectField[0];

        
        // root object
        rootSpecification = new DummyNakedObjectSpecification();
        rootSpecification.fields = new NakedObjectField[4];
        rootSpecification.fields[0] = new DummyField("one", field1Specification);
        rootSpecification.fields[1] = new DummyField("two", valueSpecification);
        rootSpecification.fields[2] = new DummyField("collection", field3Specification);
        rootSpecification.fields[3] = new DummyField("three", field4Specification);

        rootObject = new DummyNakedObject();
        rootObject.setupVersion(new DummyVersion(11));
        rootObject.setupResolveState(ResolveState.RESOLVED);
        rootObject.setupSpecification(rootSpecification);
        rootObject.setupFieldValue("one", field1);
        rootObject.setupFieldValue("two", field2);
        rootObject.setupFieldValue("collection", field3);


    }

    protected void tearDown() throws Exception {
        system.shutdown();
    }

    public void testCreateActionResultWithNull() {
        Data data = factory.createActionResult(null);
        assertTrue(data instanceof NullData);
    }

    public void testCreateActionResultWithCollection() {
        DummyNakedCollection collection = new DummyNakedCollection();
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
        DummyOid rootOid = new DummyOid(1);
        rootObject.setupOid(rootOid);

        DummyOid referencedOid = new DummyOid(1);
        field1.setupOid(referencedOid);

        DummyOid referencedOid2 = new DummyOid(1);
        field1_1.setupOid(referencedOid2);

        ObjectData rootData = (ObjectData) factory.createCompletePersistentGraph(rootObject);
        assertEquals(ResolveState.RESOLVED, rootObject.getResolveState());

        assertEquals(rootOid, rootData.getOid());
        assertEquals(rootSpecification.getFullName(), rootData.getType());
        assertEquals(new DummyVersion(11), rootData.getVersion());
        assertEquals(4, rootData.getFieldContent().length);

        ObjectData referencedFieldData = (ObjectData) rootData.getFieldContent()[0];
        assertEquals(referencedOid, referencedFieldData.getOid());
        assertEquals(1, referencedFieldData.getFieldContent().length);
        assertEquals(new DummyVersion(12), referencedFieldData.getVersion());
        assertEquals(field1Specification.getFullName(), referencedFieldData.getType());

        ObjectData referencedFieldData2 = (ObjectData) referencedFieldData.getFieldContent()[0];
        assertEquals(referencedOid2, referencedFieldData2.getOid());
        assertEquals(0, referencedFieldData2.getFieldContent().length);
        assertEquals(new DummyVersion(13), referencedFieldData2.getVersion());
        assertEquals(field1Specification.getFullName(), referencedFieldData.getType());

        ValueData valueData = (ValueData) rootData.getFieldContent()[1];
        assertEquals(valueSpecification.getFullName(), valueData.getType());
        assertEquals(value, valueData.getValue());

        NullData emptyFieldData = (NullData) rootData.getFieldContent()[3];
        assertNotNull(emptyFieldData);

    }

    public void testCreateCompletePersistentGraphWithGhosts() {
        DummyOid rootOid = new DummyOid(1);
        rootObject.setupOid(rootOid);

        DummyOid referencedOid = new DummyOid(1);
        field1.setupOid(referencedOid);

        field1.setupResolveState(ResolveState.GHOST);

        ObjectData rootData = (ObjectData) factory.createCompletePersistentGraph(rootObject);
        assertEquals(ResolveState.RESOLVED, rootObject.getResolveState());

        assertEquals(rootOid, rootData.getOid());
        assertEquals(rootSpecification.getFullName(), rootData.getType());
        assertEquals(new DummyVersion(11), rootData.getVersion());
        assertEquals(4, rootData.getFieldContent().length);

        ObjectData referencedFieldData = (ObjectData) rootData.getFieldContent()[0];
        assertEquals(referencedOid, referencedFieldData.getOid());
        assertEquals(null, referencedFieldData.getFieldContent());
        assertEquals(new DummyVersion(12), referencedFieldData.getVersion());
        assertEquals(field1Specification.getFullName(), referencedFieldData.getType());

        ValueData valueData = (ValueData) rootData.getFieldContent()[1];
        assertEquals(valueSpecification.getFullName(), valueData.getType());
        assertEquals(value, valueData.getValue());

        NullData emptyFieldData = (NullData) rootData.getFieldContent()[3];
        assertNotNull(emptyFieldData);
    }

    /**
     * For updates the whole object is passed across, but not any of its children.
     */
    public void testCreateForUpdate() {
        ObjectData rootData = (ObjectData) factory.createForUpdate(rootObject);
        assertEquals(ResolveState.RESOLVED, rootObject.getResolveState());

        assertEquals(null, rootData.getOid());
        assertEquals(rootSpecification.getFullName(), rootData.getType());
        assertEquals(new DummyVersion(11), rootData.getVersion());
        assertEquals(4, rootData.getFieldContent().length);

        ObjectData referencedFieldData = (ObjectData) rootData.getFieldContent()[0];
        assertEquals(null, referencedFieldData.getFieldContent());
        assertEquals(new DummyVersion(12), referencedFieldData.getVersion());
        assertEquals(field1Specification.getFullName(), referencedFieldData.getType());

        ValueData valueData = (ValueData) rootData.getFieldContent()[1];
        assertEquals(valueSpecification.getFullName(), valueData.getType());
        assertEquals(value, valueData.getValue());

        NullData emptyFieldData = (NullData) rootData.getFieldContent()[3];
        assertNotNull(emptyFieldData);

    }

    public void testCreateMakePersistentGraphWereAllReferencesArePersistent() {
        rootObject.setupResolveState(ResolveState.TRANSIENT);

        ObjectData rootData = (ObjectData) factory.createMakePersistentGraph(rootObject);
        assertEquals(ResolveState.TRANSIENT, rootObject.getResolveState());

        assertEquals(null, rootData.getOid());
        assertEquals(rootSpecification.getFullName(), rootData.getType());
        assertEquals(new DummyVersion(11), rootData.getVersion());
        assertEquals(4, rootData.getFieldContent().length);

        ObjectData referencedFieldData = (ObjectData) rootData.getFieldContent()[0];
        assertEquals(null, referencedFieldData.getFieldContent());
        assertEquals(new DummyVersion(12), referencedFieldData.getVersion());
        assertEquals(field1Specification.getFullName(), referencedFieldData.getType());

        ValueData valueData = (ValueData) rootData.getFieldContent()[1];
        assertEquals(valueSpecification.getFullName(), valueData.getType());
        assertEquals(value, valueData.getValue());

        NullData emptyFieldData = (NullData) rootData.getFieldContent()[3];
        assertNotNull(emptyFieldData);
    }

    public void testCreateMakePersistentGraphWereAllTransient() {
        rootObject.setupResolveState(ResolveState.TRANSIENT);
        field1.setupResolveState(ResolveState.TRANSIENT);
        field1_1.setupResolveState(ResolveState.TRANSIENT);

        ObjectData rootData = (ObjectData) factory.createMakePersistentGraph(rootObject);
        assertEquals(ResolveState.TRANSIENT, rootObject.getResolveState());

        assertEquals(null, rootData.getOid());
        assertEquals(rootSpecification.getFullName(), rootData.getType());
        assertEquals(new DummyVersion(11), rootData.getVersion());
        assertEquals(4, rootData.getFieldContent().length);

        ObjectData referencedFieldData = (ObjectData) rootData.getFieldContent()[0];
        assertEquals(1, referencedFieldData.getFieldContent().length);
        assertEquals(new DummyVersion(12), referencedFieldData.getVersion());
        assertEquals(field1Specification.getFullName(), referencedFieldData.getType());

        ObjectData referencedFieldData2 = (ObjectData) referencedFieldData.getFieldContent()[0];
        assertEquals(0, referencedFieldData2.getFieldContent().length);
        assertEquals(new DummyVersion(13), referencedFieldData2.getVersion());
        assertEquals(field1Specification.getFullName(), referencedFieldData.getType());

        ValueData valueData = (ValueData) rootData.getFieldContent()[1];
        assertEquals(valueSpecification.getFullName(), valueData.getType());
        assertEquals(value, valueData.getValue());

        NullData emptyFieldData = (NullData) rootData.getFieldContent()[3];
        assertNotNull(emptyFieldData);
    }
    


    public void testCreateMadePersistentGraphWereAllTransient() {
        // complete setup
        DummyObjectData reference2 = new DummyObjectData(null, "ref2", null, false, null);
        field1_1.setupResolveState(ResolveState.TRANSIENT);
        field1_1.setupOid(new DummyOid(192));
    
        DummyValueData value = new DummyValueData(null, "val");
    
        DummyObjectData el1 = new DummyObjectData(null, "element1", null, false, null);
        element1.setupResolveState(ResolveState.TRANSIENT);
        element1.setupOid(new DummyOid(34));
        
        DummyObjectData el2 = new DummyObjectData(null, "element2", null, false, null);
        element2.setupResolveState(ResolveState.TRANSIENT);
        element2.setupOid(new DummyOid(29));
        
        
        DummyObjectData reference1 = new DummyObjectData(null, "ref1", new Data[] {reference2}, false, null);
        field1.setupResolveState(ResolveState.TRANSIENT);
        field1.setupFields(field1Specification.getFields());
        field1.setupOid(new DummyOid(583));
    
        DummyCollectionData coll = new DummyCollectionData(null, "coll", new ObjectData[] {el1, el2}, null);
    
        ObjectData data = new DummyObjectData(null, "root", new Data[] {reference1, value, coll, null}, false, null);
        rootObject.setupResolveState(ResolveState.TRANSIENT);
        rootObject.setupFields(rootSpecification.getFields());
        rootObject.setupOid(new DummyOid(712));
        
        // test
        ObjectData updates = factory.createMadePersistentGraph(data, rootObject);
        assertEquals(new DummyOid(712), updates.getOid());
        assertEquals(new DummyVersion(11), updates.getVersion());
        assertEquals(4, updates.getFieldContent().length);
        
        // check root's 3 fields
        ObjectData field1 = (ObjectData) updates.getFieldContent()[0];
        assertEquals("ref1", field1.getType());
        assertEquals(new DummyOid(583), field1.getOid());
        assertEquals(new DummyVersion(12), field1.getVersion());
        assertEquals(1, field1.getFieldContent().length);
        
        Data field2 =updates.getFieldContent()[1];
        assertEquals(null, field2);
    
        CollectionData field3 =(CollectionData) updates.getFieldContent()[2];
        assertNotNull(field3);
        assertEquals("coll", field3.getType());
        assertEquals(null, field3.getOid());
        assertEquals(new DummyVersion(27), field3.getVersion());
        assertEquals(2, field3.getElements().length);
    
        Data field4 =updates.getFieldContent()[3];
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

    public void testCreateMadePersistentGraphWereSomePersistent() {
        // complete setup
        DummyObjectData reference2 = new DummyObjectData(null, "ref2", null, false, null);
        field1_1.setupResolveState(ResolveState.TRANSIENT);
        field1_1.setupOid(new DummyOid(192));

        DummyValueData value = new DummyValueData(null, "val");

        DummyObjectData el1 = new DummyObjectData(new DummyOid(34), "element1", null, false, null);
        element1.setupResolveState(ResolveState.TRANSIENT);
        element1.setupOid(new DummyOid(34));
        
        DummyObjectData el2 = new DummyObjectData(null, "element2", null, false, null);
        element2.setupResolveState(ResolveState.TRANSIENT);
        element2.setupOid(new DummyOid(29));
        
        
        DummyObjectData reference1 = new DummyObjectData(new DummyOid(583), "ref1", new Data[] {reference2}, false, null);
        field1.setupResolveState(ResolveState.RESOLVED);
        field1.setupFields(field1Specification.getFields());
        field1.setupOid(new DummyOid(583));

        DummyCollectionData coll = new DummyCollectionData(null, "coll", new ObjectData[] {el1, el2}, null);

        ObjectData data = new DummyObjectData(null, "root", new Data[] {reference1, value, coll, null}, false, null);
        rootObject.setupResolveState(ResolveState.TRANSIENT);
        rootObject.setupFields(rootSpecification.getFields());
        rootObject.setupOid(new DummyOid(712));
        
        // test
        ObjectData updates = factory.createMadePersistentGraph(data, rootObject);
        assertEquals(new DummyOid(712), updates.getOid());
        assertEquals(new DummyVersion(11), updates.getVersion());
        assertEquals(4, updates.getFieldContent().length);
        
        // check root's 3 fields
        ObjectData field1 = (ObjectData) updates.getFieldContent()[0];
        assertEquals("first field was persistent, no data sent back", null, field1);
        
        Data field2 =updates.getFieldContent()[1];
        assertEquals(null, field2);

        CollectionData field3 =(CollectionData) updates.getFieldContent()[2];
        assertNotNull(field3);
        assertEquals("coll", field3.getType());
        assertEquals(null, field3.getOid());
        assertEquals(new DummyVersion(27), field3.getVersion());
        assertEquals(2, field3.getElements().length);

        Data field4 =updates.getFieldContent()[3];
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

        ObjectData rootData = (ObjectData) factory.createDataForParameter("", rootObject);
        assertEquals(ResolveState.RESOLVED, rootObject.getResolveState());

        assertEquals(rootOid, rootData.getOid());
        assertEquals(rootSpecification.getFullName(), rootData.getType());
        assertEquals(new DummyVersion(11), rootData.getVersion());
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
        field1.setupResolveState(ResolveState.TRANSIENT);

        ObjectData rootData = (ObjectData) factory.createDataForParameter("", rootObject);
        assertEquals(ResolveState.TRANSIENT, rootObject.getResolveState());

        assertEquals(null, rootData.getOid());
        assertEquals(rootSpecification.getFullName(), rootData.getType());
        assertEquals(new DummyVersion(11), rootData.getVersion());
        assertEquals(4, rootData.getFieldContent().length);

        ObjectData referencedFieldData = (ObjectData) rootData.getFieldContent()[0];
        assertEquals(1, referencedFieldData.getFieldContent().length);
        assertEquals(new DummyVersion(12), referencedFieldData.getVersion());
        assertEquals(field1Specification.getFullName(), referencedFieldData.getType());

        ObjectData referencedFieldData2 = (ObjectData) referencedFieldData.getFieldContent()[0];
        assertEquals(null, referencedFieldData2.getFieldContent());
        assertEquals(new DummyVersion(13), referencedFieldData2.getVersion());
        assertEquals(field1Specification.getFullName(), referencedFieldData.getType());

        ValueData valueData = (ValueData) rootData.getFieldContent()[1];
        assertEquals(valueSpecification.getFullName(), valueData.getType());
        assertEquals(value, valueData.getValue());

        NullData emptyFieldData = (NullData) rootData.getFieldContent()[3];
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