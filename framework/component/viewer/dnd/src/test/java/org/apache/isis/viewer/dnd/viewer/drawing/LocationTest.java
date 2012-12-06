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

import org.apache.isis.viewer.dnd.drawing.Location;

public class LocationTest extends TestCase {

    public static void main(final String[] args) {
        junit.textui.TestRunner.run(LocationTest.class);
    }

    public void testCopy() {
        final Location l = new Location(10, 20);
        final Location m = new Location(l);
        assertTrue(l != m);
        assertEquals(l, m);
    }

    public void testTranslate() {
        final Location l = new Location(10, 20);
        l.move(5, 10);
        assertEquals(new Location(15, 30), l);
        l.move(-10, -5);
        assertEquals(new Location(5, 25), l);
    }
}
