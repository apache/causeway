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

package org.apache.isis.applib.value;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

public class PercentageTest {
    Percentage p1;
    Percentage p2;
    Percentage p3;

    @Before
    public void setUp() throws Exception {
        p1 = new Percentage(10.5f);
        p2 = new Percentage(10.5f);
        p3 = new Percentage(12.0f);
    }

    @Test
    public void testEquals() {
        assertEquals(p1, p2);
        assertNotSame(p1, p2);
        assertFalse(p1.equals(p3));
    }

    @Test
    public void testAddFloat() {
        final Percentage p4 = p1.add(10.0f);
        assertEquals(20.5f, p4.floatValue(), 0.0f);
    }

    @Test
    public void testAddPercentage() {
        final Percentage p4 = p1.add(p3);
        assertEquals(22.5f, p4.floatValue(), 0.0f);
    }

    @Test
    public void testIsEqualTo() {
        assertTrue(p1.isEqualTo(p2));
        assertFalse(p1.isEqualTo(p3));
    }

    @Test
    public void testIsLessThan() {
        assertTrue(p1.isLessThan(p3));
        assertFalse(p3.isLessThan(p1));
        assertFalse(p1.isLessThan(p1));
    }
}
