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
package org.apache.causeway.viewer.graphql.viewer.test.e2e.query;

import org.apache.causeway.viewer.graphql.viewer.test.e2e.Abstract_IntegTest;

import org.approvaltests.Approvals;
import org.approvaltests.reporters.DiffReporter;
import org.approvaltests.reporters.UseReporter;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import org.springframework.test.context.ActiveProfiles;


//NOT USING @Transactional since we are running server within same transaction otherwise
@Order(30)
@ActiveProfiles("test")
public class Calculator_IntegTest extends Abstract_IntegTest {

    @Test
    @UseReporter(DiffReporter.class)
    void add_integers() throws Exception {

        // when, then
        Approvals.verify(submit(), jsonOptions());
    }

    @Test
    @UseReporter(DiffReporter.class)
    void add_integer_wrappers() throws Exception {

        // when, then
        Approvals.verify(submit(), jsonOptions());
    }

    @Test
    @UseReporter(DiffReporter.class)
    void add_doubles() throws Exception {

        // when, then
        Approvals.verify(submit(), jsonOptions());
    }

    @Test
    @UseReporter(DiffReporter.class)
    void add_double_wrappers() throws Exception {

        // when, then
        Approvals.verify(submit(), jsonOptions());
    }

    @Test
    @UseReporter(DiffReporter.class)
    void add_floats() throws Exception {

        // when, then
        Approvals.verify(submit(), jsonOptions());
    }

    @Test
    @UseReporter(DiffReporter.class)
    void add_float_wrappers() throws Exception {

        // when, then
        Approvals.verify(submit(), jsonOptions());
    }

    @Test
    @UseReporter(DiffReporter.class)
    void add_big_integers() throws Exception {

        // when, then
        Approvals.verify(submit(), jsonOptions());
    }

    @Test
    @UseReporter(DiffReporter.class)
    void add_big_decimals() throws Exception {

        // when, then
        Approvals.verify(submit(), jsonOptions());
    }

    @Test
    @UseReporter(DiffReporter.class)
    void plus_days() throws Exception {

        // when, then
        Approvals.verify(submit(), jsonOptions());
    }

    @Test
    @UseReporter(DiffReporter.class)
    void plus_joda_days() throws Exception {

        // when, then
        Approvals.verify(submit(), jsonOptions());
    }

    @Test
    @UseReporter(DiffReporter.class)
    void boolean_and_1() throws Exception {

        // when, then
        Approvals.verify(submit(), jsonOptions());

    }

    @Test
    @UseReporter(DiffReporter.class)
    void boolean_and_2() throws Exception {

        // when, then
        Approvals.verify(submit(), jsonOptions());
    }

    @Test
    @UseReporter(DiffReporter.class)
    void boolean_or_1() throws Exception {

        // when, then
        Approvals.verify(submit(), jsonOptions());

    }

    @Test
    @UseReporter(DiffReporter.class)
    void boolean_or_2() throws Exception {

        // when, then
        Approvals.verify(submit(), jsonOptions());

    }

    @Test
    @UseReporter(DiffReporter.class)
    void boolean_not() throws Exception {

        // when, then
        Approvals.verify(submit(), jsonOptions());

    }

    @Test
    @UseReporter(DiffReporter.class)
    void next_month() throws Exception {

        // when, then
        Approvals.verify(submit(), jsonOptions());

    }

    @Test
    @UseReporter(DiffReporter.class)
    void concat() throws Exception {

        // when, then
        Approvals.verify(submit(), jsonOptions());

    }


}
