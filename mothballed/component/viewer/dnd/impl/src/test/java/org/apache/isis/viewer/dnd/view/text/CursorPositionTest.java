/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.viewer.dnd.view.text;

import junit.framework.TestCase;

public class CursorPositionTest extends TestCase {

    public static void main(final String[] args) {
        junit.textui.TestRunner.run(CursorPositionTest.class);
    }

    private CursorPosition cursor;

    private void assertPosition(final int line, final int character, final CursorPosition cursor) {
        assertEquals(line, cursor.getLine());
        assertEquals(character, cursor.getCharacter());
    }

    @Override
    protected void setUp() throws Exception {
        final TextFieldContentStub stub = new TextFieldContentStub();
        cursor = new CursorPosition(stub, 12, 3);
        final String text = stub.getText(1);
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
        final CursorPosition copy = new CursorPosition(null, 1, 1);
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
        assertPosition(11, 35, cursor);
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
        assertPosition(12, 7, cursor);
        cursor.wordRight();
        assertPosition(12, 11, cursor);
        cursor.wordRight();
        assertPosition(12, 18, cursor);
        cursor.wordRight();
        assertPosition(12, 21, cursor);
        cursor.wordRight();
        assertPosition(12, 25, cursor);
        cursor.wordRight();
        assertPosition(13, 0, cursor);
        cursor.wordRight();
        assertPosition(13, 4, cursor);
        cursor.wordRight();
        assertPosition(13, 7, cursor);
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
        final CursorPosition other = new CursorPosition(null, 12, 3);
        assertFalse(cursor.isBefore(other));
    }

    public void testIsBeforeWhere2CharacterAfter() {
        final CursorPosition other = new CursorPosition(null, 12, 5);
        assertTrue(cursor.isBefore(other));
    }

    public void testIsBeforeWhere2CharacterBefore() {
        final CursorPosition other = new CursorPosition(null, 12, 1);
        assertFalse(cursor.isBefore(other));
    }

}
