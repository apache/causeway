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

import org.apache.isis.viewer.dnd.drawing.Location;

/**
 * Represents the position of a line cursor within a TextContent. The character
 * position for a line extends from zero to text.length + 1. Where 0 means the
 * cursor is to the left of the first character, and 1 is to right of the first
 * character and to the left of the second character.
 */
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

    public void asFor(final CursorPosition pos) {
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

    public void cursorAt(final Location atLocation) {
        line = textContent.cursorAtLine(atLocation);
        character = textContent.cursorAtCharacter(atLocation, line);

        if (line >= textContent.getNoLinesOfContent()) {
            line = textContent.getNoLinesOfContent() - 1;
            end();
        }
    }

    /**
     * Move the cursor to the end of the line
     */
    public void end() {
        final String text = textContent.getText(line);
        character = text == null ? 0 : text.length();
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

    private void moveDown(final int byLines) {
        final int size = textContent.getNoLinesOfContent();

        if (line < (size - 1)) {
            line += byLines;
            line = Math.min(size - 1, line);

            character = Math.min(character, textContent.getText(line).length());

            textContent.alignDisplay(line);
        }
    }

    private void moveUp(final int byLines) {
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
    public void right(final int characters) {
        final int length = textContent.getText(line).length();

        if ((character + characters) > length) {
            if ((line + 1) < textContent.getNoLinesOfContent()) {
                line++;
                textContent.alignDisplay(line);

                final int remainder = (character + characters) - length;
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

    @Override
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

            final String text = textContent.getText(line);

            do {
                character--;
            } while ((character >= 0) && (text.charAt(character) == ' '));

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
        final String text = textContent.getText(line);
        final int lineLength = text.length();
        if (!(line == textContent.getNoLinesOfContent() - 1 && character == lineLength - 1)) {
            // skip spaces before
            while (character < lineLength && text.charAt(character) == ' ') {
                character++;
            }
            // skip characters (until next space)
            while (character < lineLength && text.charAt(character) != ' ') {
                character++;
            }
            // skip spaces after word
            while (character < lineLength && text.charAt(character) == ' ') {
                character++;
            }
            // wrap to nexrt line if at end
            if (character >= lineLength && line + 1 < textContent.getNoLinesOfContent()) {
                line++;
                character = 0;
            }
        }
    }

    public boolean samePosition(final CursorPosition positionToCompare) {
        return line == positionToCompare.line && character == positionToCompare.character;
    }

    public boolean isBefore(final CursorPosition positionToCompare) {
        return line < positionToCompare.line || (line == positionToCompare.line && character < positionToCompare.character);
    }
}
