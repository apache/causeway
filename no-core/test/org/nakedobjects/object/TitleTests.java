package org.nakedobjects.object;


import org.nakedobjects.object.value.TestClock;
import org.nakedobjects.object.value.TextString;

import junit.framework.TestCase;
import junit.framework.TestSuite;


public class TitleTests extends TestCase {
    String test;
    Title t;
    Employer e;
    String companyName;

    public TitleTests(String name) {
        super(name);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(new TestSuite(TitleTests.class));
    }

    protected void setUp() {
        new TestClock();
        NakedObjectSpecification.setReflectionFactory(new LocalReflectionFactory());
        test = "Fred";
        t = new Title(test);
        assertEquals(test, t.toString());
        companyName = "ABC Co.";
        e = new ConcreteEmployer();

        String companyName = "ABC Co.";

        e.getCompanyName().setValue(companyName);
    }

    public void testAppend() {
        t.append("");
        assertEquals("add empty string", test, t.toString());
        t.append("Smith");
        test += (" " + "Smith");
        assertEquals("append simple string", test, t.toString());
        t.append(",", "");
        assertEquals("append empty string with delimiter", test, t.toString());
        t.append(",", (org.nakedobjects.object.NakedObject) null);
        assertEquals("append null with delimiter", test, t.toString());
        t.append(",", "Xyz Ltd.");
        test += (", " + "Xyz Ltd.");
        assertEquals("append string with delimiter", test, t.toString());
    }

    public void testAppendObjects() {
        t.append((Employer) null);
        assertEquals("append company name", test, t.toString());

        //
        t.append(e);
        assertEquals("append company name", test + " " + companyName, 
            t.toString());
    }

    public void testAppendObjectsWithDefaults() {
        t.append(e, "none");
        assertEquals("concat company name", test + " " + companyName, 
            t.toString());
    }

    public void testAppendObjectsWithDefaults2() {
        t.append((Employer) null, "none");
        assertEquals("concat company name", test + " " + "none", t.toString());
    }

    public void testAppendObjectsWithJoiner() {
        t.append(",", (Employer) null);
        assertEquals(test, t.toString());
        t.append(",", e);
        assertEquals("concat company name", test + ", " + companyName, 
            t.toString());
    }

    public void testAppendStrings() {
        t.append("");
        assertEquals("add empty string", test, t.toString());
        t.append("Smith");
        test += (" " + "Smith");
        assertEquals("append simple string", test, t.toString());
        t.append(",", "");
        assertEquals("append empty string with delimiter", test, t.toString());
        t.append(",", "Xyz Ltd.");
        test += (", " + "Xyz Ltd.");
        assertEquals("append string with delimiter", test, t.toString());
    }

    public void testAppendValue() {
        TextString s = new TextString();

        assertTrue("empty string", s.isEmpty());
        t.append(s);
        assertEquals("append empty TextString", test, t.toString());

        //
        t.append(new TextString("square"));
        assertEquals("append empty TextString", test + " " + "square", 
            t.toString());
    }

    public void testConcatObjects() {
        t.concat(e);
        assertEquals("concat company name", test + companyName, t.toString());
    }

    public void testConcatObjectsWithDefaults() {
        t.concat(e, "none");
        assertEquals("concat company name", test + companyName, t.toString());
    }

    public void testConcatObjectsWithDefaults2() {
        e = null;
        t.concat(e, "none");
        assertEquals("concat company name", test + "none", t.toString());
    }

    public void testConcatStrings() {
        t.concat("");
        assertEquals("add empty string", test, t.toString());
        t.concat("Smith");
        test += "Smith";
        assertEquals("concat simple string", test, t.toString());
        t.concat((org.nakedobjects.object.NakedObject) null);
        assertEquals("concat null with delimiter", test, t.toString());
    }

    public void testConstructors() {
        Title t = new Title();

        assertEquals("empty title", "", t.toString());

        //
        t = new Title("Test");
        assertEquals("for string", "Test", t.toString());

        //
        TextString s = new TextString();

        t = new Title(s);
        assertEquals("for empty TextString object", "", t.toString());

        //
        Employer e = new ConcreteEmployer();

        e.getCompanyName().setValue("Tada");
        t = new Title(e);
        assertEquals("for object", "Tada", t.toString());

        //
        t = new Title(e, "test");
        assertEquals("for object (with default)", "Tada", t.toString());

        //	
        t = new Title(null, "test");
        assertEquals("for no object (with default)", "test", t.toString());
    }
    
    public void testTruncate() {
		String text1 = "This is a";
		String text2 = " long title";
		String fullText = text1 + text2;
    	Title t = new Title(fullText);

		// with a lenght greater than the fullText
    	t.truncate(7);
    	assertEquals(fullText, t.toString());
    	
    	// same size
		t.truncate(5);
		assertEquals(fullText, t.toString());
		
		// smaller
		t.truncate(3);
		assertEquals(text1 + "...", t.toString());

		// none!!
		try{
			t.truncate(0);
			fail("Exception expected");
		} catch(IllegalArgumentException ee){}
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
