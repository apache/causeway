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

import lombok.val;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.viewer.graphql.viewer.test.domain.StaffMember;
import org.apache.causeway.viewer.graphql.viewer.test.domain.StaffMemberRepository;

import org.approvaltests.Approvals;
import org.approvaltests.reporters.DiffReporter;
import org.approvaltests.reporters.UseReporter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;

import org.apache.causeway.viewer.graphql.viewer.test.CausewayViewerGraphqlTestModuleIntegTestAbstract;
import org.apache.causeway.viewer.graphql.viewer.test.domain.Department;
import org.apache.causeway.viewer.graphql.viewer.test.domain.DepartmentRepository;
import org.apache.causeway.viewer.graphql.viewer.test.domain.DeptHead;
import org.apache.causeway.viewer.graphql.viewer.test.domain.DeptHeadRepository;

import static org.apache.causeway.commons.internal.assertions._Assert.assertEquals;
import static org.apache.causeway.commons.internal.assertions._Assert.assertTrue;


//NOT USING @Transactional since we are running server within same transaction otherwise
@ActiveProfiles("test")
public class Domain_IntegTest extends CausewayViewerGraphqlTestModuleIntegTestAbstract {

    @Inject DepartmentRepository departmentRepository;
    @Inject DeptHeadRepository deptHeadRepository;
    @Inject StaffMemberRepository staffMemberRepository;
    @Inject BookmarkService bookmarkService;

    @BeforeEach
    void beforeEach(){
        transactionService.runTransactional(Propagation.REQUIRED, () -> {

            // departments
            Department classics = departmentRepository.create("Classics", null);
            Department physics = departmentRepository.create("Physics", null); // no head.
            Department textiles = departmentRepository.create("Textiles", null);
            Department pathology = departmentRepository.create("Pathology", null);
            Department mathematics = departmentRepository.create("Mathematics", null);
            Department civilEngineering = departmentRepository.create("Civil Engineering", null);

            // heads
            deptHeadRepository.create("Dr. Barney Jones", classics);
            deptHeadRepository.create("Prof. Dicky Horwich", textiles);
            deptHeadRepository.create("Dr. Susan Hopwood", pathology);
            deptHeadRepository.create("Dr. Helen Johansen", mathematics);
            deptHeadRepository.create("Dr. George Harwood", civilEngineering);

            // staff
            staffMemberRepository.create("Letitia Leadbetter", classics);
            staffMemberRepository.create("Gerry Jones", classics);
            staffMemberRepository.create("Mervin Hughes", physics);
            staffMemberRepository.create("John Gaffney", physics);
            staffMemberRepository.create("Margaret Randall", physics);

        });
    }
    @AfterEach
    void afterEach(){
        transactionService.runTransactional(Propagation.REQUIRED, () -> {
            staffMemberRepository.removeAll();
            deptHeadRepository.removeAll();
            departmentRepository.removeAll();
        });
    }


    @Test
    @UseReporter(DiffReporter.class)
    void find_all_departments() throws Exception {

        // given
        transactionService.runTransactional(Propagation.REQUIRED, () -> {

            departmentRepository.create("foo", null);
            departmentRepository.create("bar", null);
            transactionService.flushTransaction();

            List<Department> allDepartment = departmentRepository.findAll();
            assertThat(allDepartment).hasSize(2);

            List<DeptHead> allDeptHead = deptHeadRepository.findAll();
            assertThat(allDeptHead).isEmpty();
        });

        // when, then
        Approvals.verify(submit(), jsonOptions());
    }

    @Test
    @UseReporter(DiffReporter.class)
    void find_department_by_name() throws Exception {

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
    void find_depthead_by_name() throws Exception {

        // when, then
        Approvals.verify(submit(), jsonOptions());
    }

    @Test
    @UseReporter(DiffReporter.class)
    void create_department() throws Exception {

        // given
        final int numDepartmentsInitially =
                transactionService.callTransactional(
                        Propagation.REQUIRED,
                        () -> departmentRepository.findAll().size()
                ).valueAsNonNullElseFail();

        // when
        String newDepartmentName = "newbie";
        val response = submit();

        // then payload
        Approvals.verify(response, jsonOptions());

        // and also in the database
        final List<Department> allDepartment =
                transactionService.callTransactional(
                        Propagation.REQUIRED,
                        () -> departmentRepository.findAll()
                ).valueAsNonNullElseFail();

        assertThat(allDepartment)
                .hasSize(numDepartmentsInitially + 1)
                .filteredOn(x -> x.getName().equals(newDepartmentName))
                .extracting(Department::getName)
                .first()
                .isEqualTo(newDepartmentName);

        final Department newbieDepartment =
                transactionService.callTransactional(
                        Propagation.REQUIRED,
                        () -> departmentRepository.findByName(newDepartmentName)
                ).valueAsNullableElseFail();

        assertThat(newbieDepartment).isNotNull();
    }

    @Test
    @UseReporter(DiffReporter.class)
    void admin_action() throws Exception {

        // when
        val response = submit();

        // then payload
        Approvals.verify(response, jsonOptions());
    }

    @Test
    @UseReporter(DiffReporter.class)
    void other_admin_action() throws Exception {

        // when
        val response = submit();

        // then payload
        Approvals.verify(response, jsonOptions());
    }

    @Test
    @UseReporter(DiffReporter.class)
    void find_depthead_and_change_name_invalid() throws Exception {

        String response = submit();

        // then payload
        Approvals.verify(response, jsonOptions());
    }

    @Test
    @UseReporter(DiffReporter.class)
    void find_depthead_and_change_name() throws Exception {

        // when lookup 'Prof. Dicky Horwich' and change it to 'Prof. Richard Horwich'
        String response = submit();

        // then payload
        Approvals.verify(response, jsonOptions());

        // and also in the database
        DeptHead deptHeadAfter = transactionService.callTransactional(
                Propagation.REQUIRED,
                () -> deptHeadRepository.findByName("Prof. Richard Horwich")
        ).valueAsNullableElseFail();

        assertThat(deptHeadAfter).isNotNull();
    }

}
