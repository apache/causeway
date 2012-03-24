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

import junit.framework.TestCase;

public class DebugStringTest extends TestCase {
    private DebugString str;

    @Override
    protected void setUp() throws Exception {
        str = new DebugString();
    }

    public void testLFNotAddedToEmptyString() {
        str.blankLine();
        assertEquals("", str.toString());
    }

    public void testBlankLineAfterFirstLineWithLineFeed() {
        str.appendln("fred");
        str.blankLine();
        assertEquals("fred\n\n", str.toString());
    }

    public void testBlankLineAfterFirstLine() {
        str.append("fred");
        str.blankLine();
        assertEquals("fred\n\n", str.toString());
    }

    public void testOnlyOneBlankLine() {
        str.append("fred");
        str.blankLine();
        str.blankLine();
        str.blankLine();
        assertEquals("fred\n\n", str.toString());
    }

    public void testOnlyOneBlankLine2() {
        str.appendln("fred");
        str.blankLine();
        str.blankLine();
        str.blankLine();
        assertEquals("fred\n\n", str.toString());
    }

    public void testSection() {
        str.startSection("Section A");
        str.appendln("text 1");
        str.appendln("text 2");
        str.endSection();
        str.startSection("Section B");
        assertEquals("\n1. Section A\n" + "------------\n" + "   text 1\n" + "   text 2\n\n\n" + "2. Section B\n" + "------------\n", str.toString());
    }

    public void testTitle() {
        str.appendTitle("New Section");
        str.appendln("text");
        assertEquals("\nNew Section\n" + "-----------\n" + "text\n", str.toString());
    }

}
