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

package org.apache.isis.applib.util;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TitleBufferTest {

    String companyName;
    String name;
    TitleTestObject objectWithEmptyTitle;
    TitleTestObject objectWithNoTitle;
    TitleTestObject objectWithTitle;

    TitleBuffer t1;
    TitleBuffer t2;
    TitleBuffer t3;

    @Before
    public void setUp() {
        name = "Fred";
        t1 = new TitleBuffer(name);
        t2 = new TitleBuffer();
        companyName = "ABC Co.";
        objectWithTitle = new TitleTestObject();
        objectWithTitle.setupTitle(companyName);
        objectWithNoTitle = new TitleTestObject();
        objectWithEmptyTitle = new TitleTestObject();
        objectWithEmptyTitle.setupTitle("");
        t3 = new TitleBuffer("This is a long title");
    }

    @Test
    public void testAppend() {
        t1.append("");
        assertEquals("add empty string", name, t1.toString());
        t1.append("Smith");
        name += (" " + "Smith");
        assertEquals("append simple string", name, t1.toString());
        t1.append(",", "");
        assertEquals("append empty string with delimiter", name, t1.toString());
        t1.append(",", null);
        assertEquals("append null with delimiter", name, t1.toString());
        t1.append(",", "Xyz Ltd.");
        name += (", " + "Xyz Ltd.");
        assertEquals("append string with delimiter", name, t1.toString());
    }

    @Test
    public void testAppendEmptyStringLeavesBufferUnchanged() {
        t1.append("");
        assertEquals(name, t1.toString());
    }

    @Test
    public void testAppendEmptyStringWithJoinerLeavesBufferUnchanged() {
        t1.append(",", "");
        assertEquals(name, t1.toString());
    }

    @Test
    public void testAppendInt() {
        final TitleBuffer t = new TitleBuffer();
        t.append(123);
        assertEquals("123", t.toString());
    }

    @Test
    public void testAppendNull() {
        t1.append(null);
        assertEquals(name, t1.toString());
    }

    @Test
    public void testAppendNullWithJoinerLeavesBufferUnchanged() {
        t1.append(",", (Object) null);
        assertEquals(name, t1.toString());
    }

    @Test
    public void testAppendObjectsWithJoinerAddsTitleWithJoinerAndSpace() {
        t1.append(",", objectWithTitle);
        assertEquals(name + ", " + companyName, t1.toString());
    }

    @Test
    public void testAppendObjectsWithJoinerOnlyAddsTitleWhenBufferEmpty() {
        t2.append(",", objectWithTitle);
        assertEquals(name, t1.toString());
    }

    @Test
    public void testAppendObjectWhereDefaultNotNeeded() {
        t1.append(objectWithTitle, "none");
        assertEquals(name + " " + companyName, t1.toString());
    }

    @Test
    public void testAppendObjectWhereDefaultUsedAsObjectHasEmptyTitle() {
        t1.append(objectWithEmptyTitle, "none");
        assertEquals(name + " " + "none", t1.toString());
    }

    @Test
    public void testAppendObjectWhereDefaultUsedAsReferenceIsNull() {
        t1.append((Object) null, "none");
        assertEquals(name + " " + "none", t1.toString());
    }

    @Test
    public void testAppendObjectWithEmptyTitleLeavesBufferUnchanged() {
        t1.append(objectWithEmptyTitle);
        assertEquals(name, t1.toString());
    }

    @Test
    public void testAppendObjectWithNoTitleLeavesBufferUnchanged() {
        t1.append(objectWithNoTitle);
        assertEquals(name, t1.toString());
    }

    @Test
    public void testAppendObjectWithTitleAddTitleWithSpace() {
        t1.append(objectWithTitle);
        assertEquals(name + " " + companyName, t1.toString());
    }

    @Test
    public void testAppendStringAddStringWithSpace() {
        t1.append("Smith");
        assertEquals("Fred Smith", t1.toString());
    }

    @Test
    public void testAppendStringToEmptyBufferAddsStringWithoutSpace() {
        t2.append("Smith");
        assertEquals("Smith", t2.toString());
    }

    @Test
    public void testAppendStringWithJoinerAddsStringWithJoinerAndSpace() {
        t1.append(",", "Smith");
        assertEquals("Fred, Smith", t1.toString());
    }

    @Test
    public void testAppendToBuffer() {
        final TitleBuffer t = new TitleBuffer("123");
        t.append("test");
        assertEquals("123 test", t.toString());
    }

    @Test
    public void testAppendToEmpty() {
        final TitleBuffer t = new TitleBuffer();
        t.append("test");
        assertEquals("test", t.toString());
    }

    @Test
    public void testAppendValue() {
        final TitleTestObject s = new TitleTestObject();

        t1.append(s);
        assertEquals("append empty TextString", name, t1.toString());

        //
        t1.append(new TitleTestObject("square"));
        assertEquals("append empty TextString", name + " " + "square", t1.toString());
    }

    @Test
    public void testConcatEmptyStringLeavesBufferUnchanged() {
        t1.concat("");
        assertEquals(name, t1.toString());
    }

    @Test
    public void testConcatObjects() {
        t1.concat(objectWithTitle);
        assertEquals(name + companyName, t1.toString());
    }

    @Test
    public void testConcatObjectsWhereDefaultNotNeededAddsTitle() {
        t1.concat(objectWithTitle, "none");
        assertEquals(name + companyName, t1.toString());
    }

    @Test
    public void testConcatObjectsWhereNoTitleAddDefaultTitle() {
        t1.concat(objectWithNoTitle, "none");
        assertEquals(name + "none", t1.toString());
    }

    @Test
    public void testConcatObjectWhereNoTitleLeavesBufferUnchanged() {
        t1.concat(objectWithNoTitle);
        assertEquals(name, t1.toString());
    }

    @Test
    public void testConcatObjectWhereTitleIsAdded() {
        t1.concat(objectWithTitle);
        assertEquals("FredABC Co.", t1.toString());
    }

    @Test
    public void testConcatStringAddsString() {
        t1.concat("Smith");
        assertEquals("FredSmith", t1.toString());
    }

    @Test
    public void testConstructorsWithObjectWhereDefaultIsNotNeeded() {
        t1 = new TitleBuffer(objectWithTitle, "test");
        assertEquals("ABC Co.", t1.toString());
    }

    @Test
    public void testConstructorsWithObjectWhereDefaultIsUsedAsTitle() {
        t1 = new TitleBuffer(objectWithNoTitle, "test");
        assertEquals("test", t1.toString());
    }

    @Test
    public void testConstructorWithObject() {
        final TitleBuffer t = new TitleBuffer(objectWithTitle);
        assertEquals("ABC Co.", t.toString());
    }

    @Test
    public void testConstructorWithObjectWithNoTitle() {
        final TitleBuffer t = new TitleBuffer(objectWithEmptyTitle);
        assertEquals("", t.toString());
    }

    @Test
    public void testConstructorWithString() {
        final TitleBuffer t = new TitleBuffer("Test");
        assertEquals("Test", t.toString());
    }

    @Test
    public void testDefaultConstructor() {
        final TitleBuffer t = new TitleBuffer();
        assertEquals("", t.toString());
    }

    @Test
    public void testTruncateHasNoEffectUntilTitleLongEnough() {
        t3.truncate(5);
        assertEquals("This is a long title", t3.toString());
    }

    @Test
    public void testTruncateLimitsTitleLength() {
        t3.truncate(3);
        assertEquals("This is a...", t3.toString());
    }

    @Test
    public void testTruncateMustBeAUsableLength() {
        try {
            t3.truncate(0);
            fail("Exception expected");
        } catch (final IllegalArgumentException ee) {
        }
    }
}
