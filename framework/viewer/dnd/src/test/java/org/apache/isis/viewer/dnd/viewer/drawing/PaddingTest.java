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

public class PaddingTest extends TestCase {

    private Padding p;

    public static void main(final String[] args) {
        junit.textui.TestRunner.run(PaddingTest.class);
    }

    @Override
    protected void setUp() throws Exception {
        p = new Padding(2, 3, 4, 5);
    }

    public void testCopy() {
        final Padding q = new Padding(p);
        assertTrue(p != q);
        assertEquals(p, q);
    }

    public void testValues() {
        assertEquals(2, p.getTop());
        assertEquals(3, p.getLeft());
        assertEquals(4, p.getBottom());
        assertEquals(5, p.getRight());
    }

    public void testExtend() {
        p.extendTop(10);
        assertEquals(new Padding(12, 3, 4, 5), p);

        p.extendLeft(10);
        assertEquals(new Padding(12, 13, 4, 5), p);

        p.extendBottom(10);
        assertEquals(new Padding(12, 13, 14, 5), p);

        p.extendRight(10);
        assertEquals(new Padding(12, 13, 14, 15), p);
    }
}
