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

import lombok.SneakyThrows;
import lombok.val;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.inject.Inject;

import org.apache.causeway.applib.value.Blob;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.Propagation;

import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.viewer.graphql.viewer.test.CausewayViewerGraphqlTestModuleIntegTestAbstract;
import org.apache.causeway.viewer.graphql.viewer.test.domain.dept.Department;
import org.apache.causeway.viewer.graphql.viewer.test.domain.dept.DepartmentRepository;
import org.apache.causeway.viewer.graphql.viewer.test.domain.dept.DeptHeadRepository;
import org.apache.causeway.viewer.graphql.viewer.test.domain.dept.StaffMemberRepository;


public abstract class Abstract_IntegTest extends CausewayViewerGraphqlTestModuleIntegTestAbstract {

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
            staffMemberRepository.create("Letitia Leadbetter", classics, asBlob("Foo.pdf"));
            staffMemberRepository.create("Gerry Jones", classics, asBlob("Bar.pdf"));
            staffMemberRepository.create("Mervin Hughes", physics, asBlob("Fizz.pdf"));
            staffMemberRepository.create("John Gartner", physics);
            staffMemberRepository.create("Margaret Randall", physics);

        });
    }

    private Blob asBlob(String fileName) {
        val bytes = toBytes(fileName);
        return new Blob(fileName, "application/pdf", bytes);
    }

    @AfterEach
    void afterEach(){
        transactionService.runTransactional(Propagation.REQUIRED, () -> {
            staffMemberRepository.removeAll();
            deptHeadRepository.findAll().forEach(x -> x.setDepartment(null));
            departmentRepository.findAll().forEach(x -> x.setDeptHead(null));
            deptHeadRepository.removeAll();
            departmentRepository.removeAll();
        });
    }

    @SneakyThrows
    private byte[] toBytes(String fileName){
        InputStream inputStream = new ClassPathResource(fileName, getClass()).getInputStream();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        return buffer.toByteArray();
    }


}
