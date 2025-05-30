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
package org.apache.causeway.viewer.graphql.viewer.test.e2e.univ.staff;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.causeway.viewer.graphql.viewer.test.e2e.Abstract_IntegTest;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestFactory;

import org.springframework.test.context.ActiveProfiles;

//NOT USING @Transactional since we are running server within same transaction otherwise
@Order(60)
@ActiveProfiles("test")
public class Staff_IntegTest extends Abstract_IntegTest {

    @Override
    @TestFactory
    public Iterable<DynamicTest> each() throws IOException, URISyntaxException {
        return super.each();
    }

}
