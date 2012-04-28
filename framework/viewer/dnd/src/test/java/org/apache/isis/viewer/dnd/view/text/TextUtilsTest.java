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

import junit.framework.TestCase;

import org.apache.isis.viewer.dnd.drawing.Text;
import org.apache.isis.viewer.dnd.viewer.drawing.DummyText;

public class TextUtilsTest extends TestCase {

    public void testDrawingTextTruncated() {
        /* Word boundaries at 4, 11, 16, 21, 24 & 34 */
        final String title = "test string that will be truncated";
        final Text style = new DummyText();

        assertEquals("test string that will be truncated", TextUtils.limitText(title, style, 340));

        assertEquals("test string that will be...", TextUtils.limitText(title, style, 339));

        assertEquals("test string that will...", TextUtils.limitText(title, style, 210 + 30));

        assertEquals("test string that...", TextUtils.limitText(title, style, 199 + 30));

        assertEquals("test string...", TextUtils.limitText(title, style, 140));

        assertEquals("test...", TextUtils.limitText(title, style, 139));

        assertEquals("test...", TextUtils.limitText(title, style, 70));

        assertEquals("tes...", TextUtils.limitText(title, style, 60));
    }

    public void testDrawingTextTruncatedBeforeCommasEtc() {
        final String title = "test string, that? is truncated";
        final Text style = new DummyText();

        assertEquals("test string, that...", TextUtils.limitText(title, style, 210));

        assertEquals("test string...", TextUtils.limitText(title, style, 199));
    }

    public void testNoSpace() {
        final String title = "test string, that? is truncated";
        final Text style = new DummyText();

        assertEquals("...", TextUtils.limitText(title, style, 5));
    }

}
