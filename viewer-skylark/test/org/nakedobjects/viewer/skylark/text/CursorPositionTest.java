package org.nakedobjects.viewer.skylark.text;

import org.nakedobjects.viewer.skylark.text.CursorPosition;

import junit.framework.TestCase;


public class CursorPositionTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(CursorPositionTest.class);
    }

    private CursorPosition cursor;

    private void assertPosition(int line, int character, CursorPosition cursor) {
        assertEquals(line, cursor.getLine());
        assertEquals(character, cursor.getCharacter());
    }

    protected void setUp() throws Exception {
        TextFieldContentStub stub = new TextFieldContentStub();
        cursor = new CursorPosition(stub, 12, 3);
        String text = stub.getText(1);
        assertEquals(35, text.length());
        assertEquals("Now ", text.substring(0, 4));
       assertEquals("is ", text.substring(4, 7));
       assertEquals("the ", text.substring(7, 11));
       assertEquals("winter ", text.substring(11, 18));
       assertEquals("of ", text.substring(18, 21));
       assertEquals("our ", text.substring(21, 25));
       assertEquals("discontent", text.substring(25, 35));
    }

    public void testBottom() {
        cursor.bottom();
        assertPosition(15, 35, cursor);
    }

    public void testCopy() {
        CursorPosition copy = new CursorPosition(null, 1, 1);
        copy.asFor(cursor);
        assertPosition(12, 3, copy);
    }

    public void testCreate() {
        assertPosition(12, 3, cursor);
    }

    public void testEnd() {
        cursor.end();
        assertPosition(12, 35, cursor);
    }

    public void testHome() {
        cursor.home();
        assertPosition(12, 0, cursor);
    }

    public void testLeft() {
        cursor.left();
        assertPosition(12, 2, cursor);
        cursor.left();
        assertPosition(12, 1, cursor);
        cursor.left();
        assertPosition(12, 0, cursor);
        cursor.left();
        assertPosition(11, 35, cursor);
    }

    public void testLeftPastHome() {
        cursor.home();
        cursor.left();
        assertPosition(11,35, cursor);
    }

    public void testLeftPastTop() {
        cursor.top();
        cursor.left();
        assertPosition(0, 0, cursor);
    }

    public void testLineDown() {
        cursor.lineDown();
        assertPosition(13, 3, cursor);
    }

    public void testLineDownPastBottom() {
        cursor.bottom();
        cursor.lineDown();
        assertPosition(15, 35, cursor);
    }

    public void testLineUp() {
        cursor.lineUp();
        assertPosition(11, 3, cursor);
    }

    public void testLineUpPastTop() {
        cursor.top();
        cursor.lineUp();
        assertPosition(0, 0, cursor);
    }
    
    public void testPageDown() {
        cursor.pageDown();
        assertPosition(14, 3, cursor);
        cursor.pageDown();
        assertPosition(15, 3, cursor);
    }
    
    public void testRight() {
        cursor.right();
        assertPosition(12, 4, cursor);
        cursor.right();
        assertPosition(12, 5, cursor);
    }

    public void testRightPastEnd() {
        cursor.end();
        cursor.right();
        assertPosition(13, 1, cursor);
        cursor.right();
        assertPosition(13, 2, cursor);
    }
    
    public void testRightPastBottom() {
        cursor.bottom();
        cursor.right();
        assertPosition(15, 35, cursor);
    }
    
    public void testRightByNumber() {
        cursor.right(22);
        assertPosition(12, 25, cursor);
        cursor.right(100);
        assertPosition(15, 20, cursor);
        cursor.right(30); // won't move as it would be past end
        assertPosition(15, 20, cursor);
    }

    public void testWordLeft() {
        cursor.wordLeft();
        assertPosition(12, 0, cursor);
        cursor.wordLeft();
        assertPosition(11, 25, cursor);

    }

    public void testWordRight() {
        cursor.wordRight();
        assertPosition(12, 5, cursor);
        cursor.wordRight();
        assertPosition(12, 9, cursor);
        cursor.wordRight();
        assertPosition(12, 16, cursor);
        cursor.wordRight();
        assertPosition(12, 19, cursor);
        cursor.wordRight();
        assertPosition(12, 23, cursor);     
        cursor.wordRight();
        assertPosition(12, 34, cursor);
        cursor.wordRight();
        assertPosition(13, 2, cursor);
        cursor.wordRight();
        assertPosition(13, 5, cursor);
    }
    
    public void testSamePostion() {
        CursorPosition other = new CursorPosition(null, 12, 15);
        assertFalse("different character", cursor.samePosition(other));
        
        other = new CursorPosition(null, 7, 3);
        assertFalse("different line", cursor.samePosition(other));
        
        other = new CursorPosition(null, 12, 3);
        assertTrue(cursor.samePosition(other));
    }



    public void testPageUp() {
        cursor.pageUp();
        assertPosition(10, 3, cursor);
        cursor.pageUp();
        assertPosition(8, 3, cursor);
        cursor.pageUp();
        assertPosition(6, 3, cursor);
        cursor.pageUp();
        assertPosition(4, 3, cursor);
        cursor.pageUp();
        assertPosition(2, 3, cursor);
        cursor.pageUp();
        assertPosition(0, 3, cursor);
        cursor.pageUp();
        assertPosition(0, 3, cursor);
    }
    
    public void testIsBeforeWhereAtSamePosition() {
        CursorPosition other = new CursorPosition(null, 12, 3);
        assertFalse(cursor.isBefore(other));
    }

    public void testIsBeforeWhere2CharacterAfter() {
        CursorPosition other = new CursorPosition(null, 12, 5);
        assertTrue(cursor.isBefore(other));
    }
    
    public void testIsBeforeWhere2CharacterBefore() {
        CursorPosition other = new CursorPosition(null, 12, 1);
        assertFalse(cursor.isBefore(other));
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