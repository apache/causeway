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

import javax.inject.Inject;

import org.approvaltests.Approvals;
import org.approvaltests.reporters.DiffReporter;
import org.approvaltests.reporters.UseReporter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;

import org.apache.causeway.viewer.graphql.viewer.test.CausewayViewerGraphqlTestModuleIntegTestAbstract;
import org.apache.causeway.viewer.graphql.viewer.test.domain.Department;
import org.apache.causeway.viewer.graphql.viewer.test.domain.DepartmentRepository;
import org.apache.causeway.viewer.graphql.viewer.test.domain.DeptHead;
import org.apache.causeway.viewer.graphql.viewer.test.domain.DeptHeadRepository;
import org.apache.causeway.viewer.graphql.viewer.test.domain.DepartmentMenu;

import static org.apache.causeway.commons.internal.assertions._Assert.assertEquals;
import static org.apache.causeway.commons.internal.assertions._Assert.assertTrue;


//NOT USING @Transactional since we are running server within same transaction otherwise
@ActiveProfiles("test")
public class Domain_IntegTest extends CausewayViewerGraphqlTestModuleIntegTestAbstract {

    @Inject DepartmentRepository departmentRepository;
    @Inject DeptHeadRepository deptHeadRepository;
    @Inject DepartmentMenu departmentMenu;

    @AfterEach
    void afterEach(){
        transactionService.runTransactional(Propagation.REQUIRED, () -> {
            departmentRepository.removeAll();
            deptHeadRepository.removeAll();
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
    void create_department() throws Exception {

        // when
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
                .hasSize(1)
                .element(0)
                .extracting(Department::getName).isEqualTo("newbie");
    }

    @Test
    @Disabled // does not yet call 'changeName' action
    @UseReporter(DiffReporter.class)
    void find_department_and_change_name() throws Exception {

        // given
        transactionService.callTransactional(
                Propagation.REQUIRED,
                () -> deptHeadRepository.create("foo", null)
        ).valueAsNonNullElseFail();

        // when lookup 'foo' and change it to 'bar'
        String response = transactionService.callTransactional(
                Propagation.REQUIRED,
                this::submit
        ).valueAsNonNullElseFail();

        // then payload
        Approvals.verify(response, jsonOptions());

        // and also in the database
        DeptHead deptHeadAfter = transactionService.callTransactional(
                Propagation.REQUIRED,
                () -> deptHeadRepository.findByName("bar")
        ).valueAsNullableElseFail();

        assertThat(deptHeadAfter).isNotNull();
    }

}
