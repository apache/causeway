package org.nakedobjects.xat;

import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.security.SecurityContext;
import org.nakedobjects.xat.html.HtmlDocumentor;
import org.nakedobjects.xat.html.HtmlTestClass;
import org.nakedobjects.xat.html.HtmlTestObject;
import org.nakedobjects.xat.html.HtmlTestValue;

import java.util.Hashtable;


public class TestObjectFactory {
    private static HtmlDocumentor documentor;
    private static TestObjectFactory instance;
    
    public static TestObjectFactory  getInstance() {
        if(instance == null) {
            instance = new TestObjectFactory();
        }
        return instance;
    }
    
    protected TestClass createTestClass(SecurityContext context, NakedClass cls) {
        return new HtmlTestClass(new TestClassImpl(context, cls), documentor);
}
    
    protected TestObject createTestObject(SecurityContext context, NakedObject object) {
        return new HtmlTestObject(new TestObjectImpl(context, object), documentor);
    }

    public TestObject createTestObject(SecurityContext context, NakedObject field, Hashtable viewCache) {
        return new HtmlTestObject(new TestObjectImpl(context, field, viewCache), documentor);
    }
    
    protected TestValue createTestValue(NakedValue value) {	
        return new HtmlTestValue(new TestValueImpl(value));
    }

    public HtmlDocumentor getDocumentor(String name) {
        documentor = new HtmlDocumentor("tmp/", name);
        return documentor;
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