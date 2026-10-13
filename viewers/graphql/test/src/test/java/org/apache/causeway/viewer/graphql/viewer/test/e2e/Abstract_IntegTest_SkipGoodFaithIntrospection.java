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
package org.apache.causeway.viewer.graphql.viewer.test.e2e;

import graphql.introspection.GoodFaithIntrospection;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

/**
 * WORKAROUND
 * <p>Disables GoodFaithIntrospection as it complains about SimpleAndRich.__type being present in test queries more than once.
 * <p>Can be removed once the root cause is fixed.
 */
public abstract class Abstract_IntegTest_SkipGoodFaithIntrospection extends Abstract_IntegTest {

    @BeforeAll
    static void beforeAll() {
        GoodFaithIntrospection.enabledJvmWide(false);
    }

    @AfterAll
    static void afterAll() {
        GoodFaithIntrospection.enabledJvmWide(true);
    }

}
