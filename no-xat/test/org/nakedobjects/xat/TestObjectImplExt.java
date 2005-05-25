package org.nakedobjects.xat;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.utility.ExpectedCalls;

import junit.framework.Assert;

class TestObjectImplExt extends TestObjectImpl {
    ExpectedCalls expected = new ExpectedCalls();
    private Action action;
    private NakedObjectField field;

    public TestObjectImplExt(NakedObject object, TestObjectFactory factory) {
        super(object, factory);
    }
    
    public Action getAction(String name, TestNaked[] parameters) {
        expected.addActualMethod("getAction");
        expected.addActualParameter(name);
        for (int i = 0; i < parameters.length; i++) {
            expected.addActualParameter(parameters[i]);
        }
        return action;
    }
    
    public Action getAction(String name) {
        expected.addActualMethod("getAction");
        expected.addActualParameter(name);
        return action;
    }
    
    protected NakedObjectField fieldAccessorFor(String fieldName) {
        expected.addActualMethod("fieldAccessorFor");
        expected.addActualParameter(fieldName);
        Assert.assertNotNull("must have field for " + fieldName, field);
        return field;
    }
    
    public void setupAction(Action action) {
        this.action = action;
    }
    
    public void setupField(NakedObjectField field) {
        this.field = field;
    }

    public void verify() {
        expected.verify();
    }
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

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