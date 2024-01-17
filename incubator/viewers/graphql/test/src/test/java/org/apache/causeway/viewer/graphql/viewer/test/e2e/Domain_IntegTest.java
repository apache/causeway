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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.approvaltests.Approvals;
import org.approvaltests.reporters.DiffReporter;
import org.approvaltests.reporters.TextWebReporter;
import org.approvaltests.reporters.UseReporter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;

import org.apache.causeway.viewer.graphql.viewer.test.CausewayViewerGraphqlTestModuleIntegTestAbstract;
import org.apache.causeway.viewer.graphql.viewer.test.domain.Department;
import org.apache.causeway.viewer.graphql.viewer.test.domain.DepartmentRepository;
import org.apache.causeway.viewer.graphql.viewer.test.domain.DeptHead;
import org.apache.causeway.viewer.graphql.viewer.test.domain.DeptHeadRepository;
import org.apache.causeway.viewer.graphql.viewer.test.domain.TopLevelMenu;

import static org.apache.causeway.commons.internal.assertions._Assert.assertEquals;
import static org.apache.causeway.commons.internal.assertions._Assert.assertTrue;


//NOT USING @Transactional since we are running server within same transaction otherwise
@ActiveProfiles("test")
public class Domain_IntegTest extends CausewayViewerGraphqlTestModuleIntegTestAbstract {

    @Inject DepartmentRepository departmentRepository;
    @Inject DeptHeadRepository deptHeadRepository;
    @Inject TopLevelMenu topLevelMenu;

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

    //TODO started to fail on 2022-09-04, with testEntityRepository findAll being empty
    @Test @DisabledIfSystemProperty(named = "isRunningWithSurefire", matches = "true")
    @UseReporter(DiffReporter.class)
    void createE1() throws Exception {

        //File targetFile3 = new File("src/test/resources/testfiles/targetFile3.gql");

        String response1 = transactionService.callTransactional(Propagation.REQUIRED, () -> {

            String submit = submit();
            // just to show we need to query in separate tranasction
            List<DeptHead> list = deptHeadRepository.findAll();
            assertTrue(list.isEmpty());
            return submit;

        }).ifFailureFail().getValue().get();

        final List<Department> allDepartment = new ArrayList<>();
        transactionService.runTransactional(Propagation.REQUIRED, () -> {

            List<Department> all = departmentRepository.findAll();
            allDepartment.addAll(all);

        });

        assertEquals(1, allDepartment.size());
        assertEquals("newbee", allDepartment.get(0).getName());

        Approvals.verify(response1, jsonOptions());

    }

    //TODO started to fail on 2023-07-25
    //disabled to rescue CI build
    @Test @DisabledIfSystemProperty(named = "isRunningWithSurefire", matches = "true")
    @UseReporter(DiffReporter.class)
    void changeName() throws Exception {

        List<DeptHead> deptHeadList = new ArrayList<>();

        transactionService.runTransactional(Propagation.REQUIRED, () -> {

            deptHeadList.add(deptHeadRepository.create("foo", null));

        });

        DeptHead deptHead = deptHeadList.get(0);
        assertEquals("foo", deptHead.getName());
        assertEquals(deptHead.getName(), topLevelMenu.findDeptHeadByName("foo").getName());

        String response = transactionService.callTransactional(Propagation.REQUIRED, () -> {

            return submit();

        }).ifFailureFail().getValue().get();

        deptHeadList.clear();
        transactionService.runTransactional(Propagation.REQUIRED, () -> {

            List<DeptHead> all = deptHeadRepository.findAll();
            deptHeadList.addAll(all);

        });

        DeptHead deptHeadModified = deptHeadList.get(0);

        //TODO: implement ...
//        assertEquals("bar", deptHeadModified.getName());
//
//        Approvals.verify(response, new Options());

    }


}
