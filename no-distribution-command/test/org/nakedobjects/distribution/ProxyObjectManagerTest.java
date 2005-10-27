
package org.nakedobjects.distribution;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.TestSystem;
import org.nakedobjects.distribution.dummy.DummyObjectData;
import org.nakedobjects.distribution.dummy.DummyObjectDataFactory;
import org.nakedobjects.object.DummyOid;
import org.nakedobjects.object.DummyVersion;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.ResolveState;
import org.nakedobjects.object.reflect.TestObjectBuilder;
import org.nakedobjects.object.reflect.TestPojo;
import org.nakedobjects.object.reflect.TestPojoValuePeer;
import org.nakedobjects.object.reflect.TestValue;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

public class ProxyObjectManagerTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ProxyObjectManagerTest.class);
    }

    private TestSystem system;
    private DummyDistribution distribution;
    
    protected void setUp() throws Exception {
        LogManager.getRootLogger().setLevel(Level.OFF);
        
        system = new TestSystem();
        ProxyObjectManager om = new ProxyObjectManager();
        distribution = new DummyDistribution();
        om.setConnection(distribution);
        om.setObjectDataFactory(new DummyObjectDataFactory());
        system.setObjectManager(om);
        system.init();
    }
    
    public void testMakedPersistent() throws Exception {
        TestObjectBuilder referencedObject;
        referencedObject = new TestObjectBuilder(new TestPojo());
        referencedObject.setResolveState(ResolveState.TRANSIENT);
        
        TestValue value = new TestValue(new TestPojoValuePeer());
        
        TestObjectBuilder obj;
        obj = new TestObjectBuilder( new TestPojo());
        obj.setResolveState(ResolveState.TRANSIENT);

        obj.setValueField("value", value);
        obj.setReferenceField("reference", referencedObject);

        obj.init(system);

        
        Data field2 = new DummyObjectData(new DummyOid(345), "type", new Data[] {}, true, new DummyVersion(456) );
        distribution.setupMakePersistentResults(new DummyObjectData(new DummyOid(123), "type", new Data[] {null, field2}, true, new DummyVersion(456) ));
       
        NakedObject transientObject = obj.getAdapter();
        NakedObjects.getObjectManager().makePersistent(transientObject);
       
        assertEquals(new DummyOid(123), transientObject.getOid());
        assertEquals(new DummyOid(345), referencedObject.getAdapter().getOid());
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