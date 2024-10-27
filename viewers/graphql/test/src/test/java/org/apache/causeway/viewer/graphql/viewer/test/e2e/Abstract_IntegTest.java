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

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Propagation;

import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.persistence.jpa.eclipselink.CausewayModulePersistenceJpaEclipselink;
import org.apache.causeway.viewer.graphql.viewer.test.domain.UniversityModule;
import org.apache.causeway.viewer.graphql.viewer.test.domain.dept.Department;
import org.apache.causeway.viewer.graphql.viewer.test.domain.dept.DepartmentRepository;
import org.apache.causeway.viewer.graphql.viewer.test.domain.dept.DeptHeadRepository;
import org.apache.causeway.viewer.graphql.viewer.test.domain.dept.StaffMemberRepository;
import org.apache.causeway.viewer.graphql.viewer.testsupport.CausewayViewerGraphqlIntegTestAbstract;

@Import({
        UniversityModule.class,
        CausewayModulePersistenceJpaEclipselink.class,
})
public abstract class Abstract_IntegTest extends CausewayViewerGraphqlIntegTestAbstract {

    protected Abstract_IntegTest() {
        super(Abstract_IntegTest.class);
    }

    @DynamicPropertySource
    static void apiVariant(final DynamicPropertyRegistry registry) {
        registry.add("causeway.viewer.graphql.api-scope", CausewayConfiguration.Viewer.Graphql.ApiScope.ALL::name);
        registry.add("causeway.viewer.graphql.api-variant", CausewayConfiguration.Viewer.Graphql.ApiVariant.QUERY_WITH_MUTATIONS_NON_SPEC_COMPLIANT::name);
        registry.add("causeway.viewer.graphql.schema.rich.enable-scenario-testing", () -> Boolean.TRUE);
        registry.add("causeway.viewer.graphql.resources.response-type", CausewayConfiguration.Viewer.Graphql.ResponseType.ATTACHMENT::name);
    }

    @Inject protected DepartmentRepository departmentRepository;
    @Inject protected DeptHeadRepository deptHeadRepository;
    @Inject protected StaffMemberRepository staffMemberRepository;
    @Inject protected BookmarkService bookmarkService;

    @Override
    @BeforeEach
    protected void beforeEach(){
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
            staffMemberRepository.create("Letitia Leadbetter", classics, asPdfBlob("StaffMember-photo-Foo.pdf"));
            staffMemberRepository.create("Gerry Jones", classics, asPdfBlob("StaffMember-photo-Bar.pdf"));
            staffMemberRepository.create("Mervin Hughes", physics, asPdfBlob("StaffMember-photo-Fizz.pdf"));
            staffMemberRepository.create("John Gartner", physics);
            staffMemberRepository.create("Margaret Randall", physics);

        });
    }

    @Override
    @AfterEach
    protected void afterEach(){
        transactionService.runTransactional(Propagation.REQUIRED, () -> {
            staffMemberRepository.removeAll();
            deptHeadRepository.findAll().forEach(x -> x.setDepartment(null));
            departmentRepository.findAll().forEach(x -> x.setDeptHead(null));
            deptHeadRepository.removeAll();
            departmentRepository.removeAll();
        });
    }

}
