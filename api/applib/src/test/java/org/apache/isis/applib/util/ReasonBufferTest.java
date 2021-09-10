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

public class ReasonBufferTest {
    private ReasonBuffer reason;

    @Before
    public void setUp() {
        reason = new ReasonBuffer();
    }

    @Test
    public void testNoReasonReturnsNull() throws Exception {
        assertEquals(null, reason.getReason());
    }

    @Test
    public void testAReasonReturnsString() throws Exception {
        reason.append("reason 1");
        assertEquals("reason 1", reason.getReason());
    }

    @Test
    public void testConditionalAppendWithTrue() {
        reason.appendOnCondition(true, "reason 1");
        assertEquals("reason 1", reason.getReason());
    }

    @Test
    public void testConditionalAppendWithFalse() {
        reason.appendOnCondition(false, "reason 1");
        assertEquals(null, reason.getReason());
    }

    @Test
    public void testAppendTwoReasons() {
        reason.append("reason 1");
        reason.append("reason 2");
        assertEquals("reason 1; reason 2", reason.getReason());

    }

}
