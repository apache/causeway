package org.nakedobjects.object.defaults.value;

import junit.framework.TestCase;

public class TimeTest extends TestCase {

    protected void setUp() throws Exception {
        new TestClock();
    }
    
    public void testSaveRestore() throws Exception {
    	Time time1 = new Time();
    	time1.parse("2003-1-4");
    	assertFalse(time1.isEmpty());
    	
    	Time time2 = new Time();
    	time2.restoreString(time1.saveString());
    	assertEquals(time1.longValue(), time2.longValue());
    	assertFalse(time2.isEmpty());
    }
    
    public void testSaveRestorOfNull() throws Exception {
    	Time time1 = new Time();
    	time1.clear();
    	assertTrue("Time isEmpty", time1.isEmpty());
    	
    	Time time2 = new Time();
    	time2.restoreString(time1.saveString());
 //   	assertEquals(time1.longValue(), time2.longValue());
    	assertTrue(time2.isEmpty());
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