package org.nakedobjects.viewer.skylark.text;

import org.nakedobjects.viewer.skylark.Text;
import org.nakedobjects.viewer.skylark.text.TextBlock;
import org.nakedobjects.viewer.skylark.text.TextBlockTarget;

import java.awt.Font;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;


public class TextBlockTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(TextBlockTest.class);
    }

    private TextBlock block;

    protected void setUp() throws Exception {
        LogManager.getRootLogger().setLevel(Level.OFF);

        TextBlockTarget user = new TextBlockTarget() {

            public int getMaxWidth() {
                return 200;
            }

            public Text getText() {
                return new Text() {

                    public int charWidth(char ch) {
                        return 10;
                    }

                    public int getAscent() {
                        return 0;
                    }

                    public int getLineHeight() {
                        return 0;
                    }

                    public int stringWidth(String string) {
                        return 40;
                    }

                    public Font getAwtFont() {
                        return null;
                    }

                    public int getDescent() {
                        return 0;
                    }

                    public int getTextHeight() {
                        return 0;
                    }

                    public int getLineSpacing() {
                        return 0;
                    }
                };
            }

        };

        block = new TextBlock(user, "Now is the winter of our discontent made summer by this glorious sun of York");
    }

    public void testBreakBlock() {
        TextBlock newBlock = block.breakBlock(1, 11);
        assertEquals("Now is the winter ", block.getLine(0));
        assertEquals("of our disc", block.getLine(1));

        assertEquals("ontent made summer ", newBlock.getLine(0));

    }

    public void testCantInsertNewline() {
        try {
            block.insert(0, 4, "\n");
            fail();
        } catch (IllegalArgumentException expected) {}
    }

    public void testCountLine() {
        assertEquals(5, block.noLines());
    }

    public void testDelete() {
        block.delete(0, 4, 11);
        assertEquals("Now winter of our ", block.getLine(0));
        assertEquals("discontent made ", block.getLine(1));

        block.delete(1, 3, 10);
        assertEquals("dis made summer by ", block.getLine(1));

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