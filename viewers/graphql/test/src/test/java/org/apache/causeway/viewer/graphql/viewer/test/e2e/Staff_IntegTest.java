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

import java.util.Optional;

import org.approvaltests.Approvals;
import org.approvaltests.reporters.DiffReporter;
import org.approvaltests.reporters.UseReporter;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.viewer.graphql.viewer.test.domain.dept.Department;
import org.apache.causeway.viewer.graphql.viewer.test.domain.dept.StaffMember;

import lombok.val;


//NOT USING @Transactional since we are running server within same transaction otherwise
@Order(60)
@ActiveProfiles("test")
public class Staff_IntegTest extends Abstract_IntegTest {

    @Test
    @UseReporter(DiffReporter.class)
    void list_all_staff_members() throws Exception {

        // when, then
        Approvals.verify(submit(), jsonOptions());
    }

    @Test
    @UseReporter(DiffReporter.class)
    void find_staff_member_by_name_and_edit_grade_choices() throws Exception {

        // when, then
        Approvals.verify(submit(), jsonOptions());
    }

    @Test
    @UseReporter(DiffReporter.class)
    void find_staff_member_by_name_and_edit() throws Exception {

        // given
        final Optional<Bookmark> bookmarkIfAny =
                transactionService.callTransactional(
                        Propagation.REQUIRED,
                        () -> {
                            StaffMember pojo = staffMemberRepository.findByName("Gerry Jones");
                            return bookmarkService.bookmarkFor(pojo);
                        }
                ).valueAsNullableElseFail();

        assertThat(bookmarkIfAny).isPresent();

        // when, then
        Approvals.verify(submit(), jsonOptions());

        // and in the database...
        final Optional<StaffMember> staffMemberIfAny =
                transactionService.callTransactional(
                        Propagation.REQUIRED,
                        () -> bookmarkService.lookup(bookmarkIfAny.get(), StaffMember.class)
                ).valueAsNullableElseFail();

        assertThat(staffMemberIfAny).isPresent();
        assertThat(staffMemberIfAny.get()).extracting(StaffMember::getName).isEqualTo("Gerald Johns");
    }

    @Test
    @UseReporter(DiffReporter.class)
    void staff_member_name_validate() throws Exception {

        // when, then
        Approvals.verify(submit(), jsonOptions());
    }

    @Test
    @UseReporter(DiffReporter.class)
    void staff_member_name_edit_invalid() throws Exception {

        // when, then
        Approvals.verify(submit(), jsonOptions());
    }

    @Test
    @UseReporter(DiffReporter.class)
    void create_staff_member_with_department() throws Exception {

        final Bookmark bookmark =
                transactionService.callTransactional(
                        Propagation.REQUIRED,
                        () -> {
                            Department department = departmentRepository.findByName("Classics");
                            return bookmarkService.bookmarkFor(department).orElseThrow();
                        }
                ).valueAsNonNullElseFail();

        val response = submit(_Maps.unmodifiable("$departmentId", bookmark.getIdentifier()));

        // then payload
        Approvals.verify(response, jsonOptions());

    }
}
