package org.nakedobjects.viewer.skylark.text;


import org.nakedobjects.object.repository.NakedObjectsClient;
import org.nakedobjects.utility.configuration.PropertiesConfiguration;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.Text;
import org.nakedobjects.viewer.skylark.text.CursorPosition;
import org.nakedobjects.viewer.skylark.text.TextBlockTarget;
import org.nakedobjects.viewer.skylark.text.TextContent;

import java.awt.Font;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


public class TextFieldContentTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(TextFieldContentTest.class);
    }

    private TextContent content;

    protected void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);

        new NakedObjectsClient().setConfiguration(new PropertiesConfiguration());
        
        TextBlockTarget target = new TextBlockTarget() {
            /* with this configuration:
             * 
             * 		lines are 20 characters;
             * 		each character is 10 pixels;
             * 		first line is 0 <= y <= 24;
             * 		second line is 25 <= y <= 39;
             *  	third line is 40 <= y <= 54
             */

            public int getMaxWidth() {
                return 200;
            }
            
            public Text getText() {
                return new Text() { 

		            public int charWidth(char ch) {
		                return 10;
		            }
		
		            public int stringWidth(String string) {
		                return 40;
		            }
		/*
		            public int getBaseline() {
		                return 20;
		            }
		*/
		            public int getAscent() {
		                return 10;
		            }
		
		            public int getLineHeight() {
		                return 15;
		            }

                    public int getMidPoint() {
                        return 7;
                    }
                    
                    public Font getAwtFont() {
                        return null;
                    }

                    public int getDescent() {
                        return 0;
                    }

                    public int getTextHeight() {
                        return 15;
                    }

                    public int getLineSpacing() {
                        return 0;
                    }
                };
            }
        };
        
        content = new TextContent(target, 4, TextContent.WRAPPING);
    }

    public void testCreate() {
        assertEquals("", content.getText());
        assertEquals(1, content.getNoLinesOfContent());
    }
    
    public void testDisplayLineCount() {
        assertEquals(4, content.getNoDisplayLines());
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
    
    public void testLineBreaks() {
        content.setText("Line one\nLine two\nLine three\nLine four that is long enough that it wraps");
        assertEquals(6, content.getNoLinesOfContent());

        content.setNoDisplayLines(8);
        String[] lines = content.getDisplayLines();
        
        assertEquals(8, lines.length);
        assertEquals("Line one", lines[0]);
        assertEquals("Line two", lines[1]);
        assertEquals("Line three", lines[2]);
        assertEquals("Line four that is ", lines[3]);
        assertEquals("it wraps", lines[5]);
        assertEquals("", lines[6]);
        assertEquals("", lines[7]);

    }
    
    public void testNumberOfDisplayLines() {
        assertEquals(4, content.getNoDisplayLines());
        assertEquals(4, content.getDisplayLines().length);
        assertEquals("", content.getDisplayLines()[0]);
        assertEquals("", content.getDisplayLines()[1]);
        assertEquals("", content.getDisplayLines()[2]);
        assertEquals("", content.getDisplayLines()[3]);

        content.setNoDisplayLines(6);
        assertEquals(6, content.getNoDisplayLines());
        assertEquals(6, content.getDisplayLines().length);
        assertEquals("", content.getDisplayLines()[0]);
        assertEquals("", content.getDisplayLines()[1]);
        assertEquals("", content.getDisplayLines()[2]);
        assertEquals("", content.getDisplayLines()[3]);
        assertEquals("", content.getDisplayLines()[4]);
        assertEquals("", content.getDisplayLines()[5]);
    }

    public void testAlignField() {
        // the following text wraps so it takes up 9 line
        content.setText("Naked Objects - a framework that exposes behaviourally complete business objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group");
        
        assertEquals(9, content.getNoLinesOfContent());
        
        String[] lines = content.getDisplayLines();
        assertEquals(4, lines.length);
        assertEquals("Naked Objects - a ", lines[0]);
        assertEquals("framework that ", lines[1]);
        assertEquals("exposes ", lines[2]);
        assertEquals("behaviourally complete ", lines[3]);
            
        content.alignDisplay(6);
        assertEquals(4, content.getNoDisplayLines());
        lines = content.getDisplayLines();
        assertEquals(4, lines.length);
        assertEquals("business objects ", lines[0]);
        assertEquals("directly to the user. ", lines[1]);
        assertEquals("Copyright (C) 2000 ", lines[2]);
        assertEquals("- 2005 Naked ", lines[3]);
    }
    
    
    public void testInstert() {
        content.setText("at");
        CursorPosition cursor = new CursorPosition(content, 0, 0);
        content.insert(cursor, "fl");
        
        assertEquals("flat", content.getText());
        assertEquals(4, content.getNoDisplayLines());
        assertEquals(1, content.getNoLinesOfContent());
    }
    

    
    public void testInstertOverTheEndOfLine() {
        CursorPosition cursor = new CursorPosition(content, 0, 0);
        content.insert(cursor, "test insert that is longer than the four lines that were originally allocated for this test");
        
        assertEquals("test insert that is longer than the four lines that were originally allocated for this test", content.getText());
        assertEquals(4, content.getNoDisplayLines());
        assertEquals(6, content.getNoLinesOfContent());
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
        assertEquals(16, content.cursorAtCharacter(new Location(190, 0), 0));
        assertEquals(0, content.cursorAtCharacter(new Location(0, 0), 0));
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

        assertEquals(10, content.cursorAtCharacter(new Location(14, 1000), 3));
        assertEquals(10, content.cursorAtCharacter(new Location(23, 1000), 3));

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