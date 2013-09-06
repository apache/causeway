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

import java.util.Enumeration;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.viewer.dnd.drawing.Location;

public class TextContent {
    private static final Logger LOG = LoggerFactory.getLogger(TextContent.class);
    private static final Logger UI_LOG = LoggerFactory.getLogger("ui." + TextContent.class.getName());
    public static final int NO_WRAPPING = 1;
    public static final int WRAPPING = 0;
    private final Vector blocks;
    private final TextBlockTarget target;
    private int displayFromLine;
    private int availableDisplayLines;
    private final boolean useEmptyLines;
    private final int wrap;

    public TextContent(final TextBlockTarget target, final int noLines, final int wrapStyle) {
        this(target, noLines, wrapStyle, false);
    }

    public TextContent(final TextBlockTarget target, final int noLines, final int wrapStyle, final boolean useEmptyLines) {
        this.target = target;
        this.wrap = wrapStyle;
        this.blocks = new Vector();
        this.useEmptyLines = useEmptyLines;
        availableDisplayLines = noLines;
        displayFromLine = 0;
        addBlock("");
        alignDisplay(0);
    }

    private void addBlock(final String text) {
        final TextBlock block = new TextBlock(target, text, wrap == TextContent.WRAPPING);
        LOG.debug("add block " + block);
        blocks.addElement(block);
    }

    /**
     * Returns the number of lines that this field will display the content.
     * This can be smaller than the actual number of lines of content, but will
     * be at least one.
     */
    public int getNoDisplayLines() {
        return availableDisplayLines;
    }

    /**
     * Aligns the lines of content so that the specified line is within the
     * array of lines returned by getDisplayLines().
     * 
     * @see #getDisplayLines()
     */
    public void alignDisplay(final int line) {
        final int noContentLines = getNoLinesOfContent();
        final int lastLine = noContentLines - 1;

        int displayToLine = Math.min(displayFromLine + availableDisplayLines, noContentLines);
        if (noContentLines <= availableDisplayLines) {
            displayFromLine = 0;
        } else {
            if (line >= displayToLine) {
                displayToLine = line + 3;
                displayToLine = Math.min(displayToLine, lastLine);

                displayFromLine = displayToLine - availableDisplayLines + 1;
                displayFromLine = Math.max(displayFromLine, 0);
            }

            if (line < displayFromLine) {
                displayFromLine = line;
                displayToLine = (displayFromLine + availableDisplayLines) - 1;

                if (displayToLine >= noContentLines) {
                    displayToLine = lastLine;
                    displayFromLine = Math.max(0, displayToLine - availableDisplayLines);
                }
            }
        }

        LOG.debug("display line " + line + " " + displayFromLine + "~" + displayToLine);
    }

    public void breakBlock(final CursorPosition cursorAt) {
        final BlockToLineMapping mapping = findBlockFor(cursorAt.getLine());
        final TextBlock newBlock = mapping.textBlock.splitAt(mapping.line, cursorAt.getCharacter());
        blocks.insertElementAt(newBlock, mapping.index + 1);
    }

    /**
     * deletes the selected text
     */
    public void delete(final TextSelection selection) {
        final CursorPosition from = selection.from();
        final CursorPosition to = selection.to();

        final BlockToLineMapping fromMapping = findBlockFor(from.getLine());
        final int fromBlock = fromMapping.index;
        final int fromLine = fromMapping.line;
        final int fromCharacter = from.getCharacter();

        final BlockToLineMapping toMapping = findBlockFor(to.getLine());
        final int toBlock = toMapping.index;
        final int toLine = toMapping.line;
        final int toCharacter = to.getCharacter();

        if (fromBlock == toBlock) {
            final TextBlock block = (TextBlock) blocks.elementAt(fromBlock);
            block.delete(fromLine, fromCharacter, toLine, toCharacter);
        } else {
            TextBlock block = (TextBlock) blocks.elementAt(toBlock);
            block.deleteTo(toLine, toCharacter);

            block = (TextBlock) blocks.elementAt(fromBlock);
            block.deleteFrom(fromLine, fromCharacter);

            fromMapping.textBlock.join(toMapping.textBlock);
            blocks.removeElementAt(toMapping.index);

            for (int i = fromBlock + 1; i < toBlock; i++) {
                blocks.removeElementAt(i);
            }
        }
    }

    public void deleteLeft(final CursorPosition cursorAt) {
        final BlockToLineMapping mapping = findBlockFor(cursorAt.getLine());
        if (mapping == null || mapping.textBlock == null) {
            throw new IsisException("invalid block " + mapping + " for line " + cursorAt.getLine());
        }
        mapping.textBlock.deleteLeft(mapping.line, cursorAt.getCharacter());
    }

    public void deleteRight(final CursorPosition cursorAt) {
        final BlockToLineMapping mapping = findBlockFor(cursorAt.getLine());
        mapping.textBlock.deleteRight(mapping.line, cursorAt.getCharacter());
    }

    private BlockToLineMapping findBlockFor(final int line) {
        if (line < 0) {
            throw new IllegalArgumentException("Line must be greater than, or equal to, zero: " + line);
        }

        int lineWithinBlock = line;
        for (int i = 0; i < blocks.size(); i++) {
            final TextBlock block = (TextBlock) blocks.elementAt(i);
            final int noLines = block.noLines();
            if (lineWithinBlock < noLines) {
                UI_LOG.debug("block " + i + ", line " + lineWithinBlock);
                return new BlockToLineMapping(i, block, lineWithinBlock);
            }
            lineWithinBlock -= noLines;
        }
        return null;
        // throw new IllegalArgumentException("line number not valid " + line);

    }

    /**
     * returns the entire text of the content, with a newline between each block
     * (but not after the final block.
     */
    public String getText() {
        final StringBuffer content = new StringBuffer();
        final Enumeration e = blocks.elements();
        while (e.hasMoreElements()) {
            final TextBlock block = (TextBlock) e.nextElement();
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
    public String getText(final int forLine) {
        final BlockToLineMapping block = findBlockFor(forLine);
        if (block == null) {
            return null;
        }
        return block.textBlock.getLine(block.line);
    }

    /**
     * returns only the text that is selected
     */
    public String getText(final TextSelection selection) {
        final CursorPosition from = selection.from();
        final CursorPosition to = selection.to();

        final int line = from.getLine();
        String text = getText(line);
        if (from.getLine() == to.getLine()) {
            return text.substring(from.getCharacter(), to.getCharacter());

        } else {
            final StringBuffer str = new StringBuffer();
            str.append(text.substring(from.getCharacter()));
            for (int i = line + 1; i < line + (to.getLine() - from.getLine()); i++) {
                text = getText(i);
                str.append(text);
            }
            text = getText(line + (to.getLine() - from.getLine()));
            str.append(text.substring(0, to.getCharacter()));
            return str.toString();
        }
    }

    public void insert(final CursorPosition cursorAt, final String characters) {
        Assert.assertNotNull(cursorAt);

        final BlockToLineMapping block = findBlockFor(cursorAt.getLine());

        Assert.assertNotNull("failed to get block for line " + cursorAt.getLine(), block);

        block.textBlock.insert(block.line, cursorAt.getCharacter(), characters);
    }

    /**
     * Returns the number of lines required to display the content text in it
     * entirety.
     */
    public int getNoLinesOfContent() {
        int lineCount = 0;
        final Enumeration e = blocks.elements();
        while (e.hasMoreElements()) {
            lineCount += ((TextBlock) e.nextElement()).noLines();
        }
        return lineCount;
    }

    public void setText(final String text) {
        blocks.removeAllElements();

        if (text == null || text.equals("")) {
            addBlock("");
        } else {
            final String[] tokens = text.split("\\n");
            for (final String token : tokens) {
                if (useEmptyLines || token.length() > 0) {
                    addBlock(token);
                }
            }
        }
    }

    @Override
    public String toString() {
        final ToString content = new ToString(this);
        content.append("field", target);
        content.append("lines", availableDisplayLines);
        content.append("blocks=", blocks.size());
        /*
         * for (int i = 0; i < blocks.size(); i++) { content.append(i == 0 ? " "
         * : "\n "); content.append(blocks.elementAt(i)); }
         */
        return content.toString();
    }

    public String[] getDisplayLines() {
        final String[] lines = new String[availableDisplayLines];
        for (int i = 0, j = displayFromLine; i < lines.length; i++, j++) {
            final String line = getText(j);
            lines[i] = line == null ? "" : line;
        }
        return lines;
    }

    public int getDisplayFromLine() {
        return displayFromLine;
    }

    public void setNoDisplayLines(final int noDisplayLines) {
        this.availableDisplayLines = noDisplayLines;
    }

    public void increaseDepth() {
        availableDisplayLines++;
    }

    public boolean decreaseDepth() {
        if (availableDisplayLines > 1) {
            availableDisplayLines--;
            return true;
        } else {
            return false;
        }
    }

    private static class BlockToLineMapping {
        TextBlock textBlock;
        int index;
        int line;

        public BlockToLineMapping(final int blockIndex, final TextBlock block, final int line) {
            this.index = blockIndex;
            this.textBlock = block;
            this.line = line;
        }
    }

    int cursorAtLine(final Location atLocation) {
        LOG.debug("pointer at " + atLocation);
        final int y = atLocation.getY();
        int lineIndex = displayFromLine + (y / target.getText().getLineHeight());
        lineIndex = Math.max(lineIndex, 0);
        return lineIndex;
    }

    int cursorAtCharacter(final Location atLocation, final int lineOffset) {
        final String text = getText(lineOffset);
        if (text == null) {
            for (int i = lineOffset; i >= 0; i--) {
                final String text2 = getText(i);
                if (text2 != null) {
                    final int at = text2.length();
                    LOG.debug("character at " + at + " line " + lineOffset);
                    return at;
                }
            }
        }

        /*
         * slightly offsetting mouse helps the user position the cursor between
         * characters near the pointer rather than always after the pointer
         */
        final int x = atLocation.getX() - 3;

        int at = 0;
        final int endAt = text.length();

        int width = 0;

        while (at < endAt && x > width) {
            width += target.getText().charWidth(text.charAt(at));
            at++;
        }

        LOG.debug("character at " + at + " line " + lineOffset);
        return at;
    }

}
