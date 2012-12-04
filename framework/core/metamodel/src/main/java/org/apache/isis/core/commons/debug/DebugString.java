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

package org.apache.isis.core.commons.debug;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.StringTokenizer;

public class DebugString implements DebugBuilder {

    private static final int COLUMN_SPACING = 25;
    private static final int INDENT_WIDTH = 3;
    private static final String LINE;
    private static final int MAX_LINE_LENGTH;
    private static final int MAX_SPACES_LENGTH;
    private static final String SPACES = "                                                                            ";

    static {
        LINE = "-------------------------------------------------------------------------------";
        MAX_LINE_LENGTH = LINE.length();
        MAX_SPACES_LENGTH = SPACES.length();
    }

    private int indent = 0;
    private int section = 1;
    private final StringBuffer string = new StringBuffer();
    private boolean newLine = true;

    @Override
    public void concat(final DebugBuilder debug) {
        string.append(debug.toString());
    }

    /**
     * Append the specified number within a space (number of spaces) specified
     * by the width. E.g. "15 " where number is 15 and width is 4.
     */
    @Override
    public void append(final int number, final int width) {
        appendIndent();
        final int len = string.length();
        string.append(number);
        regularizeWidth(width, len);
    }

    /**
     * Append the specified object by calling it <code>toString()</code> method.
     */
    @Override
    public void append(final Object object) {
        if (object instanceof DebuggableWithTitle) {
            indent();
            appendTitle(((DebuggableWithTitle) object).debugTitle());
            ((DebuggableWithTitle) object).debugData(this);
            unindent();
        } else {
            appendIndent();
            string.append(object);
        }
    }

    /**
     * Append the specified object by calling its <code>toString()</code>
     * method, placing it within specified space.
     */
    @Override
    public void append(final Object object, final int width) {
        appendIndent();
        final int len = string.length();
        string.append(object);
        regularizeWidth(width, len);
    }

    /**
     * Append the specified number, displayed in hexadecimal notation, with the
     * specified label, then start a new line.
     */
    @Override
    public void appendAsHexln(final String label, final long value) {
        appendln(label, "#" + Long.toHexString(value));
    }

    /**
     * Append the message and trace of the specified exception.
     */
    @Override
    public void appendException(final Throwable e) {
        ByteArrayOutputStream baos;
        final PrintStream s = new PrintStream(baos = new ByteArrayOutputStream());
        e.printStackTrace(s);
        appendln(e.getMessage());
        appendln(new String(baos.toByteArray()));
        s.close();
    }

    /**
     * Start a new line.
     * 
     * @see #blankLine()
     */
    @Override
    public void appendln() {
        string.append('\n');
        newLine = true;
    }

    @Override
    public void appendPreformatted(final String text) {
        appendln(text);
    }

    /**
     * Append the specified text, then start a new line.
     */
    @Override
    public void appendln(final String text) {
        appendIndent();
        append(text);
        appendln();
        newLine = true;
    }

    /**
     * Append the specified value, displayed as true or false, with the
     * specified label, then start a new line.
     */
    @Override
    public void appendln(final String label, final boolean value) {
        appendln(label, String.valueOf(value));
    }

    /**
     * Append the specified number with the specified label, then start a new
     * line.
     */
    @Override
    public void appendln(final String label, final double value) {
        appendln(label, String.valueOf(value));
    }

    /**
     * Append the specified number, displayed in hexadecimal notation, with the
     * specified label, then start a new line.
     */
    @Override
    public void appendln(final String label, final long value) {
        appendln(label, String.valueOf(value));
    }

    @Override
    public void appendPreformatted(final String label, final String text) {
        StringTokenizer tokenizer = new StringTokenizer(text, "\n\r\f", false);
        if (tokenizer.hasMoreTokens()) {
            appendln(label, tokenizer.nextToken());
        }
        while (tokenizer.hasMoreTokens()) {
            string.append(spaces(indent * INDENT_WIDTH + COLUMN_SPACING + 2));
            string.append(tokenizer.nextToken());
            string.append('\n');
        }
        newLine = true;
    }

    /**
     * Append the specified object with the specified label, then start a new
     * line.
     */
    @Override
    public void appendln(final String label, final Object object) {
        appendIndent();
        string.append(label);
        final int spaces = COLUMN_SPACING - label.length();
        string.append(": " + spaces(spaces > 0 ? spaces : 0));
        string.append(object);
        string.append('\n');
        newLine = true;
    }

    /**
     * Append the elements of the specified array with the specified label. Each
     * element is appended on its own line, and a new line is added after the
     * last element.
     */
    @Override
    public void appendln(final String label, final Object[] object) {
        if (object.length == 0) {
            appendln(label, "empty array");
        } else {
            appendln(label, object[0]);
            for (int i = 1; i < object.length; i++) {
                string.append(spaces(COLUMN_SPACING + 2));
                string.append(object[i]);
                string.append('\n');
            }
            newLine = true;
        }
    }

    /**
     * Append the specified title, then start a new line. A title is shown on
     * two lines with the text on the first line and dashes on the second.
     */
    @Override
    public void appendTitle(final String title) {
        appendTitleString(title);
    }

    private void appendTitleString(final String titleString) {
        appendln();
        appendln(titleString);
        final String underline = LINE.substring(0, Math.min(MAX_LINE_LENGTH, titleString.length()));
        appendln(underline);
    }

    @Override
    public void startSection(final String title) {
        appendTitleString(section++ + ". " + title);
        indent();
    }

    @Override
    public void endSection() {
        appendln();
        unindent();
    }

    /**
     * Append a blank line only if there are existing lines and the previous
     * line is not blank.
     */
    @Override
    public void blankLine() {
        final int length = string.length();
        if (length == 0) {
            return;
        }
        final boolean hasLineEnding = string.charAt(length - 1) == '\n';
        if (!hasLineEnding) {
            string.append('\n');
            string.append('\n');
            newLine = true;
        } else {
            final boolean hasDoubleLineEnding = length >= 2 && string.charAt(length - 2) != '\n';
            if (hasDoubleLineEnding) {
                string.append('\n');
                newLine = true;
            }
        }
    }

    /**
     * Increase indent used when appending.
     */
    @Override
    public void indent() {
        indent++;
    }

    private void appendIndent() {
        if (newLine) {
            final String spaces = spaces(Math.min(MAX_SPACES_LENGTH, indent * INDENT_WIDTH));
            string.append(spaces);
            newLine = false;
        }
    }

    private void regularizeWidth(final int width, final int len) {
        if (width > 0) {
            final int textWidth = string.length() - len;
            if (textWidth > width) {
                string.setLength(len + width - 3);
                string.append("...");
            } else {
                int spaces = width - textWidth;
                spaces = Math.max(0, spaces);
                string.append(SPACES.substring(0, spaces));
            }
        }
    }

    private String spaces(final int spaces) {
        return SPACES.substring(0, spaces);
    }

    /**
     * Decrease indent used when appending.
     */
    @Override
    public void unindent() {
        if (indent > 0) {
            indent--;
        }
    }

    @Override
    public void close() {
    }

    /**
     * Return the <code>String</code> representation of this debug string.
     */
    @Override
    public String toString() {
        return string.toString();
    }
}
