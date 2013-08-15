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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TextBlock {
    private static final Logger LOG = LoggerFactory.getLogger(TextBlock.class);
    private static final Logger UI_LOG = LoggerFactory.getLogger("ui." + TextBlock.class.getName());
    private final TextBlockTarget forField;
    private String text;
    private int[] lineBreaks;
    private boolean isFormatted;
    private int lineCount;
    private boolean canWrap;

    TextBlock(final TextBlockTarget forField, final String text, final boolean canWrap) {
        this.forField = forField;
        this.text = text;
        isFormatted = false;
        this.canWrap = canWrap;
    }

    public String getLine(final int line) {
        if (line < 0 || line > lineCount) {
            throw new IllegalArgumentException("line outside of block " + line);
        }

        format();

        final int from = lineStart(line);
        final int to = lineEnd(line);

        return text.substring(from, to);
    }

    public String getText() {
        return text;
    }

    public void deleteLeft(final int line, final int character) {
        final int pos = pos(line, character);
        if (pos > 0) {
            text = text.substring(0, pos - 1) + text.substring(pos);
            isFormatted = false;
        }
    }

    public void delete(final int fromLine, final int fromCharacter, final int toLine, final int toCharacter) {
        format();
        final int from = pos(fromLine, fromCharacter);
        final int to = pos(toLine, toCharacter);
        text = text.substring(0, from) + text.substring(to);
        isFormatted = false;
    }

    public void deleteTo(final int toLine, final int toCharacter) {
        format();
        final int from = 0;
        final int to = pos(toLine, toCharacter);
        text = text.substring(0, from) + text.substring(to);
        isFormatted = false;
    }

    public void deleteFrom(final int fromLine, final int fromCharacter) {
        format();
        final int from = pos(fromLine, fromCharacter);
        final int to = text.length();
        text = text.substring(0, from) + text.substring(to);
        isFormatted = false;
    }

    public void deleteRight(final int line, final int character) {
        final int pos = pos(line, character);
        if (pos < text.length()) {
            text = text.substring(0, pos) + text.substring(pos + 1);
            isFormatted = false;
        }
    }

    public int noLines() {
        format();
        return lineCount + 1;
    }

    private void breakAt(final int breakAt) {
        // TODO deal with growing array
        lineBreaks[lineCount] = breakAt;
        lineCount++;
    }

    private void format() {
        if (canWrap && !isFormatted) {
            lineBreaks = new int[100];
            lineCount = 0;

            final int length = text.length();

            int lineWidth = 0;
            int breakAt = -1;

            for (int pos = 0; pos < length; pos++) {
                final char ch = text.charAt(pos);

                if (ch == '\n') {
                    throw new IllegalStateException("Block must not contain newline characters");
                }

                lineWidth += forField.getText().charWidth(ch);

                if (lineWidth > forField.getMaxFieldWidth()) {
                    breakAt = (breakAt == -1) ? pos - 1 : breakAt;
                    // ensures that a string without spaces doesn't loop forever
                    breakAt(breakAt);

                    // include the remaining chars in the starting width.
                    lineWidth = forField.getText().stringWidth(text.substring(breakAt - 1, pos + 1));

                    // reset for next line
                    // start = breakAt;
                    breakAt = -1;

                    continue;
                }

                if (ch == ' ') {
                    breakAt = pos + 1; // break at the character after the space
                }
            }

            isFormatted = true;
        }
    }

    public void insert(final int line, final int character, final String characters) {
        if (characters.indexOf('\n') >= 0) {
            throw new IllegalArgumentException("Insert characters cannot contain newline");
        }
        final int pos = pos(line, character);
        text = text.substring(0, pos) + characters + text.substring(pos);
        isFormatted = false;
    }

    private int pos(final int line, final int character) {
        int pos = lineStart(line);
        pos += character;
        LOG.debug("position " + pos);
        return pos;
    }

    private int lineStart(final int line) {
        final int pos = line == 0 ? 0 : lineBreaks[line - 1];
        UI_LOG.debug("line " + line + " starts at " + pos);
        return pos;
    }

    private int lineEnd(final int line) {
        final int pos = line >= lineCount ? text.length() : lineBreaks[line];
        UI_LOG.debug("line " + line + " ends at " + pos);
        return pos;
    }

    /**
     * breaks a block at the cursor position by truncating this block and
     * creating a new block and adding the removed text.
     */
    public TextBlock splitAt(final int line, final int character) {
        format();
        final int pos = pos(line, character);
        final TextBlock newBlock = new TextBlock(forField, text.substring(pos), canWrap);
        text = text.substring(0, pos);
        isFormatted = false;
        return newBlock;
    }

    public void setCanWrap(final boolean canWrap) {
        this.canWrap = canWrap;
    }

    @Override
    public String toString() {
        final StringBuffer content = new StringBuffer();
        content.append("TextBlock [");
        content.append("formatted=");
        content.append(isFormatted);
        content.append(",lines=");
        content.append(lineCount);
        content.append(",text=");
        content.append(text);
        content.append(",breaks=");
        if (lineBreaks == null) {
            content.append("none");
        } else {
            for (int i = 0; i < lineBreaks.length; i++) {
                content.append(i == 0 ? "" : ",");
                content.append(lineBreaks[i]);
            }
        }
        content.append("]");
        return content.toString();
    }

    public void join(final TextBlock textBlock) {
        text += textBlock.text;
    }
}
