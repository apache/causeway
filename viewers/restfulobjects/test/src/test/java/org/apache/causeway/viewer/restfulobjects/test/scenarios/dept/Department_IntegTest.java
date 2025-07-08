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
package org.apache.causeway.viewer.restfulobjects.test.scenarios.dept;

import org.approvaltests.Approvals;
import org.approvaltests.reporters.DiffReporter;
import org.approvaltests.reporters.UseReporter;
import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.viewer.restfulobjects.test.domain.dom.Department;
import org.apache.causeway.viewer.restfulobjects.test.scenarios.Abstract_IntegTest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class Department_IntegTest extends Abstract_IntegTest {

    @Test
    @UseReporter(DiffReporter.class)
    void exists() {

        // given
        Bookmark bookmark = transactionService.callTransactional(Propagation.REQUIRED, () -> {
            Department classics = departmentRepository.findByName("Classics");
            return bookmarkService.bookmarkFor(classics).orElseThrow();
        }).valueAsNonNullElseFail();

        // when
        var response = restGetJson("/objects/%s/%s".formatted(bookmark.logicalTypeName(), bookmark.identifier()), log);

        // then
        var entity = response.body(String.class);
        Approvals.verify(entity, jsonOptions());
    }

    @Test
    @UseReporter(DiffReporter.class)
    void collection_with_staff_members() {

        // given
        Bookmark bookmark = transactionService.callTransactional(Propagation.REQUIRED, () -> {
            Department classics = departmentRepository.findByName("Classics");
            return bookmarkService.bookmarkFor(classics).orElseThrow();
        }).valueAsNonNullElseFail();

        // when
        var response = restGetJson("/objects/%s/%s/collections/staffMembers".formatted(bookmark.logicalTypeName(), bookmark.identifier()), log);

        // then
        var entity = response.body(String.class);
        Approvals.verify(entity, jsonOptions());
    }

    @Test
    @UseReporter(DiffReporter.class)
    void collection_with_no_staff_members() {

        // given
        Bookmark bookmark = transactionService.callTransactional(Propagation.REQUIRED, () -> {
            Department classics = departmentRepository.findByName("Textiles");
            return bookmarkService.bookmarkFor(classics).orElseThrow();
        }).valueAsNonNullElseFail();

        // when
        var response = restGetJson("/objects/%s/%s/collections/staffMembers".formatted(bookmark.logicalTypeName(), bookmark.identifier()), log);

        // then
        var entity = response.body(String.class);
        Approvals.verify(entity, jsonOptions());
    }

    @Test
    @UseReporter(DiffReporter.class)
    void does_not_exist() {

        // when
        var response = restClient(log).build()
            .get()
            .uri("/objects/university.dept.Department/9999999")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus(assertStatusNotFoundResponseErrorHandler());

        // then
        var entity = response.body(String.class);
        Approvals.verify(entity, jsonOptions());
    }

}
