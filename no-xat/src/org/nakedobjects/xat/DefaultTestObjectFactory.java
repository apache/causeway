package org.nakedobjects.xat;

import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectContext;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.reflect.ValueFieldSpecification;

import java.util.Hashtable;


public class DefaultTestObjectFactory implements TestObjectFactory{
    Documentor documentor = new NullDocumentor();
    
    public TestClass createTestClass(NakedObjectContext context, NakedClass cls) {
        return new TestClassImpl(context, cls, this);
}
    
    public TestObject createTestObject(NakedObjectContext context, NakedObject object) {
        return new TestObjectImpl(context, object, this);
    }

    public TestObject createTestObject(NakedObjectContext context, NakedObject field, Hashtable viewCache) {
        return new TestObjectImpl(context, field, viewCache, this);
    }
    
    public TestValue createTestValue(NakedObject parent, ValueFieldSpecification field) {	
        return new TestValueImpl(parent, field);
    }

    public Documentor getDocumentor(String name) {
        return documentor;
    }

    public TestValue createParamerTestValue(NakedValue value) {
        return new ParameterValueImpl(value);
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