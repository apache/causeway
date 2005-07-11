package org.nakedobjects.object;

import junit.framework.TestCase;


public class ResolveStateTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ResolveStateTest.class);
    }

    public void testInvalidChangesFromNew() {
        ResolveState from = ResolveState.NEW;
        //assertFalse(from.isValidToChangeTo(ResolveState.GHOST));
        assertFalse(from.isValidToChangeTo(ResolveState.NEW));
        assertFalse(from.isValidToChangeTo(ResolveState.PART_RESOLVED));
        assertFalse(from.isValidToChangeTo(ResolveState.RESOLVED));
        assertFalse(from.isValidToChangeTo(ResolveState.RESOLVING));
        assertFalse(from.isValidToChangeTo(ResolveState.RESOLVING_PART));
        //assertFalse(from.isValidToChangeTo(ResolveState.TRANSIENT));
        assertFalse(from.isValidToChangeTo(ResolveState.UPDATING));
  }

    public void testValidChangesFromNew() {
        ResolveState from = ResolveState.NEW;
        assertTrue(from.isValidToChangeTo(ResolveState.GHOST));
        //assertTrue(from.isValidToChangeTo(ResolveState.NEW));
        //assertTrue(from.isValidToChangeTo(ResolveState.PART_RESOLVED));
        //assertTrue(from.isValidToChangeTo(ResolveState.RESOLVED));
        //assertTrue(from.isValidToChangeTo(ResolveState.RESOLVING));
        //assertTrue(from.isValidToChangeTo(ResolveState.RESOLVING_PART));
        assertTrue(from.isValidToChangeTo(ResolveState.TRANSIENT));
        //assertTrue(from.isValidToChangeTo(ResolveState.UPDATING));
    }
    
    public void testInvalidChangesFromGhost() {
        ResolveState from = ResolveState.GHOST;
        assertFalse(from.isValidToChangeTo(ResolveState.GHOST));
        assertFalse(from.isValidToChangeTo(ResolveState.NEW));
        assertFalse(from.isValidToChangeTo(ResolveState.PART_RESOLVED));
        assertFalse(from.isValidToChangeTo(ResolveState.RESOLVED));
        //assertFalse(from.isValidToChangeTo(ResolveState.RESOLVING));
        //assertFalse(from.isValidToChangeTo(ResolveState.RESOLVING_PART));
        assertFalse(from.isValidToChangeTo(ResolveState.TRANSIENT));
        //assertFalse(from.isValidToChangeTo(ResolveState.UPDATING));
    }

    public void testValidChangesFromGhost() {
        ResolveState from = ResolveState.GHOST;
        //assertTrue(from.isValidToChangeTo(ResolveState.GHOST));
        //assertTrue(from.isValidToChangeTo(ResolveState.NEW));
        //assertTrue(from.isValidToChangeTo(ResolveState.PART_RESOLVED));
        //assertTrue(from.isValidToChangeTo(ResolveState.RESOLVED));
        assertTrue(from.isValidToChangeTo(ResolveState.RESOLVING));
        assertTrue(from.isValidToChangeTo(ResolveState.RESOLVING_PART));
        //assertTrue(from.isValidToChangeTo(ResolveState.TRANSIENT));
        assertTrue(from.isValidToChangeTo(ResolveState.UPDATING));
    }

    public void testInvalidChangesFromTransient() {
        ResolveState from = ResolveState.TRANSIENT;
        assertFalse(from.isValidToChangeTo(ResolveState.GHOST));
        assertFalse(from.isValidToChangeTo(ResolveState.NEW));
        assertFalse(from.isValidToChangeTo(ResolveState.PART_RESOLVED));
        //assertFalse(from.isValidToChangeTo(ResolveState.RESOLVED));
        assertFalse(from.isValidToChangeTo(ResolveState.RESOLVING));
        assertFalse(from.isValidToChangeTo(ResolveState.RESOLVING_PART));
        assertFalse(from.isValidToChangeTo(ResolveState.TRANSIENT));
        assertFalse(from.isValidToChangeTo(ResolveState.UPDATING));
    }

    public void testValidChangesFromTransient() {
        ResolveState from = ResolveState.TRANSIENT;
        //assertTrue(from.isValidToChangeTo(ResolveState.GHOST));
        //assertTrue(from.isValidToChangeTo(ResolveState.NEW));
        //assertTrue(from.isValidToChangeTo(ResolveState.PART_RESOLVED));
        assertTrue(from.isValidToChangeTo(ResolveState.RESOLVED));
        //assertTrue(from.isValidToChangeTo(ResolveState.RESOLVING));
        //assertTrue(from.isValidToChangeTo(ResolveState.RESOLVING_PART));
        //assertTrue(from.isValidToChangeTo(ResolveState.TRANSIENT));
        //assertTrue(from.isValidToChangeTo(ResolveState.UPDATING));
    }

    
    public void testInvalidChangesFromResolvingPart() {
        ResolveState from = ResolveState.RESOLVING_PART;
        assertFalse(from.isValidToChangeTo(ResolveState.GHOST));
        assertFalse(from.isValidToChangeTo(ResolveState.NEW));
        //assertFalse(from.isValidToChangeTo(ResolveState.PART_RESOLVED));
        //assertFalse(from.isValidToChangeTo(ResolveState.RESOLVED));
        assertFalse(from.isValidToChangeTo(ResolveState.RESOLVING));
        assertFalse(from.isValidToChangeTo(ResolveState.RESOLVING_PART));
        assertFalse(from.isValidToChangeTo(ResolveState.TRANSIENT));
        assertFalse(from.isValidToChangeTo(ResolveState.UPDATING));
    }

    public void testValidChangesFromResolvingPart() {
        ResolveState from = ResolveState.RESOLVING_PART;
        //assertTrue(from.isValidToChangeTo(ResolveState.GHOST));
        //assertTrue(from.isValidToChangeTo(ResolveState.NEW));
        assertTrue(from.isValidToChangeTo(ResolveState.PART_RESOLVED));
        assertTrue(from.isValidToChangeTo(ResolveState.RESOLVED));
        //assertTrue(from.isValidToChangeTo(ResolveState.RESOLVING));
        //assertTrue(from.isValidToChangeTo(ResolveState.RESOLVING_PART));
        //assertTrue(from.isValidToChangeTo(ResolveState.TRANSIENT));
        //assertTrue(from.isValidToChangeTo(ResolveState.UPDATING));
    }
    
    
    public void testInvalidChangesFromPartResolved() {
        ResolveState from = ResolveState.PART_RESOLVED;
        assertFalse(from.isValidToChangeTo(ResolveState.GHOST));
        assertFalse(from.isValidToChangeTo(ResolveState.NEW));
        assertFalse(from.isValidToChangeTo(ResolveState.PART_RESOLVED));
        assertFalse(from.isValidToChangeTo(ResolveState.RESOLVED));
        //assertFalse(from.isValidToChangeTo(ResolveState.RESOLVING));
        assertFalse(from.isValidToChangeTo(ResolveState.RESOLVING_PART));
        assertFalse(from.isValidToChangeTo(ResolveState.TRANSIENT));
        assertFalse(from.isValidToChangeTo(ResolveState.UPDATING));
    }

    public void testValidChangesFromPartResolved() {
        ResolveState from = ResolveState.PART_RESOLVED;
        //assertTrue(from.isValidToChangeTo(ResolveState.GHOST));
        //assertTrue(from.isValidToChangeTo(ResolveState.NEW));
        //assertTrue(from.isValidToChangeTo(ResolveState.PART_RESOLVED));
        //assertTrue(from.isValidToChangeTo(ResolveState.RESOLVED));
        assertTrue(from.isValidToChangeTo(ResolveState.RESOLVING));
        //assertTrue(from.isValidToChangeTo(ResolveState.RESOLVING_PART));
        //assertTrue(from.isValidToChangeTo(ResolveState.TRANSIENT));
        //assertTrue(from.isValidToChangeTo(ResolveState.UPDATING));
    }
    
    
    public void testInvalidChangesFromResolving() {
        ResolveState from = ResolveState.RESOLVING;
        assertFalse(from.isValidToChangeTo(ResolveState.GHOST));
        assertFalse(from.isValidToChangeTo(ResolveState.NEW));
        assertFalse(from.isValidToChangeTo(ResolveState.PART_RESOLVED));
        //assertFalse(from.isValidToChangeTo(ResolveState.RESOLVED));
        assertFalse(from.isValidToChangeTo(ResolveState.RESOLVING));
        assertFalse(from.isValidToChangeTo(ResolveState.RESOLVING_PART));
        assertFalse(from.isValidToChangeTo(ResolveState.TRANSIENT));
        assertFalse(from.isValidToChangeTo(ResolveState.UPDATING));
    }

    public void testValidChangesFromResolving() {
        ResolveState from = ResolveState.RESOLVING;
        //assertTrue(from.isValidToChangeTo(ResolveState.GHOST));
        //assertTrue(from.isValidToChangeTo(ResolveState.NEW));
        //assertTrue(from.isValidToChangeTo(ResolveState.PART_RESOLVED));
        assertTrue(from.isValidToChangeTo(ResolveState.RESOLVED));
        //assertTrue(from.isValidToChangeTo(ResolveState.RESOLVING));
        //assertTrue(from.isValidToChangeTo(ResolveState.RESOLVING_PART));
        //assertTrue(from.isValidToChangeTo(ResolveState.TRANSIENT));
        //assertTrue(from.isValidToChangeTo(ResolveState.UPDATING));
    }


    
    public void testInvalidChangesFromResolved() {
        ResolveState from = ResolveState.RESOLVED;
        assertFalse(from.isValidToChangeTo(ResolveState.GHOST));
        assertFalse(from.isValidToChangeTo(ResolveState.NEW));
        assertFalse(from.isValidToChangeTo(ResolveState.PART_RESOLVED));
        assertFalse(from.isValidToChangeTo(ResolveState.RESOLVED));
        assertFalse(from.isValidToChangeTo(ResolveState.RESOLVING));
        assertFalse(from.isValidToChangeTo(ResolveState.RESOLVING_PART));
        assertFalse(from.isValidToChangeTo(ResolveState.TRANSIENT));
        //assertFalse(from.isValidToChangeTo(ResolveState.UPDATING));
    }

    public void testValidChangesFromResolved() {
        ResolveState from = ResolveState.RESOLVED;
        //assertTrue(from.isValidToChangeTo(ResolveState.GHOST));
        //assertTrue(from.isValidToChangeTo(ResolveState.NEW));
        //assertTrue(from.isValidToChangeTo(ResolveState.PART_RESOLVED));
        //assertTrue(from.isValidToChangeTo(ResolveState.RESOLVED));
        //assertTrue(from.isValidToChangeTo(ResolveState.RESOLVING));
        //assertTrue(from.isValidToChangeTo(ResolveState.RESOLVING_PART));
        //assertTrue(from.isValidToChangeTo(ResolveState.TRANSIENT));
        assertTrue(from.isValidToChangeTo(ResolveState.UPDATING));
    }

    
    
    
    
    
    public void testInvalidChangesFromUpdating() {
        ResolveState from = ResolveState.UPDATING;
        assertFalse(from.isValidToChangeTo(ResolveState.GHOST));
        assertFalse(from.isValidToChangeTo(ResolveState.NEW));
        assertFalse(from.isValidToChangeTo(ResolveState.PART_RESOLVED));
        //assertFalse(from.isValidToChangeTo(ResolveState.RESOLVED));
        assertFalse(from.isValidToChangeTo(ResolveState.RESOLVING));
        assertFalse(from.isValidToChangeTo(ResolveState.RESOLVING_PART));
        assertFalse(from.isValidToChangeTo(ResolveState.TRANSIENT));
        assertFalse(from.isValidToChangeTo(ResolveState.UPDATING));
    }

    public void testValidChangesFromUpdating() {
        ResolveState from = ResolveState.UPDATING;
        //assertTrue(from.isValidToChangeTo(ResolveState.GHOST));
        //assertTrue(from.isValidToChangeTo(ResolveState.NEW));
        //assertTrue(from.isValidToChangeTo(ResolveState.PART_RESOLVED));
        assertTrue(from.isValidToChangeTo(ResolveState.RESOLVED));
        //assertTrue(from.isValidToChangeTo(ResolveState.RESOLVING));
        //assertTrue(from.isValidToChangeTo(ResolveState.RESOLVING_PART));
        //assertTrue(from.isValidToChangeTo(ResolveState.TRANSIENT));
        //assertTrue(from.isValidToChangeTo(ResolveState.UPDATING));
    }
    
    
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user. Copyright (C) 2000 -
 * 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is Kingsway House, 123
 * Goldworth Road, Woking GU21 1NR, UK).
 */