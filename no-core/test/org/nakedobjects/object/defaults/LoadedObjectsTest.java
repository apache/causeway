package org.nakedobjects.object.defaults;

import org.nakedobjects.object.LoadedObjects;
import org.nakedobjects.object.MockOid;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.Person;
import org.nakedobjects.object.defaults.value.TestClock;

import junit.framework.TestCase;

public class LoadedObjectsTest extends TestCase {

    private LoadedObjects lookup;
    private MockOid oid;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(LoadedObjectsTest.class);
    }

    protected void setUp() throws Exception {
        new TestClock();
        NakedObjectSpecification.setReflectionFactory(new LocalReflectionFactory());
        
        lookup = new LoadedObjectsHashtable();
        oid = new MockOid(1);
  }
    
    public void testNotLoaded() {
        assertFalse(lookup.isLoaded(oid));
    }
    
    public void testLoaded() {
        Person person = new Person();
        person.setOid(oid);
        lookup.loaded(person);

        assertTrue(lookup.isLoaded(oid));
    }
    
    public void testUnloaded() {
        Person person = new Person();
        person.setOid(oid);
        lookup.loaded(person);
        lookup.unloaded(person);
        assertFalse(lookup.isLoaded(oid));
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