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
package org.apache.causeway.applib.query;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.val;

class NamedQueryTest_withStart_or_withCount {

    private NamedQuery<Customer> namedQuery;
    private final static long UNLIMITED = 0L;


    static class Customer {}

    @BeforeEach
    void setUp() throws Exception {
        namedQuery = Query.named(Customer.class, "findByLastName")
                .withParameter("lastName", "Smith");
    }

    @Test
    public void defaults() throws Exception {

        val range = namedQuery.getRange();

        assertThat(range.getStart(), is(0L));
        assertThat(range.getLimit(), is(UNLIMITED));

        assertTrue(range.isUnconstrained());
        assertFalse(range.hasOffset());
        assertFalse(range.hasLimit());
    }

    @Test
    public void typicalHappyCase() throws Exception {

        val range = namedQuery
                .withRange(QueryRange.start(10L).withLimit(5L))
                .getRange();

        assertThat(range.getStart(), is(10L));
        assertThat(range.getLimit(), is(5L));

        assertFalse(range.isUnconstrained());
        assertTrue(range.hasOffset());
        assertTrue(range.hasLimit());
    }

    @Test
    public void happyCase_startOnly() throws Exception {

        val range = namedQuery
                .withRange(QueryRange.start(10L))
                .getRange();

        assertThat(range.getStart(), is(10L));
        assertThat(range.getLimit(), is(UNLIMITED));

        assertFalse(range.isUnconstrained());
        assertTrue(range.hasOffset());
        assertFalse(range.hasLimit());
    }

    @Test
    public void happyCase_startZero() throws Exception {

        val range = namedQuery
                .withRange(QueryRange.start(0L))
                .getRange();

        assertThat(range.getStart(), is(0L));
        assertThat(range.getLimit(), is(UNLIMITED));

        assertTrue(range.isUnconstrained());
        assertFalse(range.hasOffset());
        assertFalse(range.hasLimit());
    }

    @Test
    public void startNegative() throws Exception {
        assertThrows(IllegalArgumentException.class, ()->{
            QueryRange.start(-1L);
        });
    }

    @Test
    public void happyCase_countOnly() throws Exception {

        val range = namedQuery
                .withRange(QueryRange.limit(10L))
                .getRange();

        assertThat(range.getStart(), is(0L));
        assertThat(range.getLimit(), is(10L));

        assertFalse(range.isUnconstrained());
        assertFalse(range.hasOffset());
        assertTrue(range.hasLimit());
    }

    @Test
    public void countNegative() throws Exception {
        assertThrows(IllegalArgumentException.class, ()->{
            QueryRange.limit(-1L);
        });
    }

    @Test
    public void countUnlimited() throws Exception {

        val range = namedQuery
                .withRange(QueryRange.limit(UNLIMITED))
                .getRange();

        assertThat(range.getStart(), is(0L));
        assertThat(range.getLimit(), is(UNLIMITED));

        assertTrue(range.isUnconstrained());
        assertFalse(range.hasOffset());
        assertFalse(range.hasLimit());

    }


}
