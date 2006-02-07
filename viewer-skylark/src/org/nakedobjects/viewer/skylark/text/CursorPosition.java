package org.nakedobjects.viewer.skylark.text;

import org.nakedobjects.viewer.skylark.Location;


public class CursorPosition {
    private int character;
    private int line;
    private final TextContent textContent;

    public CursorPosition(final TextContent content, final CursorPosition pos) {
        this(content, pos.line, pos.character);
    }

    public CursorPosition(final TextContent content, final int line, final int afterCharacter) {
        this.textContent = content;
        this.line = line;
        this.character = afterCharacter;
    }

    public void asFor(CursorPosition pos) {
        line = pos.line;
        character = pos.character;
    }

    /**
     * Move the cursor to the bottom-right of the field
     */
    public void bottom() {
        line = textContent.getNoLinesOfContent() - 1;
        textContent.alignDisplay(line);
        end();
    }

    public void cursorAt(Location atLocation) {
       line = textContent.cursorAtLine(atLocation);
       character = textContent.cursorAtCharacter(atLocation, line);
    }

    /**
     * Move the cursor to the end of the line
     */
    public void end() {
        String text = textContent.getText(line);
        character =  text == null ? 0 : text.length();
    }

    /**
     * @return the character within this line.
     */
    public int getCharacter() {
        return character;
    }

    /**
     * @return the line within the field
     */
    public int getLine() {
        return line;
    }

    /**
     * Move the cursor to the left end of the field
     */
    public void home() {
        character = 0;
    }

    /**
     * Movet the cursor left by one character.
     */
    public void left() {
        if (!((line == 0) && (character == 0))) {
            character--;

            if (character < 0) {
                line--;
                textContent.alignDisplay(line);
                end();
            }
        }
    }

    /**
     * Move down one line.
     */
    public void lineDown() {
        moveDown(1);
    }

    /**
     * Move up one line.
     */
    public void lineUp() {
        moveUp(1);
    }

    private void moveDown(int byLines) {
        int size = textContent.getNoLinesOfContent();

        if (line < (size - 1)) {
            line += byLines;
            line = Math.min(size - 1, line);

            character = Math.min(character, textContent.getText(line).length());

            textContent.alignDisplay(line);
        }
    }

    private void moveUp(int byLines) {
        if (line > 0) {
            line -= byLines;
            line = Math.max(0, line);
            textContent.alignDisplay(line);
        }
    }

    /**
     * Move down one page.
     */
    public void pageDown() {
        moveDown(textContent.getNoDisplayLines() - 1);
    }

    /**
     * Move cursor up by a page
     */
    public void pageUp() {
        moveUp(textContent.getNoDisplayLines() - 1);
    }

    /**
     * Move the cursor right by one character.
     */
    public void right() {
        right(1);
    }

    /**
     * Move the cursor right by one character.
     */
    public void right(int characters) {
        int length = textContent.getText(line).length();

        if ((character + characters) > length) {
            if ((line + 1) < textContent.getNoLinesOfContent()) {
                line++;
                textContent.alignDisplay(line);

                int remainder = (character + characters) - length;
                character = 0;
                right(remainder);
            }
        } else {
            character += characters;
        }
    }

    /**
     * Move the cursor to the top-left of the field
     */
    public void top() {
        line = 0;
        character = 0;
        textContent.alignDisplay(line);
    }

    public String toString() {
        return "CursorPosition [line=" + line + ",character=" + character + "]";
    }

    /**
     * Move the cursor left to the beginning of the previous word.
     */
    public void wordLeft() {
        if (!((line == 0) && (character == 0))) {
            if (character == 0) {
                line--;
                end();
            }

            String text = textContent.getText(line);

            do
                character--;
            while ((character >= 0) && (text.charAt(character) == ' '));

            while ((character >= 0) && (text.charAt(character) != ' ')) {
                character--;
            }

            character++;
        }
    }

    /**
     * Move the cursor right to the end of the current word.
     */
    public void wordRight() {
        String text = textContent.getText(line);
        if (!(line == (textContent.getNoLinesOfContent() - 1) && character == (text.length() - 1))) {
            if (character == text.length() - 1) {
                line++;
                character = 0;
            }

            int lineLength = text.length();
            // skip spaces
            while ((character < lineLength) && (text.charAt(character) == ' ')) {
                character++;
            }
        
            // skip characters (until next space)
            while (character + 1 <=  text.length() && text.charAt(character) != ' ') {
                character++;
            }
        }
    }

    public boolean samePosition(CursorPosition positionToCompare) {
        return line == positionToCompare.line && character == positionToCompare.character;
    }

    public boolean isBefore(CursorPosition positionToCompare) {
        return line < positionToCompare.line || (line == positionToCompare.line && character < positionToCompare.character);
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