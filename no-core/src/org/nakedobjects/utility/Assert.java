package org.nakedobjects.utility;

public class Assert {

    public static void assertEquals(Object expected, Object actual) {
        assertEquals("", expected, actual);
    }

    public static void assertEquals(String message, Object expected, Object actual) {
        assertTrue(message + ": expected " + expected + " but was " + actual, (expected == null && actual == null)
                || (expected != null && expected.equals(actual)));
    }

    public static void assertFalse(boolean flag) {
        assertTrue("expected false", flag);
    }

    public static void assertFalse(String message, boolean flag) {
        assertTrue(message, !flag);
    }

    public static void assertFalse(String message, Object target, boolean flag) {
        assertTrue(message, target, !flag);
    }

    public static void assertNotNull(Object identified) {
        assertNotNull("", identified);
    }

    public static void assertNotNull(String message, Object object) {
        assertTrue("unexpected null: " + message, object != null);
    }

    public static void assertNotNull(String message, Object target, Object object) {
        assertTrue(message, target, object != null);
    }

    public static void assertNull(Object object) {
        assertTrue("unexpected reference; should be null", object == null);
    }

    public static void assertNull(String message, Object object) {
        assertTrue(message, object == null);
    }

    public static void assertTrue(boolean flag) {
        assertTrue("expected true", flag);
    }

    public static void assertTrue(String message, boolean flag) {
        assertTrue(message, null, flag);
    }

    public static void assertTrue(String message, Object target, boolean flag) {
        if (!flag) {
            throw new NakedObjectAssertException(message + (target == null ? "" : (": " + target)));
        }
    }

    public static void assertEquals(String message, int expected, int value) {
        if(expected != value) {
            throw new NakedObjectAssertException(message + " expected " + expected + "; but was "+ value);
        }
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