package org.nakedobjects.object;


import org.nakedobjects.object.reflect.Association;
import org.nakedobjects.object.value.Date;
import org.nakedobjects.object.value.TestClock;
import org.nakedobjects.object.value.Time;
import org.nakedobjects.object.value.TimeStamp;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import junit.framework.TestCase;


public abstract class NakedObjectTestCase extends TestCase {

    public NakedObjectTestCase(String name) {
        super(name);
    }
    
    public NakedObjectTestCase() {}

    public void assertEquals(String name, String expected, NakedValue value) {
        super.assertEquals(name, expected, value.title().toString());
    }

    public void assertEquals(String expected, NakedValue value) {
        super.assertEquals(expected, value.title().toString());
    }

    public void assertEquals(NakedValue expected, NakedValue value) {
    	super.assertEquals(expected.title().toString(), value.title().toString());
    }

    protected Association findAssocation(String attributeName, NakedObject forObject) {
        NakedClass c = forObject.getNakedClass();

        return (Association) c.getField(attributeName);
    }
    
    protected void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);
        
        TestClock testClock = new TestClock();
        Date.setClock(testClock);
        Time.setClock(testClock);
        TimeStamp.setClock(testClock);
    }
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
