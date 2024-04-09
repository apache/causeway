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

import java.io.IOException;
import java.util.Base64;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.Response;

import com.google.gson.GsonBuilder;

import org.approvaltests.Approvals;
import org.approvaltests.reporters.DiffReporter;
import org.approvaltests.reporters.UseReporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.transaction.annotation.Propagation;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.commons.io.DataSource;
import org.apache.causeway.viewer.restfulobjects.test.scenarios.Abstract_IntegTest;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;

public class Staff_lowlevel_v2_IntegTest extends Abstract_IntegTest {

    private GsonBuilder gsonBuilder;

    @BeforeEach
    void setup() {
        gsonBuilder = new GsonBuilder();
    }

    @SneakyThrows
    @Test
    @UseReporter(DiffReporter.class)
    public void createStaffMemberWithPhoto2() {

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

        String departmentHref = asRelativeHref(departmentBookmark);
        Invocation.Builder departmentRequest = restfulClient.request(departmentHref);
        Response departmentResponse = departmentRequest.get();
        assertThat(departmentResponse.getStatusInfo().getFamily()).isEqualTo(Response.Status.Family.SUCCESSFUL);

        // and given
        final var photoEncoded = readFileAsBlob("StaffMember-photo-Bar.pdf");

        // when create request
        final var requestBuilder = restfulClient.request("services/university.dept.Staff/actions/createStaffMemberWithPhoto2/invoke");

        final var body = new Body(staffName, asAbsoluteHref(departmentBookmark), photoEncoded);
        final var bodyJson = gsonBuilder.create().toJson(body);

        // then
        Approvals.verify(bodyJson, jsonOptions());

        // and when send request
        val response = requestBuilder.post(Entity.entity(bodyJson, "application/json"));

        // then
        val entity = response.readEntity(String.class);
        assertThat(response.getStatusInfo().getFamily()).isEqualTo(Response.Status.Family.SUCCESSFUL);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        // and also json response

        // and also object is created in database
        final var bookmarkAfterIfAny = transactionService.callTransactional(Propagation.REQUIRED, () -> {
            final var staffMember = staffMemberRepository.findByName(staffName);
            return bookmarkService.bookmarkFor(staffMember);
        }).valueAsNonNullElseFail();
        assertThat(bookmarkAfterIfAny).isNotEmpty();
    }

    private String asAbsoluteHref(final Bookmark bookmark) {
        return String.format("%s%s", restfulClient.getConfig().getRestfulBaseUrl(), asRelativeHref(bookmark));
    }

    private String asRelativeHref(final Bookmark bookmark) {
        return String.format("objects/%s/%s", bookmark.getLogicalTypeName(), bookmark.getIdentifier());
    }

    private Blob readFileAsBlob(final String fileName) throws IOException {
        var bytes = DataSource.ofResource(Abstract_IntegTest.class, fileName)
                .bytes();
        return new Blob(fileName, "application/pdf", bytes);
    }


    @Getter
    static class Body {

        /**
         * @param nameValue
         * @param departmentHrefValue
         * @param blob - is the Blob to be formatted
         */
        Body(final String nameValue, final String departmentHrefValue, final org.apache.causeway.applib.value.Blob blob) {
            name = new Name(nameValue);
            department = new Department(new Department.Value(departmentHrefValue));
            photo = new Blob(new Blob.Value(blob.getName(), blob.getMimeType().toString(), Base64.getEncoder().encodeToString(blob.getBytes())));
        }

        private Name name;
        private Department department;
        private Blob photo;

        @lombok.Value
        static class Name {
            private String value;
        }

        @lombok.Value
        static class Department {
            private Value value;

            @lombok.Value
            static class Value {
                private String href;
            }

        }


        @lombok.Value
        static class Blob {
            private Value value;

            @lombok.Value
            static class Value {
                private String name;
                private String mimeType;
                private String bytes;
            }
        }

    }

}


