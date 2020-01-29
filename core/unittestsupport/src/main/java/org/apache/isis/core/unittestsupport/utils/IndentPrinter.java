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
package org.apache.isis.core.unittestsupport.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * Adapted from <tt>groovy.util.IndentPrinter</tt> (published under ASL 2.0).
 *
 * <p>
 *     Used by domain apps only.
 * </p>
 */
public class IndentPrinter {

    private int indentLevel;
    private String indent;
    private Writer out;
    private final boolean addNewlines;

    /**
     * Creates an IndentPrinter backed by a PrintWriter pointing to System.out, with an indent of two spaces.
     *
     * @see #IndentPrinter(Writer, String)
     */
    public IndentPrinter() {
        this(new PrintWriter(System.out), "  ");
    }

    /**
     * Creates an IndentPrinter backed by the supplied Writer, with an indent of two spaces.
     *
     * @param out Writer to output to
     * @see #IndentPrinter(Writer, String)
     */
    public IndentPrinter(Writer out) {
        this(out, "  ");
    }

    /**
     * Creates an IndentPrinter backed by the supplied Writer,
     * with a user-supplied String to be used for indenting.
     *
     * @param out Writer to output to
     * @param indent character(s) used to indent each line
     */
    public IndentPrinter(Writer out, String indent) {
        this(out, indent, true);
    }

    /**
     * Creates an IndentPrinter backed by the supplied Writer,
     * with a user-supplied String to be used for indenting
     * and the ability to override newline handling.
     *
     * @param out Writer to output to
     * @param indent character(s) used to indent each line
     * @param addNewlines set to false to gobble all new lines (default true)
     */
    public IndentPrinter(Writer out, String indent, boolean addNewlines) {
        this.addNewlines = addNewlines;
        if (out == null) {
            throw new IllegalArgumentException("Must specify a Writer");
        }
        this.out = out;
        this.indent = indent;
    }

    /**
     * Prints a string followed by an end of line character.
     *
     * @param  text String to be written
     */
    public void println(String text) {
        printIndent();
        try {
            out.write(text);
            println();
            flush();
        } catch(IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    /**
     * Prints a string.
     *
     * @param  text String to be written
     */
    public void print(String text) {
        try {
            out.write(text);
        } catch(IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    /**
     * Prints a character.
     *
     * @param  c char to be written
     */
    public void print(char c) {
        try {
            out.write(c);
        } catch(IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    /**
     * Prints the current indent level.
     */
    public void printIndent() {
        for (int i = 0; i < indentLevel; i++) {
            try {
                out.write(indent);
            } catch(IOException ioe) {
                throw new RuntimeException(ioe);
            }
        }
    }

    /**
     * Prints an end-of-line character (if enabled via addNewLines property).
     * Defaults to outputting a single '\n' character but by using a custom
     * Writer, e.g. PlatformLineWriter, you can get platform-specific
     * end-of-line characters.
     *
     * @see #IndentPrinter(Writer, String, boolean)
     */
    public void println() {
        if (addNewlines) {
            try {
                out.write("\n");
            } catch(IOException ioe) {
                throw new RuntimeException(ioe);
            }
        }
    }

    public void incrementIndent() {
        ++indentLevel;
    }

    public void decrementIndent() {
        --indentLevel;
    }

    public int getIndentLevel() {
        return indentLevel;
    }

    public void setIndentLevel(int indentLevel) {
        this.indentLevel = indentLevel;
    }

    public void flush() {
        try {
            out.flush();
        } catch(IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}