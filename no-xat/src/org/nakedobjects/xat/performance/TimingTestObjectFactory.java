package org.nakedobjects.xat.performance;

import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.security.Session;
import org.nakedobjects.xat.Documentor;
import org.nakedobjects.xat.ParameterValueImpl;
import org.nakedobjects.xat.TestClass;
import org.nakedobjects.xat.TestClassImpl;
import org.nakedobjects.xat.TestCollection;
import org.nakedobjects.xat.TestCollectionImpl;
import org.nakedobjects.xat.TestObject;
import org.nakedobjects.xat.TestObjectFactory;
import org.nakedobjects.xat.TestObjectImpl;
import org.nakedobjects.xat.TestValue;
import org.nakedobjects.xat.TestValueImpl;

import java.util.Hashtable;


public class TimingTestObjectFactory implements TestObjectFactory {
    private static TimingDocumentor documentor;

    public TestClass createTestClass(Session session, NakedClass cls) {
        return new TimingTestClass(new TestClassImpl(session, cls, this), documentor);
    }

    public TestObject createTestObject(Session session, NakedObject object) {
        return new TimingTestObject(new TestObjectImpl(session, object, this), documentor);
    }

    public TestCollection createTestCollection(Session session, NakedCollection collection) {
        return new TestCollectionImpl(session, collection);
    }
    
    public TestObject createTestObject(Session session, NakedObject field, Hashtable viewCache) {
        return new TimingTestObject(new TestObjectImpl(session, field, viewCache, this), documentor);
    }
    
    public TestValue createTestValue(Session session, NakedValue object) {
        return new TestValueImpl(session, object);
    }

    public void testStarting(String className, String methodName) {}

    public Documentor getDocumentor() {
        if (documentor == null) {
            documentor = new TimingDocumentor("\\tmp\\timing");
        }
        return documentor;
    }

    public void testEnding() {}

    public TestValue createParamerTestValue(Object value) {
        return new ParameterValueImpl(value);
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