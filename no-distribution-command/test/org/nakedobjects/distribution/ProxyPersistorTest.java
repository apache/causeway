package org.nakedobjects.distribution;

import org.nakedobjects.distribution.dummy.DummyObjectData;
import org.nakedobjects.distribution.dummy.DummyValueData;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.ResolveState;
import org.nakedobjects.object.transaction.TransactionException;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.easymock.MockControl;

import test.org.nakedobjects.object.DummyOid;
import test.org.nakedobjects.object.TestObjectBuilder;
import test.org.nakedobjects.object.TestSystem;
import test.org.nakedobjects.object.reflect.DummyNakedObject;
import test.org.nakedobjects.object.reflect.DummyVersion;
import test.org.nakedobjects.object.reflect.TestPojo;
import test.org.nakedobjects.object.reflect.TestPojoValuePeer;
import test.org.nakedobjects.object.reflect.defaults.TestValue;


public class ProxyPersistorTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ProxyPersistorTest.class);
    }

    private Distribution distribution;
    private MockControl distributionControl;
    private ProxyPersistor persistor;
    private MockControl dataFactoryControl;
    private DataFactory dataFactoryHelper;
    private TestSystem system;

    protected void setUp() throws Exception {
        LogManager.getRootLogger().setLevel(Level.OFF);

        persistor = new ProxyPersistor();
        
        distributionControl = MockControl.createControl(Distribution.class);
        distributionControl.setDefaultMatcher(MockControl.ARRAY_MATCHER);
        distribution = (Distribution) distributionControl.getMock();
        
        dataFactoryControl = MockControl.createControl(DataFactory.class);
        dataFactoryHelper = (DataFactory) dataFactoryControl.getMock();
        
        persistor.setConnection(distribution);
        ObjectEncoder dataFactory = new ObjectEncoder();
        dataFactory.setDataFactory(dataFactoryHelper);
        
        persistor.setObjectDataFactory(dataFactory);
        
        system = new TestSystem();
        system.init();
    }

    public void testMakePersistentOutsideTransaction() throws Exception {
        NakedObject transientObject = new DummyNakedObject();
        try {
           persistor.makePersistent(transientObject);
            fail();
        } catch (TransactionException e) {}
    }

    public void testMakePersistent() throws Exception {
        TestObjectBuilder referencedObject;
        referencedObject = new TestObjectBuilder(new TestPojo());
        referencedObject.setResolveState(ResolveState.TRANSIENT);

        TestValue value = new TestValue(new TestPojoValuePeer());

        TestObjectBuilder obj;
        obj = new TestObjectBuilder(new TestPojo());
        obj.setResolveState(ResolveState.TRANSIENT);

        obj.setValueField("value", value);
        obj.setReferenceField("reference", referencedObject);
        
        obj.init(system);
        

        ObjectData field2 = new DummyObjectData(new DummyOid(345), "type", true, new DummyVersion(456));
        field2.setFieldContent(new Data[] {});
        DummyObjectData transientData = new DummyObjectData(new DummyOid(123), "type", true, new DummyVersion(456));
        transientData.setFieldContent(new Data[] { null, field2 });
        
        DummyValueData dummyValueData = new DummyValueData("", "value type");

        dataFactoryControl.expectAndReturn(
                dataFactoryHelper.createObjectData(null, TestPojo.class.getName(), true, null), transientData);
        dataFactoryControl.expectAndReturn(
                dataFactoryHelper.createObjectData(null, TestPojo.class.getName(), true, null), transientData);
        dataFactoryControl.expectAndDefaultReturn(
                dataFactoryHelper.createValueData(value.toString(), null), dummyValueData);
        dataFactoryControl.replay();
        
        DummyObjectData updateData = new DummyObjectData(new DummyOid(123), "type", true, new DummyVersion(456));
        distributionControl.expectAndReturn(distribution.executeClientAction(null, new ObjectData[] {transientData}, new ObjectData[0],
                new ReferenceData[0]), new ObjectData[] { updateData });
        distributionControl.replay();

        NakedObject transientObject = obj.getAdapter();
        persistor.startTransaction();
        persistor.makePersistent(transientObject);
        persistor.endTransaction();

        assertEquals(new DummyOid(123), transientObject.getOid());
        assertEquals(new DummyOid(345), referencedObject.getAdapter().getOid());
        
        distributionControl.verify();
    }

    public void testObjectChangedtOutsideTransaction() throws Exception {
        DummyNakedObject transientObject = new DummyNakedObject();
        transientObject.setupResolveState(ResolveState.RESOLVED);
        try {
            persistor.objectChanged(transientObject);
            fail();
        } catch (TransactionException e) {}
    }

    public void testObjectChanged() throws Exception {
        TestObjectBuilder referencedObject;
        referencedObject = new TestObjectBuilder(new TestPojo());
        referencedObject.setResolveState(ResolveState.RESOLVED);
        referencedObject.setOid(new DummyOid(23));

        TestValue value = new TestValue(new TestPojoValuePeer());

        TestObjectBuilder obj;
        obj = new TestObjectBuilder(new TestPojo());
        obj.setResolveState(ResolveState.RESOLVED);
        obj.setOid(new DummyOid(56));

        obj.setValueField("value", value);
        obj.setReferenceField("reference", referencedObject);

//        obj.init(system);

        ObjectData[] expectedChanges = new ObjectData[] {new DummyObjectData(new DummyOid(56), 
                TestPojo.class.getName(), true, new DummyVersion())};
        distributionControl.expectAndReturn(distribution.executeClientAction(null, new ObjectData[0], expectedChanges ,
                new ObjectData[0]), new ObjectData[0]);
        distributionControl.replay();

        
        NakedObject object = obj.getAdapter();
        persistor.startTransaction();
        persistor.objectChanged(object);
        persistor.endTransaction();

        distributionControl.verify();
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