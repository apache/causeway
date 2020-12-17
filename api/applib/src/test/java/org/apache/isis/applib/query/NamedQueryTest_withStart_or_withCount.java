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
package org.apache.isis.applib.query;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NamedQueryTest_withStart_or_withCount {

    private NamedQuery<Customer> namedQuery;


    static class Customer {}

    @BeforeEach
    void setUp() throws Exception {
        namedQuery = Query.named(Customer.class, "findByLastName")
                .withParameter("lastName", "Smith");
    }

    @Test
    public void defaults() throws Exception {
        assertThat(namedQuery.getStart(), is(0L));
        assertThat(namedQuery.getCount(), is(0L));
    }

    @Test
    public void typicalHappyCase() throws Exception {
        final Query<Customer> q = namedQuery
                .withStart(10L)
                .withCount(5L);

        assertThat(q.getStart(), is(10L));
        assertThat(q.getCount(), is(5L));
    }

    @Test
    public void happyCase_startOnly() throws Exception {
        final NamedQuery<Customer> q = namedQuery.withStart(10L);

        assertThat(q.getStart(), is(10L));
        assertThat(q.getCount(), is(0L));
    }

    @Test
    public void happyCase_startZero() throws Exception {
        final NamedQuery<Customer> q = namedQuery.withStart(0);

        assertThat(q.getStart(), is(0L));
    }

    @Test
    public void startNegative() throws Exception {
        assertThrows(IllegalArgumentException.class, ()->{
            namedQuery.withStart(-1);
        });
    }

    @Test
    public void happyCase_countOnly() throws Exception {
        final NamedQuery<Customer> q = namedQuery.withCount(20L);

        assertThat(q, is(namedQuery));
        assertThat(q.getStart(), is(0L));
        assertThat(q.getCount(), is(20L));
    }

    @Test
    public void countNegative() throws Exception {
        assertThrows(IllegalArgumentException.class, ()->{
            namedQuery.withCount(-1);
        });
    }

    @Test
    public void countZero() throws Exception {
        assertThrows(IllegalArgumentException.class, ()->{
            namedQuery.withCount(0);
        });
    }


}
