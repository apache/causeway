package org.nakedobjects.distribution;

import org.nakedobjects.TestSystem;
import org.nakedobjects.object.DummyNakedObjectSpecification;
import org.nakedobjects.object.DummyObjectLoader;
import org.nakedobjects.object.MockNakedObject;
import org.nakedobjects.object.MockOid;
import org.nakedobjects.object.ResolveState;
import org.nakedobjects.object.persistence.defaults.MockField;
import org.nakedobjects.object.reflect.NakedObjectField;

import junit.framework.TestCase;


public class ObjectDataFactoryTest extends TestCase {
    private TestingObjectDataFactory factory;
    private DummyNakedObjectSpecification specification;
    private MockNakedObject object;
    private TestSystem system;
    private DummyObjectLoader objectLoader;


    public static void main(String[] args) {
        junit.textui.TestRunner.run(ObjectDataFactoryTest.class);
    }

    protected void setUp() throws Exception {
        system = new TestSystem();
        objectLoader = new DummyObjectLoader();
        system.setObjectLoader(objectLoader);
        system.init();
        
        
        factory = new TestingObjectDataFactory();
        
        specification = new DummyNakedObjectSpecification();
        specification.fields = new NakedObjectField[0];

        object = new MockNakedObject();
        object.setupResolveState(ResolveState.NEW);
        object.setupSpecification(specification);
    }
    
    protected void tearDown() throws Exception {
        system.shutdown();
    }
    
    public void testBasicObject() {
        MockOid oid = new MockOid(1);
        object.setOid(oid);
        
        ObjectData od = factory.createObjectData(object, 0);

        assertEquals(oid, od.getOid());
        assertEquals(specification.getFullName(), od.getType());
        assertEquals(false, od.isResolved());
        assertEquals(0, od.getVersion());
        assertEquals(0, od.getFieldContent().length);
        assertEquals(ResolveState.SERIALIZING_RESOLVED, object.getResolveState());
    }

    public void testResolved() {
        object.setupResolveState(ResolveState.RESOLVED);
        
        ObjectData od = factory.createObjectData(object, 0);

        assertEquals(true, od.isResolved());
        assertEquals(ResolveState.SERIALIZING_RESOLVED, object.getResolveState());
    }
    
    public void testVersion() {
        object.setVersion(78821L);
        
        ObjectData od = factory.createObjectData(object, 0);

        assertEquals(78821L, od.getVersion());
        assertEquals(ResolveState.SERIALIZING_RESOLVED, object.getResolveState());
    }


    public void testObjectWithEmptyFields() {
        specification.fields = new NakedObjectField[] {
                new MockField(), new MockField(), new MockField()
        };
        
        ObjectData od = factory.createObjectData(object, 0);

        assertEquals(3, od.getFieldContent().length);
        assertEquals(ResolveState.SERIALIZING_RESOLVED, object.getResolveState());
    }

    public void testObjectWithFields() {
        specification.fields = new NakedObjectField[] {
                new MockField(), new MockField(), new MockField()
        };
        
        MockNakedObject fieldObject = new MockNakedObject();
        fieldObject.setupResolveState(ResolveState.NEW);
        
        DummyNakedObjectSpecification fieldSpecification = new DummyNakedObjectSpecification();
        fieldSpecification.fields = new NakedObjectField[0];
        fieldObject.setupSpecification(fieldSpecification);
        
        object.setupFieldValue("", fieldObject);
        
        ObjectData od = factory.createObjectData(object, 0);

        assertEquals(3, od.getFieldContent().length);
        ObjectData objectData = ((ObjectData) od.getFieldContent()[1]);
        assertEquals(fieldSpecification.getFullName(), objectData.getType());
        assertEquals(ResolveState.SERIALIZING_RESOLVED, object.getResolveState());
  
    }
    
    // TODO implement
    public void testTransientHasAllDataSerialized() {
    }


}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
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