package org.nakedobjects.distribution;


import org.nakedobjects.distribution.example.SimpleRemoteObjectFactory;
import org.nakedobjects.distribution.example.SimpleSessionId;
import org.nakedobjects.distribution.server.RemoteMechanism;
import org.nakedobjects.object.TypedNakedCollection;
import org.nakedobjects.object.defaults.LoadedObjectsHashtable;
import org.nakedobjects.object.defaults.LocalReflectionFactory;
import org.nakedobjects.object.defaults.MockObjectManager;
import org.nakedobjects.object.defaults.NakedObjectSpecificationImpl;
import org.nakedobjects.object.reflect.defaults.JavaReflectorFactory;
import org.nakedobjects.object.system.TestClock;

import junit.framework.TestCase;

public class DistributionTest extends TestCase {

    private RemoteMechanism server;
    private SimpleSessionId sessionId;
    private MockObjectManager mockObjectManager;
    private LoadedObjectsHashtable loadedObjects;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(DistributionTest.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
    
        new TestClock();
    
        NakedObjectSpecificationImpl.setReflectionFactory(new LocalReflectionFactory());
        NakedObjectSpecificationImpl.setReflectorFactory(new JavaReflectorFactory());
        
        server = new RemoteMechanism();
        mockObjectManager = MockObjectManager.setup();
        loadedObjects = new LoadedObjectsHashtable();
        server.setObjectManager(mockObjectManager);
        server.setFactory(new SimpleRemoteObjectFactory());
        
        sessionId = new SimpleSessionId(99);
    }
    
    public void testSerialNumber() {
        long serial = server.serialNumber(sessionId, "test");
        assertEquals(108, serial);
    }

    public void test() {
        InstanceSet instances = server.allInstances(sessionId, TestObject.class.getName(), false);
   
        TypedNakedCollection collection = instances.recreateInstances(loadedObjects);
        assertEquals(2, collection.size());
    }
    
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2004  Naked Objects Group Ltd

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

The authors can be contacted via www.nakedobjects.org (the
registered address of Naked Objects Group is Kingsway House, 123 Goldworth
Road, Woking GU21 1NR, UK).
*/