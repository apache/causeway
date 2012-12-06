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


package org.apache.isis.application.valueholder;

public class MultilineTextStringTests extends ValueTestCase {

    public static void main(final String[] args) {
        junit.textui.TestRunner.run(MultilineTextStringTests.class);
    }

    public void testInvalidCharacters() {

        MultilineTextString t = new MultilineTextString("Hello\r");
        assertEquals("Hello\n", t.stringValue());

        t = new MultilineTextString("Hello\r\n");
        assertEquals("Hello\n", t.stringValue());

        t = new MultilineTextString("Hello\n\r");
        assertEquals("Hello\n\n", t.stringValue());
    }

    public void testTitle() {
        String text = "Hello\nYou";
        MultilineTextString t = new MultilineTextString(text);
        assertEquals("Hello\nYou", t.title().toString());

    }

    public void testValidCharacters() {
        String text = "Hello\tYou\n";
        MultilineTextString t = new MultilineTextString(text);
        assertEquals(text, t.stringValue());
    }
}
