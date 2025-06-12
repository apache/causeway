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

import jakarta.ws.rs.core.Response;

import org.approvaltests.Approvals;
import org.approvaltests.reporters.DiffReporter;
import org.approvaltests.reporters.UseReporter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.springframework.transaction.annotation.Propagation;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.commons.io.DataSource;
import org.apache.causeway.viewer.restfulobjects.test.domain.dom.Department;
import org.apache.causeway.viewer.restfulobjects.test.scenarios.Abstract_IntegTest;

import lombok.SneakyThrows;

class Staff_hilevel_IntegTest extends Abstract_IntegTest {

    @Test
    @UseReporter(DiffReporter.class)
    @SneakyThrows
    void createStaffMemberWithPhoto() {

        // given
        final var staffName = "Fred Smith";

        final var bookmarkBeforeIfAny = transactionService.callTransactional(Propagation.REQUIRED, () -> {
            final var staffMember = staffMemberRepository.findByName(staffName);
            return bookmarkService.bookmarkFor(staffMember);
        }).valueAsNonNullElseFail();

        assertThat(bookmarkBeforeIfAny).isEmpty();

        final Blob photo = readFileAsBlob("StaffMember-photo-Bar.pdf");
        final var requestBuilder = restfulClient.request("services/university.dept.Staff/actions/createStaffMemberWithPhoto/invoke");

        /*
         *  String name,
         *  Department.SecondaryKey departmentSecondaryKey,
         *  Blob photo
         */
        var args = restfulClient.arguments()
                .addActionParameter("name", staffName)
                .addActionParameter("departmentSecondaryKey", Department.SecondaryKey.class, new Department.SecondaryKey("Classics"))
                .addActionParameter("photo", photo)
                .build();

        Approvals.verify(args.getEntity(), jsonOptions());

        // when
        var response = requestBuilder.post(args);

        // then
        assertThat(response.getStatusInfo().getFamily()).isEqualTo(Response.Status.Family.SUCCESSFUL);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        // and also JSON response
        assertResponseOK(response);
        var entity = response.readEntity(String.class);
        assertNotNull(entity);

        // and also object is created in database
        final var bookmarkAfterIfAny = transactionService.callTransactional(Propagation.REQUIRED, () -> {
            final var staffMember = staffMemberRepository.findByName(staffName);
            return bookmarkService.bookmarkFor(staffMember);
        }).valueAsNonNullElseFail();
        assertThat(bookmarkAfterIfAny).isNotEmpty();
    }

    @Test
    @UseReporter(DiffReporter.class)
    @SneakyThrows
    void createStaffMemberWithPhoto_using_map() {

        // given
        final var staffName = "Fred Smith";

        final var bookmarkBeforeIfAny = transactionService.callTransactional(Propagation.REQUIRED, () -> {
            final var staffMember = staffMemberRepository.findByName(staffName);
            return bookmarkService.bookmarkFor(staffMember);
        }).valueAsNonNullElseFail();

        assertThat(bookmarkBeforeIfAny).isEmpty();

        final Blob photo = readFileAsBlob("StaffMember-photo-Bar.pdf");
        final var requestBuilder = restfulClient.request("services/university.dept.Staff/actions/createStaffMemberWithPhoto/invoke");

        var args = restfulClient.arguments()
                .addActionParameter("name", staffName)
                .addActionParameter("departmentSecondaryKey", Map.of("name", "Classics"))
                .addActionParameter("photo", photo)
                .build();

        Approvals.verify(args.getEntity(), jsonOptions());

        // when
        var response = requestBuilder.post(args);

        // then
        assertThat(response.getStatusInfo().getFamily()).isEqualTo(Response.Status.Family.SUCCESSFUL);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        // and also json response
        assertResponseOK(response);
        var entity = response.readEntity(String.class);
        assertNotNull(entity);

        // and also object is created in database
        final var bookmarkAfterIfAny = transactionService.callTransactional(Propagation.REQUIRED, () -> {
            final var staffMember = staffMemberRepository.findByName(staffName);
            return bookmarkService.bookmarkFor(staffMember);
        }).valueAsNonNullElseFail();
        assertThat(bookmarkAfterIfAny).isNotEmpty();
    }

    @Test
    @UseReporter(DiffReporter.class)
    @SneakyThrows
    void createStaffMemberWithPhoto2() {

        // given
        final var staffName = "Fred Smith";

        final var bookmarkBeforeIfAny = transactionService.callTransactional(Propagation.REQUIRED, () -> {
            final var staffMember = staffMemberRepository.findByName(staffName);
            return bookmarkService.bookmarkFor(staffMember);
        }).valueAsNonNullElseFail();

        assertThat(bookmarkBeforeIfAny).isEmpty();

        // and given
        final var departmentName = "Classics";
        final var departmentBookmark = transactionService.callTransactional(Propagation.REQUIRED, () -> {
            final var staffMember = departmentRepository.findByName(departmentName);
            return bookmarkService.bookmarkFor(staffMember).orElseThrow();
        }).valueAsNonNullElseFail();

        // and given
        final Blob photo = readFileAsBlob("StaffMember-photo-Bar.pdf");
        final var requestBuilder = restfulClient.request("services/university.dept.Staff/actions/createStaffMemberWithPhoto2/invoke");

        var args = restfulClient.arguments()
                .addActionParameter("name", staffName)
                .addActionParameter("department", departmentBookmark)
                .addActionParameter("photo", photo)
                .build();

        Approvals.verify(args.getEntity(), jsonOptions());

        // when
        var response = requestBuilder.post(args);

        // then
        assertThat(response.getStatusInfo().getFamily()).isEqualTo(Response.Status.Family.SUCCESSFUL);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        // and also json response
        assertResponseOK(response);
        var entity = response.readEntity(String.class);
        assertNotNull(entity);

        // and also object is created in database
        final var bookmarkAfterIfAny = transactionService.callTransactional(Propagation.REQUIRED, () -> {
            final var staffMember = staffMemberRepository.findByName(staffName);
            return bookmarkService.bookmarkFor(staffMember);
        }).valueAsNonNullElseFail();
        assertThat(bookmarkAfterIfAny).isNotEmpty();
    }

    @Test
    @UseReporter(DiffReporter.class)
    @SneakyThrows
    void createStaffMemberWithPhoto2_using_map() {

        // given
        final var staffName = "Fred Smith";

        final var bookmarkBeforeIfAny = transactionService.callTransactional(Propagation.REQUIRED, () -> {
            final var staffMember = staffMemberRepository.findByName(staffName);
            return bookmarkService.bookmarkFor(staffMember);
        }).valueAsNonNullElseFail();

        assertThat(bookmarkBeforeIfAny).isEmpty();

        // and given
        final var departmentName = "Classics";
        final var departmentBookmark = transactionService.callTransactional(Propagation.REQUIRED, () -> {
            final var staffMember = departmentRepository.findByName(departmentName);
            return bookmarkService.bookmarkFor(staffMember).orElseThrow();
        }).valueAsNonNullElseFail();

        // and given
        final Blob photo = readFileAsBlob("StaffMember-photo-Bar.pdf");
        final var requestBuilder = restfulClient.request("services/university.dept.Staff/actions/createStaffMemberWithPhoto2/invoke");

        /*
         *  String name,
         *  Department.SecondaryKey departmentSecondaryKey,
         *  Blob photo
         */
        var args = restfulClient.arguments()
                .addActionParameter("name", staffName)
                .addActionParameter("department", Map.of("href", asAbsoluteHref(departmentBookmark)))
                .addActionParameter("photo", photo)
                .build();

        Approvals.verify(args.getEntity(), jsonOptions());

        // when
        var response = requestBuilder.post(args);

        // then
        assertThat(response.getStatusInfo().getFamily()).isEqualTo(Response.Status.Family.SUCCESSFUL);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        // and also json response
        assertResponseOK(response);
        var entity = response.readEntity(String.class);
        assertNotNull(entity);

        // and also object is created in database
        final var bookmarkAfterIfAny = transactionService.callTransactional(Propagation.REQUIRED, () -> {
            final var staffMember = staffMemberRepository.findByName(staffName);
            return bookmarkService.bookmarkFor(staffMember);
        }).valueAsNonNullElseFail();
        assertThat(bookmarkAfterIfAny).isNotEmpty();
    }

    private Blob readFileAsBlob(final String fileName) {
        var bytes = DataSource.ofResource(Abstract_IntegTest.class, fileName)
                .bytes();
        return Blob.of(fileName, CommonMimeType.PDF, bytes);
    }

    private String asAbsoluteHref(final Bookmark bookmark) {
        return String.format("%s%s", restfulClient.getConfig().getRestfulBaseUrl(), asRelativeHref(bookmark));
    }

    private String asRelativeHref(final Bookmark bookmark) {
        return String.format("objects/%s/%s", bookmark.logicalTypeName(), bookmark.identifier());
    }

}
