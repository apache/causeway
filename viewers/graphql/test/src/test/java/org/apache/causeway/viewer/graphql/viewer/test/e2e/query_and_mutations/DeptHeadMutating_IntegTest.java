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
package org.apache.causeway.viewer.graphql.viewer.test.e2e.query_and_mutations;

import java.util.Optional;

import org.approvaltests.Approvals;
import org.approvaltests.reporters.DiffReporter;
import org.approvaltests.reporters.UseReporter;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.viewer.graphql.viewer.test.CausewayViewerGraphqlTestModuleIntegTestAbstract;
import org.apache.causeway.viewer.graphql.viewer.test.domain.dept.Department;
import org.apache.causeway.viewer.graphql.viewer.test.e2e.Abstract_IntegTest;

import lombok.val;


//NOT USING @Transactional since we are running server within same transaction otherwise
@SpringBootTest(
        classes = {
                CausewayViewerGraphqlTestModuleIntegTestAbstract.TestApp.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "causeway.viewer.graphql.api-variant=QUERY_AND_MUTATIONS"
        }
)
@Order(120)
@DirtiesContext
@ActiveProfiles("test")
public class DeptHeadMutating_IntegTest extends Abstract_IntegTest {

    @Test
    @UseReporter(DiffReporter.class)
    void create_department() throws Exception {

        // when lookup 'Prof. Dicky Horwich' and change it to 'Prof. Richard Horwich'
        String response = submit();

        // then payload
        Approvals.verify(response, jsonOptions());

    }

    @Test
    @UseReporter(DiffReporter.class)
    void change_department_name() throws Exception {

        final Bookmark bookmark =
                transactionService.callTransactional(
                        Propagation.REQUIRED,
                        () -> {
                            Department department = departmentRepository.findByName("Classics");
                            Optional<Bookmark> bookmark1 = bookmarkService.bookmarkFor(department);
                            return bookmark1.orElseThrow();
                        }
                ).valueAsNonNullElseFail();

        val response = submit(_Maps.unmodifiable("$departmentId", bookmark.getIdentifier()));

        // then payload
        Approvals.verify(response, jsonOptions());
    }

}
