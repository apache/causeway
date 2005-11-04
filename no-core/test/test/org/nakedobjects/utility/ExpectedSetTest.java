package test.org.nakedobjects.utility;


import junit.framework.AssertionFailedError;
import junit.framework.TestCase;


public class ExpectedSetTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ExpectedSetTest.class);
    }

    private ExpectedSet set;

    protected void setUp() throws Exception {
        set = new ExpectedSet();

        set.addExpected("test");
        set.addExpected("expected");
        set.addExpected("list");
    }

    public void testAddActuals() {
        set.addActual("test");
        set.addActual("expected");
        set.addActual("list");
        set.verify();
    }

    public void testAddActualsInWrongOrder() {
        try {
            set.addActual("test");
            set.addActual("list");
        } catch (AssertionFailedError e) {
            return;
        }
        fail();
    }

    public void testAddInvalidActuals() {
        try {
            set.addActual("not");
            set.addActual("part");
        } catch (AssertionFailedError e) {
            return;
        }
        fail();
    }

    public void testAddTooFewActuals() {
        try {
            set.addActual("test");
            set.addActual("expected");
            set.verify();
        } catch (AssertionFailedError e) {
            return;
        }
        fail();
    }

    public void testAddTooManyActuals() {
        try {
            set.addActual("test");
            set.addActual("expected");
            set.addActual("list");
            set.addActual("overrun");
        } catch (AssertionFailedError e) {
            return;
        }
        fail();
    }

    public void testNoActuals() {
        try {
            set.verify();
        } catch (AssertionFailedError e) {
            return;
        }
        fail();
    }

    public void testNoExpectedNoActuals() {
        set = new ExpectedSet();
        set.verify();
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