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
package org.apache.causeway.viewer.graphql.viewer.test.e2e.univ.staffmutating;

import org.apache.causeway.viewer.graphql.viewer.test.e2e.Abstract_IntegTest;

import org.approvaltests.Approvals;
import org.approvaltests.reporters.DiffReporter;
import org.approvaltests.reporters.UseReporter;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.viewer.graphql.viewer.test.domain.dept.StaffMember;

// NOT USING @Transactional since we are running server within same transaction otherwise
@Order(130)
@ActiveProfiles("test")
public class StaffMutating_IntegTest extends Abstract_IntegTest {

    @Test
    @UseReporter(DiffReporter.class)
    void staff_member_edit_name() throws Exception {

        final Bookmark bookmark =
                transactionService.callTransactional(
                        Propagation.REQUIRED,
                        () -> {
                            StaffMember staffMember = staffMemberRepository.findByName("John Gartner");
                            return bookmarkService.bookmarkFor(staffMember).orElseThrow();
                        }
                ).valueAsNonNullElseFail();

        var response = submit(_Maps.unmodifiable("$staffMemberId", bookmark.getIdentifier()));

        // then payload
        Approvals.verify(response, jsonOptions());
    }
}
