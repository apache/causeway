package org.nakedobjects.object.value;

import junit.framework.TestCase;

public class TimeStampTest extends TestCase {
    
    protected void setUp() throws Exception {
        super.setUp();
        
        TimeStamp.setClock(new TestClock());
    }

    public void testSaveRestore() throws Exception {
    	TimeStamp timeStamp1 = new TimeStamp();
    	timeStamp1.parse("2003-1-4 10:45");
    	assertFalse(timeStamp1.isEmpty());
    	
    	TimeStamp timeStamp2 = new TimeStamp();
    	timeStamp2.restoreString(timeStamp1.saveString());
    	assertEquals(timeStamp1.longValue(), timeStamp2.longValue());
    	assertFalse(timeStamp2.isEmpty());
    }
    
    public void testSaveRestorOfNull() throws Exception {
    	TimeStamp timeStamp1 = new TimeStamp();
    	timeStamp1.clear();
    	assertTrue("TimeStamp isEmpty", timeStamp1.isEmpty());
    	
    	TimeStamp timeStamp2 = new TimeStamp();
    	timeStamp2.restoreString(timeStamp1.saveString());
    	assertEquals(timeStamp1.longValue(), timeStamp2.longValue());
    	assertTrue(timeStamp2.isEmpty());
    }

    public void testNew() {
        TimeStamp expected = new TimeStamp(2003, 8, 17, 21, 30, 25);
        TimeStamp actual = new TimeStamp();
        assertEquals(expected, actual);
    }
    
    public void testNow() {
        TimeStamp expected = new TimeStamp(2003, 8, 17, 21, 30, 25);
        TimeStamp actual = new TimeStamp();
        actual.reset();
        assertEquals(expected, actual);
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