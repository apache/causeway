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

package org.apache.isis.metamodel.consent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.isis.metamodel.consent.InteractionResult;
import org.apache.isis.metamodel.interactions.InteractionAdvisorFacet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class InteractionResultTest {

    private InteractionResult result;

    @Before
    public void setUp() throws Exception {
        result = new InteractionResult(null);
    }

    @After
    public void tearDown() throws Exception {
        result = null;
    }

    @Test
    public void shouldHaveNullReasonWhenJustInstantiated() {
        assertEquals(null, result.getReason());
    }

    @Test
    public void shouldBeEmptyWhenJustInstantiated() {
        assertFalse(result.isVetoing());
        assertTrue(result.isNotVetoing());
    }

    @Test
    public void shouldHaveNonNullReasonWhenAdvisedWithNonNull() {
        result.advise("foo", InteractionAdvisorFacet.NOOP);
        assertEquals("foo", result.getReason());
    }

    @Test
    public void shouldConcatenateAdviseWhenAdvisedWithNonNull() {
        result.advise("foo", InteractionAdvisorFacet.NOOP);
        result.advise("bar", InteractionAdvisorFacet.NOOP);
        assertEquals("foo; bar", result.getReason());
    }

    @Test
    public void shouldNotBeEmptyWhenAdvisedWithNonNull() {
        result.advise("foo", InteractionAdvisorFacet.NOOP);
        assertTrue(result.isVetoing());
        assertFalse(result.isNotVetoing());
    }

    @Test
    public void shouldBeEmptyWhenAdvisedWithNull() {
        result.advise(null, InteractionAdvisorFacet.NOOP);
        assertTrue(result.isNotVetoing());
        assertFalse(result.isVetoing());
        assertEquals(null, result.getReason());
    }

}
