package test.org.nakedobjects.object.reflect.valueadapter;

import org.nakedobjects.object.TextEntryParseException;
import org.nakedobjects.object.value.adapter.DateAdapter;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import junit.framework.TestCase;


public class DateAdapterTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(DateAdapterTest.class);
    }

    private DateAdapter fpv;

    protected void setUp() throws Exception {
        Locale.setDefault(Locale.CANADA);
        
        Date date = new Date(0);
        fpv = new DateAdapter(date);
    }

    public void testDateValue() {
        assertEquals(new Date(0), fpv.dateValue());
        
        fpv.setValue(new Date(50000));
        assertEquals(new Date(50000), fpv.dateValue());
    }

    public void testInvalidParse() throws Exception {
        try {
            fpv.parseTextEntry("date");
            fail();
        } catch (TextEntryParseException expected) {}
    }

    public void testLengths() {
        assertEquals(0, fpv.getMaximumLength());
        assertEquals(0, fpv.getMinumumLength());
    }

    public void testOutputAsString() {
        assertEquals("1-Jan-1970", fpv.titleString());
    }

    public void testParse() throws Exception {
        fpv.parseTextEntry("1/1/1980");
        
        Calendar calendar = Calendar.getInstance();
        calendar.set(1980, 0, 1, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        
        assertEquals(calendar.getTime(), fpv.dateValue());
    }
    
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
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