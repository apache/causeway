package org.nakedobjects.object.io;

import org.nakedobjects.object.defaults.LocalReflectionFactory;
import org.nakedobjects.object.defaults.NakedObjectSpecificationImpl;
import org.nakedobjects.object.defaults.NakedObjectSpecificationLoaderImpl;
import org.nakedobjects.object.defaults.value.Date;
import org.nakedobjects.object.reflect.defaults.JavaReflectorFactory;
import org.nakedobjects.object.system.TestClock;

import junit.framework.TestCase;

public class ValueMementoTest extends TestCase {

    private Date d;
    private ValueMemento mem;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ValueMementoTest.class);
    }

    protected void setUp() throws Exception {
        new TestClock();
        
    	new NakedObjectSpecificationLoaderImpl();
       	NakedObjectSpecificationImpl.setReflectionFactory(new LocalReflectionFactory());
    	NakedObjectSpecificationImpl.setReflectorFactory(new JavaReflectorFactory());
   
        d = new Date(2001,3, 7);
        mem = new ValueMemento(d);
    }
    
    public void testRecreate() {
        Date d2 = (Date) mem.recreate();
        assertEquals(d, d2);
    }
    
    public void testUpdate() {
        Date d2  = new Date(); 
        mem.update(d2);
        assertEquals(d, d2);
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