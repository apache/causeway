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
package org.apache.causeway.viewer.restfulobjects.test.scenarios.staff;

import java.util.Map;

import org.approvaltests.Approvals;
import org.approvaltests.reporters.DiffReporter;
import org.approvaltests.reporters.UseReporter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.springframework.transaction.annotation.Propagation;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.commons.io.DataSource;
import org.apache.causeway.viewer.restfulobjects.applib.client.ActionParameterModel;
import org.apache.causeway.viewer.restfulobjects.test.domain.dom.Department;
import org.apache.causeway.viewer.restfulobjects.test.scenarios.Abstract_IntegTest;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class Staff_IntegTest extends Abstract_IntegTest {

    final String staffName = "Fred Smith";

    interface Scenario {
        String uri();
    }

    @RequiredArgsConstructor
    enum Basic implements Scenario {
        DEPARTMENT_KEY_AS_VALUE,
        DEPARTMENT_KEY_AS_MAP;
        @Override public String uri() { return "services/university.dept.Staff/actions/createStaffMemberWithPhoto/invoke"; }
    }

    enum Bookmarked implements Scenario {
        DEPARTMENT_BOOKMARK_AS_VALUE,
        DEPARTMENT_BOOKMARK_AS_MAP;
        @Override public String uri() { return "services/university.dept.Staff/actions/createStaffMemberWithPhoto2/invoke"; }
    }

    @ParameterizedTest
    @EnumSources({
        @EnumSource(Basic.class),
        @EnumSource(Bookmarked.class)
    })
    @UseReporter(DiffReporter.class)
    @SneakyThrows
    void createStaffMemberWithPhoto(final Scenario scenario) {

        prolog();

        // given
        final Blob photo = readFileAsBlob("StaffMember-photo-Bar.pdf");

        var argModel = ActionParameterModel.create(baseUrl())
            .addActionParameter("name", staffName);

        if(scenario instanceof Basic basic) {
            argModel = switch(basic) {
                case DEPARTMENT_KEY_AS_VALUE->
                    argModel.addActionParameter("departmentSecondaryKey", Department.SecondaryKey.class, new Department.SecondaryKey("Classics"));
                case DEPARTMENT_KEY_AS_MAP->
                    argModel.addActionParameter("departmentSecondaryKey", Map.of("name", "Classics"));
            };
        } else if(scenario instanceof Bookmarked bookmarked) {
            var bookmark = departmentBookmark("Classics");
            argModel = switch(bookmarked) {
                case DEPARTMENT_BOOKMARK_AS_VALUE->
                    argModel.addActionParameter("department", bookmark);
                case DEPARTMENT_BOOKMARK_AS_MAP->
                    argModel.addActionParameter("department", Map.of("href", asAbsoluteHref(bookmark)));
            };
        }
        argModel = argModel.addActionParameter("photo", photo);

        Approvals.settings().allowMultipleVerifyCallsForThisMethod();
        Approvals.verify(argModel.toJson(), jsonOptions(Approvals.NAMES.withParameters(scenario.toString())));

        // when
        final var response = restClient(log).build()
            .post()
            .uri(scenario.uri())
            .body(argModel.toJson())
            .retrieve()
            .onStatus(assertStatusOkResponseErrorHandler());

        // then
        var entity = response.body(String.class);
        assertNotNull(entity);

        Thread.sleep(2000);

        epilog();
    }

    // -- HELPER

    void prolog() {
        final var bookmarkBeforeIfAny = transactionService.callTransactional(Propagation.REQUIRED, () -> {
            final var staffMember = staffMemberRepository.findByName(staffName);
            return bookmarkService.bookmarkFor(staffMember);
        }).valueAsNonNullElseFail();

        assertThat(bookmarkBeforeIfAny).isEmpty();
    }

    void epilog() {
        // and also object is created in database
        final var bookmarkAfterIfAny = transactionService.callTransactional(Propagation.REQUIRED, () -> {
            final var staffMember = staffMemberRepository.findByName(staffName);
            return bookmarkService.bookmarkFor(staffMember);
        }).valueAsNonNullElseFail();
        assertThat(bookmarkAfterIfAny).isNotEmpty();
    }

    Bookmark departmentBookmark(final String departmentName) {
        return transactionService.callTransactional(Propagation.REQUIRED, () -> {
            final var staffMember = departmentRepository.findByName(departmentName);
            return bookmarkService.bookmarkFor(staffMember).orElseThrow();
        }).valueAsNonNullElseFail();
    }

    private Blob readFileAsBlob(final String fileName) {
        var bytes = DataSource.ofResource(Abstract_IntegTest.class, fileName)
                .bytes();
        return Blob.of(fileName, CommonMimeType.PDF, bytes);
    }

    private String asAbsoluteHref(final Bookmark bookmark) {
        return String.format("%s%s", baseUrl(), asRelativeHref(bookmark));
    }

    private String asRelativeHref(final Bookmark bookmark) {
        return String.format("objects/%s/%s", bookmark.logicalTypeName(), bookmark.identifier());
    }

}
