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
package org.apache.causeway.viewer.restfulobjects.test.scenarios;

import javax.inject.Inject;

import org.apache.causeway.viewer.restfulobjects.client.RestfulClient;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;

import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.persistence.jpa.eclipselink.CausewayModulePersistenceJpaEclipselink;
import org.apache.causeway.viewer.restfulobjects.test.CausewayViewerRestfulObjectsIntegTestAbstract;
import org.apache.causeway.viewer.restfulobjects.test.domain.UniversityModule;
import org.apache.causeway.viewer.restfulobjects.test.domain.dom.Department;
import org.apache.causeway.viewer.restfulobjects.test.domain.dom.DepartmentRepository;
import org.apache.causeway.viewer.restfulobjects.test.domain.dom.DeptHeadRepository;
import org.apache.causeway.viewer.restfulobjects.test.domain.dom.StaffMemberRepository;

@Import({
        UniversityModule.class,
        CausewayModulePersistenceJpaEclipselink.class,
})
public abstract class Abstract_IntegTest extends CausewayViewerRestfulObjectsIntegTestAbstract {

    @Inject protected DepartmentRepository departmentRepository;
    @Inject protected DeptHeadRepository deptHeadRepository;
    @Inject protected StaffMemberRepository staffMemberRepository;
    @Inject protected BookmarkService bookmarkService;

    protected RestfulClient restfulClient;

    protected Abstract_IntegTest(Class<?> resourceBaseClazz) {
        super(resourceBaseClazz);
    }

    protected Abstract_IntegTest() {
        this(Abstract_IntegTest.class);
    }

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

        restfulClient = restfulClient();
    }

    @Override
    @AfterEach
    protected void afterEach(){
        restfulClient.close();

        transactionService.runTransactional(Propagation.REQUIRED, () -> {
            staffMemberRepository.removeAll();
            deptHeadRepository.findAll().forEach(x -> x.setDepartment(null));
            departmentRepository.findAll().forEach(x -> x.setDeptHead(null));
            deptHeadRepository.removeAll();
            departmentRepository.removeAll();
        });
    }

}
