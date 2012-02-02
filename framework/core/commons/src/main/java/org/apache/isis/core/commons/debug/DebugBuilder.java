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

public interface DebugBuilder {

    /**
     * Concatenate the contents of the specified debug builder to the current
     * builder.
     */
    void concat(DebugBuilder debug);

    /**
     * Append the specified number within a space (number of spaces) specified
     * by the width. E.g. "15 " where number is 15 and width is 4.
     */
    void append(final int number, final int width);

    /**
     * Append the specified object by calling it <code>toString()</code> method.
     */
    void append(final Object object);

    /**
     * Append the specified object by calling its <code>toString()</code>
     * method, placing it within specified space.
     */
    void append(final Object object, final int width);

    /**
     * Append the specified number, displayed in hexadecimal notation, with the
     * specified label, then start a new line.
     */
    void appendAsHexln(final String label, final long value);

    /**
     * Append the message and trace of the specified exception.
     */
    void appendException(final Throwable e);

    /**
     * Start a new line.
     * 
     * @see #blankLine()
     */
    void appendln();

    /**
     * Append the specified text, then start a new line.
     */
    void appendln(final String text);

    /**
     * Append the specified text without any formatting.
     */
    void appendPreformatted(final String text);

    /**
     * Append the specified value, displayed as true or false, with the
     * specified label, then start a new line.
     */
    void appendln(final String label, final boolean value);

    /**
     * Append the specified number with the specified label, then start a new
     * line.
     */
    void appendln(final String label, final double value);

    /**
     * Append the specified number, displayed in hexadecimal notation, with the
     * specified label, then start a new line.
     */
    void appendln(final String label, final long value);

    /**
     * Append the specified preformatted text with the specified label, then
     * start a new line.
     */
    void appendPreformatted(final String label, final String text);

    /**
     * Append the specified object with the specified label, then start a new
     * line.
     */
    void appendln(final String label, final Object object);

    /**
     * Append the elements of the specified array with the specified label. Each
     * element is appended on its own line, and a new line is added after the
     * last element.
     */
    void appendln(final String label, final Object[] objects);

    /**
     * Append the specified title, then start a new line. A title is shown on
     * two lines with the text on the first line and dashes on the second.
     */
    void appendTitle(final String title);

    void startSection(final String title);

    void endSection();

    /**
     * Append a blank line only if there are existing lines and the previous
     * line is not blank.
     */
    void blankLine();

    /**
     * Increase indent used when appending.
     */
    void indent();

    /**
     * Decrease indent used when appending.
     */
    void unindent();

    void close();

}
