package org.nakedobjects.object.defaults.collection;

import org.nakedobjects.object.DummyNakedObjectSpecification;
import org.nakedobjects.object.MockNakedObject;
import org.nakedobjects.object.NakedObject;

import junit.framework.TestCase;

public class InstanceCollectionVectorTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(InstanceCollectionVectorTest.class);
    }

    public void testSort() {
        NakedObject[] instances = new NakedObject[] {
                object("one"),
                object("two"),
                object("three"),
                object("four"),
        };
        InstanceCollectionVector v = new InstanceCollectionVector(new DummyNakedObjectSpecification(), instances);
        assertEquals(4, v.size());
        assertEquals(instances[0], v.elementAt(0));
        assertEquals(instances[1], v.elementAt(1));
        assertEquals(instances[2], v.elementAt(2));
        assertEquals(instances[3], v.elementAt(3));

        v.sort();
        assertEquals(instances[3], v.elementAt(0));
        assertEquals(instances[0], v.elementAt(1));
        assertEquals(instances[2], v.elementAt(2));
        assertEquals(instances[1], v.elementAt(3));
    }

    private NakedObject object(String string) {
        MockNakedObject object = new MockNakedObject();
        object.setupTitleString(string);
        return object;
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