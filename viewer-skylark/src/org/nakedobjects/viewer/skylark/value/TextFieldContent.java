package org.nakedobjects.viewer.skylark.value;

import org.nakedobjects.utility.NotImplementedException;
import org.nakedobjects.viewer.skylark.Location;

import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Category;


class TextFieldContent {
    private static final Category LOG = Category.getInstance(TextFieldContent.class);
    private static final int MULTILINE_FIELD_SIZE = 8;
    private Vector blocks;
    private TextBlockUser forField;
    private int displayFromLine;
    private int displayToLine;
    private int noDisplayLines = 1; // number of lines to display

    public TextFieldContent(final TextBlockUser field, final boolean multiline) {
        this.forField = field;
        this.blocks = new Vector();
        
        //multiline = false; //value instanceof MultilineTextString;
        noDisplayLines = multiline ? MULTILINE_FIELD_SIZE : 1;
        displayFromLine = 0;
        displayToLine = noDisplayLines - 1;
        addBlock("");
        alignDisplay(0);
    }

    private void addBlock(String text) {
        blocks.addElement(new TextBlock(forField, text));
    }

    /**
     * Returns the number of lines that this field will display the content. This can 
     * be smaller than the actual number of lines of content, but will be at least one.
     */
    public int getNoDisplayLines() {
        return noDisplayLines;
    }
    
    /**
     * Aligns the lines of content so that the specified line is within the array of lines returned
     * by getDisplayLines().
     * 
     * @see #getDisplayLines()
     */
    void alignDisplay(int line) {
        int noContentLines = getNoLinesOfContent();
        int lastLine = noContentLines - 1;

        if (noContentLines < noDisplayLines) {
            displayFromLine = 0;
            displayToLine = lastLine;
        } else {
            if (line > displayToLine) {
                displayToLine = line + 3;
                displayToLine = Math.min(displayToLine, lastLine);

                displayFromLine = displayToLine - noDisplayLines;
                displayFromLine = Math.max(displayFromLine, 0);
            }
            
           noDisplayLines = Math.max(noDisplayLines, displayToLine - displayFromLine + 1);

            if (line < displayFromLine) {
                displayFromLine = line;
                displayToLine = (displayFromLine + noDisplayLines) - 1;

                if (displayToLine >= noContentLines) {
                    displayToLine = lastLine;
                    displayFromLine = Math.max(0, displayToLine - noDisplayLines);
                }
            }
        }

        LOG.debug("display line " + line + " " + displayFromLine + "~" + displayToLine);
    }

    public void breakBlock(CursorPosition cursorAt) {
        TextBlockReference block = getBlockFor(cursorAt.getLine());
        TextBlock newBlock = block.block.breakBlock(block.line, cursorAt.getCharacter());
        blocks.insertElementAt(newBlock, block.blockIndex + 1);
    }

    /**
     * deletes the selected text
     */
    public void delete(TextSelection selection) {
        CursorPosition from = selection.from();
        CursorPosition to = selection.to();

        if (from.getLine() == to.getLine()) {
            TextBlockReference block = getBlockFor(from.getLine());
            block.block.delete(block.line, from.getCharacter(), to.getCharacter());

        } else {
            throw new NotImplementedException();
        }
    }

    public void deleteLeft(CursorPosition cursorAt) {
        TextBlockReference block = getBlockFor(cursorAt.getLine());
        block.block.deleteLeft(block.line, cursorAt.getCharacter());
    }

    public void deleteRight(CursorPosition cursorAt) {
        TextBlockReference block = getBlockFor(cursorAt.getLine());
        block.block.deleteRight(block.line, cursorAt.getCharacter());
    }

    private TextBlockReference getBlockFor(final int line) {
        if (line < 0) {
            throw new IllegalArgumentException("Line must be greater than, or equal to, zero: " + line);
        }
        
        int l = line;
        for (int i = 0; i < blocks.size(); i++) {
            TextBlock block = (TextBlock) blocks.elementAt(i);
            int noLines = block.noLines();
            if (l < noLines) {
                LOG.debug("Block " + i + ", line " + l);
                return new TextBlockReference(i, (TextBlock) blocks.elementAt(i), line);
            }
            l -= noLines;
        }
        return null;
      //  throw new IllegalArgumentException("line number not valid " + line);

    }

    /**
     * returns the entire text of the content, with a newline between each block
     * (but not after the final block.
     */
    public String getText() {
        StringBuffer content = new StringBuffer();
        Enumeration e = blocks.elements();
        while (e.hasMoreElements()) {
            TextBlock block = (TextBlock) e.nextElement();
            if (content.length() > 0) {
                content.append("\n");
            }
            content.append(block.getText());
        }
        return content.toString();
    }

    /**
     * returns the text on the specified line
     */
    public String getText(int forLine) {
        TextBlockReference block = getBlockFor(forLine);
        if(block == null) {
            return "";
        }
        return block.block.getLine(block.line);
    }

    /**
     * returns only the text that is selected
     */
    public String getText(TextSelection selection) {
        CursorPosition from = selection.from();
        CursorPosition to = selection.to();

        if (from.getLine() == to.getLine()) {
            TextBlockReference block = getBlockFor(from.getLine());
            return block.block.getText().substring(from.getCharacter(), to.getCharacter());

        } else {
            throw new NotImplementedException();
        }
    }

    public void insert(CursorPosition cursorAt, String characters) {
        TextBlockReference block = getBlockFor(cursorAt.getLine());
        block.block.insert(block.line, cursorAt.getCharacter(), characters);
    }
    
    /**
     * Returns the number of lines required to display the content text in it entirety.
     */
    public int getNoLinesOfContent() {
        int lineCount = 0;
        Enumeration e = blocks.elements();
        while (e.hasMoreElements()) {
            lineCount += ((TextBlock) e.nextElement()).noLines();
        }
        return lineCount;
    }

    public void setText(String text) {
        blocks.removeAllElements();

        if (text.equals("")) {
            addBlock("");
        } else {
            StringTokenizer st = new StringTokenizer(text, "\n");

            while (st.hasMoreTokens()) {
                addBlock(st.nextToken());
            }
        }
    }

    public String toString() {
        StringBuffer content = new StringBuffer();
        content.append("TextFieldContent [");
        content.append("field=");
        content.append(forField);
        content.append(",no blocks=");
        content.append(blocks.size());
        content.append("]\n");

        for (int i = 0; i < blocks.size(); i++) {
            content.append(i == 0 ? "   " : "\n   ");
            content.append(blocks.elementAt(i));
        }

        return content.toString();
    }

    public String[] getDisplayLines() {
        String[] lines = new String[noDisplayLines];
        for (int i = displayFromLine; i <= displayToLine; i++) {
            String line = getText(i);
            lines[i - displayFromLine] = line == null ? "" : line;
        }
        return lines;
    }

    public void setNoDisplayLines(int noDisplayLines) {
        this.noDisplayLines = noDisplayLines;
        displayToLine = displayFromLine + noDisplayLines - 1;
    }
    
    public void increaseDepth() {
        noDisplayLines++;
     }

    public boolean decreaseDepth() {
        if(noDisplayLines > 1) {
            noDisplayLines--;
            return true;
        } else {
            return false;
        }
    }
    

    private static class TextBlockReference {
        TextBlock block;
        int blockIndex;
        int line;

        public TextBlockReference(int blockIndex, TextBlock block, int line) {
            this.blockIndex = blockIndex;
            this.block = block;
            this.line = line;
        }
    }
    
    int cursorAtLine(Location atLocation) {
        LOG.debug("Pointer at " + atLocation);
        int y = atLocation.getY(); // - (forField.getBaseline() - forField.getAscent());
        int lineIndex = displayFromLine + (y / forField.lineHeight());
        lineIndex = Math.max(lineIndex, 0);
        return lineIndex;
    }
    
    

    int cursorAtCharacter(Location atLocation, int line) {
        /*
         * slightly offsetting mouse helps the user position the cursor between
         * characters near the pointer rather than always after the pointer
         */
        int x = atLocation.getX() - 3;
        
        int at = 0;
        if ((displayFromLine + line) <= displayToLine) {
            String text = getText(line);
            int endAt = text.length();
            int width = 0;

            while (at < endAt && x > width) {
                width += forField.charWidth(text.charAt(at));
                at++;
            }
        } else {
            line = displayToLine;
            at = getText(line).length();
        }

        LOG.debug("Character at " + at + " line " + line);
        return at;
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

