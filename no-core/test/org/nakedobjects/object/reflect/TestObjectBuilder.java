package org.nakedobjects.object.reflect;

import org.nakedobjects.TestSystem;
import org.nakedobjects.object.DummyNakedObjectSpecification;
import org.nakedobjects.object.DummyNakedValue;
import org.nakedobjects.object.DummyOid;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.ResolveState;

import java.util.Vector;

/**
 * Creates a all the objects (Pojo, NakedObject, NakedObjectSpecification etc) for use in a 
 * test.  By creating a set of inter-related builders you can simply create the necessary graphs
 * and all the needed suipporting objects.
 */
public class TestObjectBuilder {
    private final Object pojo;
    private DummyNakedObject adapter;
    private DummyNakedObjectSpecification specification;
    private DummyOid oid;
    private ResolveState resolveState;
    private final Vector fieldNames;
    private final Vector fieldContents;
    
    public TestObjectBuilder(Object pojo) {
        this.pojo = pojo;
        resolveState = ResolveState.NEW;
        fieldNames = new Vector();
        fieldContents = new Vector();
    }
    
    public void setReferenceField(String name, TestObjectBuilder reference) {
        fieldNames.addElement(name);
        fieldContents.addElement(reference);
    }
    
    public void setResolveState(ResolveState resolveState) {
        this.resolveState = resolveState;
    }
    
    public void init(TestSystem system) {
        String className = pojo.getClass().getName();
        if(specification == null) {
            specification = new DummyNakedObjectSpecification(className);
        }
        
        if(adapter == null) {
            adapter = new DummyNakedObject(pojo.toString());
        }
        
        adapter.setupResolveState(resolveState);
        adapter.setupObject(pojo);
        adapter.setupSpecification(specification);
        
        if(oid != null) {
            system.addRecreated(oid, adapter);
        }
        system.addSpecification(specification);      
        
        NakedObjectField[] fields = new NakedObjectField[fieldNames.size()];
        for (int i = 0; i < fieldNames.size(); i++) {
            String name = (String) fieldNames.elementAt(i);
            Object content = fieldContents.elementAt(i);
            
            //NakedObjectField field;
            if(content instanceof TestObjectBuilder) {
                TestObjectBuilder testObjectSpec = ((TestObjectBuilder) content);
                testObjectSpec.init(system);
                fields[i]  = new OneToOneAssociation(className, name, testObjectSpec.specification,
                        new TestPojoReferencePeer());
                adapter.setupFieldValue(name, testObjectSpec.getAdapter());
            } else if(content instanceof TestValue) {
                DummyNakedObjectSpecification valueFieldSpec = new DummyNakedObjectSpecification();
                valueFieldSpec.setupIsValue();
                fields[i]  = new OneToOneAssociation(className, name, valueFieldSpec, new TestPojoValuePeer());
                adapter.setupFieldValue(name, new DummyNakedValue());
            }
            
            
        }
        specification.setupFields(fields);

    }
    
    public void setSpecification(DummyNakedObjectSpecification specification) {
        this.specification = specification;
    }
    
    public void setAdapter(DummyNakedObject adapter) {
        this.adapter = adapter;
    }
    
    public void setOid(DummyOid oid) {
        this.oid = oid;
    }
    
    public static void main(String[] args) {
        
        TestObjectBuilder obj;
        obj = new TestObjectBuilder(new TestPojo());
        obj.setOid(new DummyOid(123));

        TestValue value = new TestValue(new TestPojoValuePeer());
        obj.setValueField("value", value);
        /*

        DummyNakedObjectSpecification valueFieldSpec = new DummyNakedObjectSpecification();
        valueFieldSpec.setupIsValue();
        field1 = new OneToOneAssociation("cls", "value", valueFieldSpec, new TestPojoValuePeer());
*/
        TestObjectBuilder referencedObject;
        referencedObject = new TestObjectBuilder(new TestPojo());
        referencedObject.setOid(new DummyOid(345));
        
        //referencedObjectSpec = new DummyNakedObjectSpecification();
        //referencedObject = new TestPojo();
        //referencedObjectAdapter = new DummyNakedObject("referenced object");
        //referencedObjectAdapter.setupResolveState(ResolveState.NEW);
        //referencedObjectAdapter.setupObject(rootObject);
        //referencedObjectAdapter.setupSpecification(this);
        //system.addRecreated(new DummyOid(345), referencedObjectAdapter);
        
        obj.setReferenceField("reference", referencedObject);

     //   OneToOneAssociation field2 = new OneToOneAssociation("cls", "reference", referencedObjectSpec,
        //        new TestPojoReferencePeer());

       // NakedObjectField[] fields = new NakedObjectField[] { field1, field2 };
      //  setupFields(fields);

      //  rootObjectAdapter = new DummyNakedObject("root object");
      //  rootObjectAdapter.setupResolveState(ResolveState.NEW);
      //  rootObjectAdapter.setupObject(rootObject);
      //  rootObjectAdapter.setupSpecification(this);
      //  rootObjectAdapter.setupFieldValue("reference", referencedObjectAdapter);
     //   system.addRecreated(new DummyOid(123), rootObjectAdapter);
        
        TestSystem system = new TestSystem();
        system.init();
        obj.init(system);
    }

    /**
     * Sets up a fields with the specified value.  The specification for the field is also added to the parent's spec.
     */
    public void setValueField(String name, TestValue value) {
        fieldNames.addElement(name);
        fieldContents.addElement(value);
    }

    public NakedObject getAdapter() {
        return adapter;
    }

    public Object getPojo() {
        return pojo;
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