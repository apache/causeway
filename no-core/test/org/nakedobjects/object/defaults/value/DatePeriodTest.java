package org.nakedobjects.object.defaults.value;

import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.ValueParseException;
import org.nakedobjects.object.defaults.LocalReflectionFactory;
import org.nakedobjects.object.defaults.value.Date;
import org.nakedobjects.object.defaults.value.DatePeriod;
import org.nakedobjects.system.SystemClock;

import java.util.Locale;

import junit.framework.TestCase;


public class DatePeriodTest extends TestCase {
	
	private DatePeriod mayJul, junAug, junJul, dp1, dp2;

	public DatePeriodTest(String arg0) {
		super(arg0);
	}

	protected void setUp() throws Exception {
		Locale.setDefault(Locale.UK);

		NakedObjectSpecification.setReflectionFactory(new LocalReflectionFactory());
	    	
		Date.setClock(new SystemClock());
		
		mayJul = new DatePeriod();
		mayJul.getStart().setValue(2003,5,1);
		mayJul.getEnd().setValue(2003,7,31);

		junAug = new DatePeriod();
		junAug.getStart().setValue(2003,6,1);
		junAug.getEnd().setValue(2003,8,31);
		
		junJul = new DatePeriod();
		junJul.getStart().setValue(2003,6,1);
		junJul.getEnd().setValue(2003,7,31);
		
		dp1 = new DatePeriod();
		dp2 = new DatePeriod();
	}

	protected void tearDown() throws Exception {
		mayJul = null;
		junAug = null;
		junJul = null;
	}
	
	public void testClear() {
		mayJul.clear();
		assertTrue(mayJul.title().toString().equals("~"));
		assertTrue(mayJul.isEmpty());
	}
		
	
	public void testOverlaps() throws Exception {
		
		assertTrue(mayJul.startsBefore(junAug));
		assertFalse(junAug.startsBefore(mayJul));
		assertTrue(junAug.endsAfter(mayJul));
		assertFalse(mayJul.endsAfter(junAug));
		
		assertTrue(mayJul.overlaps(junAug));

		
		DatePeriod overlap = (DatePeriod) mayJul.deepCopy();
		overlap.overlap(junAug);
		assertTrue(junJul.isSameAs(overlap));
		
		overlap = (DatePeriod) junAug.deepCopy();
		overlap.overlap(mayJul);
		assertTrue(junJul.isSameAs(overlap));
		
		DatePeriod lead = (DatePeriod) junAug.deepCopy();	
		lead.leadDifference(mayJul);
//		assertEquals("01-May-2003 ~ 31-May-2003", lead.title().toString());
		
		DatePeriod tail = (DatePeriod) junAug.deepCopy();	
		tail.tailDifference(mayJul);
		assertEquals("01-Aug-2003 ~ 31-Aug-2003", tail.title().toString());
	}
	

	public void testParse() throws Exception {
		dp1.parse("14-Apr-2003 ~ 16-Nov-2004");
		assertEquals("14-Apr-2003 ~ 16-Nov-2004", dp1.title().toString());
		dp1.parse("15-Jan-2002  ~   6-May-2005");
		assertEquals("15-Jan-2002 ~ 06-May-2005", dp1.title().toString());
		dp1.parse("5-jan-2002~06-may-2005");
		assertEquals("05-Jan-2002 ~ 06-May-2005", dp1.title().toString());
		try {
			dp1.parse("hgjuiy");
			fail();
		}  catch (ValueParseException e){
			assertTrue(e.getMessage().equals("No tilde found"));
		}		
		try {
			dp1.parse("05-Jan-2002 - 06-May-2005");
			fail();
		}  catch (ValueParseException e){
			assertTrue(e.getMessage().equals("No tilde found"));
		}
		try {
			dp1.parse("rtyu~ghjk");
			fail();
		}  catch (ValueParseException e){
			assertTrue(e.getMessage().equals("Invalid date rtyu"));
		}
		try {
			dp1.parse("14-Apr-2003 ~ 16-Nov-2002");
			fail();
		} catch (ValueParseException expected) {
        }
		
		dp1.parse("14-Apr-2003~");
		assertTrue(dp1.title().toString().equals("14-Apr-2003 ~"));
		dp1.parse("~16-Nov-2004");
		assertTrue(dp1.title().toString().equals("~ 16-Nov-2004"));
		
	}

		
	
	public void testSaveAndRestore() throws Exception {
		dp1.parse("04-May-2003 ~ 16-May-2004");
		String s = dp1.saveString();
		assertEquals("20030504~20040516", s);
		dp2.restoreString(s);
		assertEquals("04-May-2003 ~ 16-May-2004", dp2.title().toString());
		assertTrue(dp1.isSameAs(dp2));
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
