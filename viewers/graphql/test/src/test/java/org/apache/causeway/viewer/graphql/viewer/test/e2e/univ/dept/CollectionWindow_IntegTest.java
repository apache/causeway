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
package org.apache.causeway.viewer.graphql.viewer.test.e2e.univ.dept;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;

import org.apache.causeway.viewer.graphql.viewer.test.domain.dept.Department;
import org.apache.causeway.viewer.graphql.viewer.test.e2e.Abstract_IntegTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Order(41)
@ActiveProfiles("test")
public class CollectionWindow_IntegTest extends Abstract_IntegTest {

    @Override
    @BeforeEach
    protected void beforeEach() {
        super.beforeEach();
        transactionService.runTransactional(Propagation.REQUIRED, () -> {
            var physics = departmentRepository.findByName("Physics");
            IntStream.range(0, 9)
                    .forEach(index -> staffMemberRepository.create("Window %02d".formatted(index), physics));
            staffMemberRepository.create("Window Error", physics);
        });
        Department.resetInstrumentedMaterializationCount();
    }

    @Override
    @TestFactory
    @Order(10)
    public Iterable<DynamicTest> each() throws IOException, URISyntaxException {
        return super.each();
    }

    @Test
    @Order(1)
    void boundedResponseStillMaterializesTheCurrentAssociation() {
        Department.resetInstrumentedMaterializationCount();

        var response = submit("materialization");

        assertTrue(response.contains("\"returnedCount\":2"));
        assertEquals(13, Department.instrumentedMaterializationCount());
    }

    @Test
    @Order(2)
    void oversizedRequestIsRejectedBeforeReadingTheAssociation() {
        Department.resetInstrumentedMaterializationCount();

        var response = submit("oversized");

        assertTrue(response.contains("Collection window size exceeds the configured maximum of 100."));
        assertEquals(0, Department.instrumentedMaterializationCount());
    }

    @Test
    @Order(3)
    void laterRequestReflectsCurrentMembershipWithoutCursorClaims() {
        var before = submit("concurrent-before");
        assertTrue(before.contains("\"totalCount\":13"));

        transactionService.runTransactional(Propagation.REQUIRED, () ->
                staffMemberRepository.create(
                        "Window Added",
                        departmentRepository.findByName("Physics")));

        var after = submit("concurrent-after");
        assertTrue(after.contains("\"totalCount\":14"));
    }
}
