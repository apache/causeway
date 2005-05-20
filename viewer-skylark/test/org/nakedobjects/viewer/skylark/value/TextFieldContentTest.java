package org.nakedobjects.viewer.skylark.value;


import org.nakedobjects.NakedObjectsClient;
import org.nakedobjects.container.configuration.Configuration;
import org.nakedobjects.viewer.skylark.Location;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


public class TextFieldContentTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(TextFieldContentTest.class);
    }

    private TextFieldContent content;

    protected void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);

        new NakedObjectsClient().setConfiguration(new Configuration());
        
        TextBlockUser user = new TextBlockUser() {
            /* with this configuration:
             * 
             * 		lines are 20 characters
             * 		each character is 10 pixels
             * 		first line is 0 <= y <= 24
             * 		second line is 25 <= y <= 39
             *  	third line is 40 <= y <= 54
             */
            

            public int charWidth(char ch) {
                return 10;
            }

            public int getMaxWidth() {
                return 200;
            }

            public int stringWidth(String string) {
                return 40;
            }

            public int getBaseline() {
                return 20;
            }

            public int getAscent() {
                return 10;
            }

            public int lineHeight() {
                return 15;
            }
        };

        content = new TextFieldContent(user, false);
    }

    public void testCreate() {
        assertEquals("", content.getText());
        assertEquals(1, content.getNoLinesOfContent());
    }
    
    public void testOneDisplayLine() {
        assertEquals(1, content.getNoDisplayLines());
    }

    public void testMinimalTextEqualsOneLine() {
        content.setText("test");
        assertEquals(1, content.getNoLinesOfContent());
    }

    public void testInsert() {
/*
        CursorPosition cursor = new CursorPosition(0,0);
        content.insert(cursor, "test string");
        */
    }
    
    public void testNumberOfDisplayLines() {
        assertEquals(1, content.getNoDisplayLines());
        assertEquals(1, content.getDisplayLines().length);
        assertEquals("", content.getDisplayLines()[0]);

        content.setNoDisplayLines(4);
        assertEquals(4, content.getNoDisplayLines());
        assertEquals(4, content.getDisplayLines().length);
        assertEquals("", content.getDisplayLines()[0]);
  //      assertEquals("", content.getDisplayLines()[1]);
  //      assertEquals("", content.getDisplayLines()[2]);
  //      assertEquals("", content.getDisplayLines()[3]);

    }

    public void testAlignField() {
        content.setText("Naked Objects - a framework that exposes behaviourally complete business objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group");
        
        assertEquals(1, content.getNoDisplayLines());
        assertEquals(9, content.getNoLinesOfContent());
        
        
        content.alignDisplay(3);
    }
    
    
    
    public void testInstert() {
        CursorPosition cursor = new CursorPosition(content, 0, 0);
        content.insert(cursor, "test insert that is longer than a single line");
        content.alignDisplay(1);
        content.getDisplayLines();
        
        assertEquals(2, content.getDisplayLines().length);
        
    }
    
    public void testCursorPostioningAtCorner() {
        content.setText("test insert that is longer than a single line");
        assertEquals(0, content.cursorAtLine(new Location()));
        assertEquals(0, content.cursorAtCharacter(new Location(), 0));
    }
    
    public void testCursorPostioningByLine() {
        content.setText("test insert that is longer than a single line");
        assertEquals(0, content.cursorAtLine(new Location(1000, 0)));
        assertEquals(0, content.cursorAtLine(new Location(1000, 10)));
        assertEquals(0, content.cursorAtLine(new Location(1000, 14)));

        assertEquals(1, content.cursorAtLine(new Location(1000, 15)));
          
        assertEquals(1, content.cursorAtLine(new Location(1000, 25)));
        assertEquals(1, content.cursorAtLine(new Location(1000, 29)));
     
        assertEquals(2, content.cursorAtLine(new Location(1000, 30)));
        assertEquals(2, content.cursorAtLine(new Location(1000, 44)));

        assertEquals(3, content.cursorAtLine(new Location(1000, 45)));
    }
    
    
    public void testCursorPostioningByCharacter() {
        content.setText("test insert that");
        assertEquals(0, content.cursorAtCharacter(new Location(0, 1000), 0));
        assertEquals(0, content.cursorAtCharacter(new Location(3, 1000), 0));
        
        assertEquals(1, content.cursorAtCharacter(new Location(4, 1000), 0));
        assertEquals(1, content.cursorAtCharacter(new Location(13, 1000), 0));
  
        assertEquals(2, content.cursorAtCharacter(new Location(14, 1000), 0));
        assertEquals(2, content.cursorAtCharacter(new Location(23, 1000), 0));

        assertEquals(15, content.cursorAtCharacter(new Location(153, 1000), 0));
        
        assertEquals(16, content.cursorAtCharacter(new Location(154, 1000), 0));
        
        assertEquals(16, content.cursorAtCharacter(new Location(199, 1000), 0));
    }
    
    public void testCursorPostioningByCharacterPastEnd() {
        content.setText("test insert that");
        assertEquals(16, content.cursorAtCharacter(new Location(35, 0), 2));
    }
    
    public void testCursorPostioningByCharacterOnLine2() {
        content.setNoDisplayLines(4);
        content.setText("test insert that that spans three lines only");
        assertEquals(0, content.cursorAtCharacter(new Location(0, 1000), 2));
        assertEquals(0, content.cursorAtCharacter(new Location(3, 1000), 2));
        
        assertEquals(1, content.cursorAtCharacter(new Location(4, 1000), 2));
        assertEquals(1, content.cursorAtCharacter(new Location(13, 1000), 2));
  
        assertEquals(2, content.cursorAtCharacter(new Location(14, 1000), 2));
        assertEquals(2, content.cursorAtCharacter(new Location(23, 1000), 2));

        assertEquals(0, content.cursorAtCharacter(new Location(14, 1000), 3));
        assertEquals(0, content.cursorAtCharacter(new Location(23, 1000), 3));

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