package org.nakedobjects.object.defaults.collection;

import org.nakedobjects.object.NakedObjectSpecificationImpl;
import org.nakedobjects.object.NakedObjectSpecificationLoaderImpl;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.defaults.LocalReflectionFactory;
import org.nakedobjects.object.defaults.MockObjectManager;
import org.nakedobjects.object.defaults.value.TestClock;
import org.nakedobjects.object.reflect.defaults.JavaReflectorFactory;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;




public final class PersistentCollectionTestCase extends TestCase {
    private MockObjectManager objectManager;
    public PersistentCollectionTestCase(String name) {
        super(name);
    }

    public static void main(java.lang.String[] args) {
        TestRunner.run(new TestSuite(PersistentCollectionTestCase.class));
    }

    protected void setUp() throws ObjectStoreException {
        LogManager.getLoggerRepository().setThreshold(Level.OFF);
         objectManager = MockObjectManager.setup();
         objectManager.getContext();
         new TestClock();
         new NakedObjectSpecificationLoaderImpl();
         NakedObjectSpecificationImpl.setReflectionFactory(new LocalReflectionFactory());
         NakedObjectSpecificationImpl.setReflectorFactory(new JavaReflectorFactory());
    }

    protected void tearDown() throws Exception {
        objectManager.shutdown();
        super.tearDown();
    }
    
    /**
     *
     */
 
}

/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2003  Naked Objects Group Ltd

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
