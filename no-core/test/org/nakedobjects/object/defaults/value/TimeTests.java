package org.nakedobjects.object.defaults.value;

import org.nakedobjects.object.ValueParseException;


public class TimeTests extends ValueTestCase {
    private Time t;

    public void testTimeConstructors() {
        assertEquals("Two identically created objects", t.dateValue(), new Time(10, 40).dateValue());
        assertEquals("One object created from another", t.dateValue(), new Time(t).dateValue());
    }

    public void testSetTime() {
        Time t2 = new Time();
        t2.setValue(10, 40);
        assertEquals("Set with values", t.dateValue(), t2.dateValue());
        Time t3 = new Time();
        t3.setValue(10, 40);
        assertEquals("Set with values", t.dateValue(), t3.dateValue());
    }

    public void testGetHour() {
        assertEquals(10, t.getHour());
    }

    public void testGetMinute() {
        assertEquals(40, t.getMinute());
    }
    
    public void testZero() {
        assertEquals("Zero value", 0, Time.getZero());
    }

    public void testGetValue() {
        Time t2 = new Time(0, 0);
        assertEquals("new zero value time", 0, t2.longValue());

        t2 = new Time();
        t2.setValue(0, 0);
        assertEquals("set to zero", 0, t2.longValue());

        Time t3 = new Time(0, 1);
        assertEquals(t2.longValue() + 60, t3.longValue());

        assertEquals(10 * 3600 + 40 * 60, t.longValue());
    }

    public void testClear() {
        assertTrue("After creation should not be empty", !t.isEmpty());
        t.clear();
        assertTrue("After clear should be empty", t.isEmpty());
    }

    public void testDefaultTime() throws InterruptedException {
        Time t1 = new Time();
        assertEquals("temp", t1.dateValue(), new Time().dateValue());
    }

    public void testParseTime() throws ValueParseException {
        t.parse("0:00");
        assertEquals("00:00", 0, t.longValue());

        t.parse("0:01");
        assertEquals("00:00", 60, t.longValue());

        t.parse("11:35 AM");
        assertEquals("11:35", 11 * 3600 + 35 * 60, t.longValue());

        t.parse("12:50");
        assertEquals("12:50", 12 * 3600 + 50 * 60, t.longValue());

        t.parse("14:45");
        assertEquals("14:45", 14 * 3600 + 45 * 60, t.longValue());

        t.parse("22:55");
        assertEquals("22:55", 22 * 3600 + 55 * 60, t.longValue());
        t.parse("23:00");
        assertEquals("23:00", 23 * 3600, t.longValue());

        t.parse("23:59");
        assertEquals("23:59", 23 * 3600 + 59 * 60, t.longValue());
    }

    public void testIsEqualsTo() {
        Time t2 = new Time(0, 0);
        t.clear();

        assertTrue("When object is empty and is compared with non-empty", !t2.isEqualTo(t));
        assertTrue("When object is non-empty and is compared with empty", !t.isEqualTo(t2));
        t2.clear();
        assertTrue("When both objects are empty", !t.isEqualTo(t2));

        t.setValue(1, 15);
        t2.setValue(1, 00);
        assertTrue("When times are different", !t.isEqualTo(t2));

        t2.setValue(1, 15);
        assertTrue("When times are same", t.isEqualTo(t2));
    }

    public void testParseAdd() throws ValueParseException {
        assertEquals("10:40", 10 * 3600 + 40 * 60, t.longValue());

        t.parse("+1");
        assertEquals("11:40", 11 * 3600 + 40 * 60, t.longValue());

        t.parse("+1");
        assertEquals("12:40", 12 * 3600 + 40 * 60, t.longValue());

        t.parse("+22");
        assertEquals("10:40", 10 * 3600 + 40 * 60, t.longValue());

    }

    public void testSave() throws Exception {
       assertEquals("1040", t.saveString());

       t.setValue(6,25);
       assertEquals("0625", t.saveString());

       t.setValue(23,55);
       assertEquals("2355", t.saveString());
    }

       public void testSaveEmpty() throws Exception {
       t.clear();
       assertEquals("NULL", t.saveString());
    }
    
   public void testRestore() {
       t.restoreString("0805");
       assertEquals(8 * 3600 + 5 * 60, t.longValue());

       t.restoreString("2359");
       assertEquals(23 * 3600 + 59 * 60, t.longValue());
   }
       public void testRestoreEmpty() {
           t.restoreString("NULL");
       assertTrue(t.isEmpty());
   }
       
    protected void setUp() throws Exception {
        super.setUp();

        t = new Time(10, 40);
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2003 Naked Objects Group
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
