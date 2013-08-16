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

public class TextBlockTest extends TestCase {

    public static void main(final String[] args) {
        junit.textui.TestRunner.run(TextBlockTest.class);
    }

    private TextBlock block;

    @Override
    protected void setUp() throws Exception {
        org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);

        final TextBlockTarget user = new TextBlockTargetExample();
        block = new TextBlock(user, "Now is the winter of our discontent made summer by this glorious sun of York", true);
        // "Now is the winter "
        // "of our discontent "
        // "made summer by "
        // "this glorious sun "
        // "of York"
    }

    public void testBreakBlock() {
        final TextBlock newBlock = block.splitAt(1, 11);
        assertEquals("Now is the winter ", block.getLine(0));
        assertEquals("of our disc", block.getLine(1));

        assertEquals("ontent made summer ", newBlock.getLine(0));

    }

    public void testCantInsertNewline() {
        try {
            block.insert(0, 4, "\n");
            fail();
        } catch (final IllegalArgumentException expected) {
        }
    }

    public void testCountLine() {
        assertEquals(5, block.noLines());
    }

    public void testDeletePartOfLine() {
        block.delete(0, 4, 0, 11);
        assertEquals("Now winter of our ", block.getLine(0));
        assertEquals("discontent made ", block.getLine(1));

        block.delete(1, 3, 1, 10);
        assertEquals("dis made summer by ", block.getLine(1));
    }

    public void testDeleteLines() {
        block.delete(0, 4, 2, 12);
        // assertEquals("Now ", block.getLine(0));
        assertEquals("Now by this ", block.getLine(0));
        assertEquals("glorious sun of York", block.getLine(1));
    }

    public void testDeleteLeft() {
        block.deleteLeft(0, 3);
        assertEquals("No is the winter of ", block.getLine(0));

        block.deleteLeft(0, 17);
        assertEquals("No is the winterof ", block.getLine(0));
        assertEquals("our discontent ", block.getLine(1));

        block.deleteLeft(1, 9);
        assertEquals("our discntent ", block.getLine(1));
    }

    public void testDeleteRight() {
        block.deleteRight(0, 3);
        assertEquals("Nowis the winter of ", block.getLine(0));

        block.deleteRight(0, 17);
        assertEquals("Nowis the winter f ", block.getLine(0));
        assertEquals("our discontent ", block.getLine(1));

        block.deleteRight(1, 9);
        assertEquals("our discotent ", block.getLine(1));
    }

    public void testGetLine() {
        assertEquals("Now is the winter ", block.getLine(0));
        assertEquals("of our discontent ", block.getLine(1));
        assertEquals("made summer by ", block.getLine(2));
        assertEquals("this glorious sun ", block.getLine(3));
        assertEquals("of York", block.getLine(4));
    }

    public void testGetText() {
        assertEquals("Now is the winter of our discontent made summer by this glorious sun of York", block.getText());
    }

    public void testInsert() {
        block.insert(0, 0, "Quote:");
        assertEquals("Quote:Now is the ", block.getLine(0));

        block.insert(1, 10, "y");
        assertEquals("Quote:Now is the ", block.getLine(0));
        assertEquals("winter of your ", block.getLine(1));
    }

}
