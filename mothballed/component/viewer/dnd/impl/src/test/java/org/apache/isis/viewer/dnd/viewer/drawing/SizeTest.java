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

package org.apache.isis.viewer.dnd.viewer.drawing;

import junit.framework.TestCase;

import org.apache.isis.viewer.dnd.drawing.Padding;
import org.apache.isis.viewer.dnd.drawing.Size;

public class SizeTest extends TestCase {

    private Size s;

    public static void main(final String[] args) {
        junit.textui.TestRunner.run(SizeTest.class);
    }

    @Override
    protected void setUp() throws Exception {
        org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);

        s = new Size(10, 20);
    }

    public void testCopy() {
        final Size m = new Size(s);
        assertTrue(s != m);
        assertEquals(s, m);
    }

    public void testEnsure() {
        s.ensureWidth(18);
        assertEquals(new Size(18, 20), s);
        s.ensureWidth(12);
        assertEquals(new Size(18, 20), s);

        s.ensureHeight(16);
        assertEquals(new Size(18, 20), s);
        s.ensureHeight(26);
        assertEquals(new Size(18, 26), s);
    }

    public void addPadding() {
        s.extend(new Padding(1, 2, 3, 4));
        assertEquals(new Size(14, 26), s);
    }

    public void testExtend() {
        s.extendWidth(8);
        assertEquals(new Size(18, 20), s);

        s.extendHeight(6);
        assertEquals(new Size(18, 26), s);

        s.extend(new Size(3, 5));
        assertEquals(new Size(21, 31), s);

        s.extend(5, 3);
        assertEquals(new Size(26, 34), s);

    }

}
