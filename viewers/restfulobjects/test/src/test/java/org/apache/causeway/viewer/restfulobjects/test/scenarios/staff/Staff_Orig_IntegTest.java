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
import java.net.URISyntaxException;
import java.util.Base64;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import com.google.common.io.Resources;
import com.google.gson.GsonBuilder;

import org.approvaltests.reporters.DiffReporter;
import org.approvaltests.reporters.UseReporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.transaction.annotation.Propagation;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.viewer.restfulobjects.test.scenarios.Abstract_IntegTest;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;

public class Staff_Orig_IntegTest extends Abstract_IntegTest {

    private GsonBuilder gsonBuilder;

    @BeforeEach
    void setup() {
        gsonBuilder = new GsonBuilder();
    }

    @SneakyThrows
    @Test
    @UseReporter(DiffReporter.class)
    public void can_create_staff_member() {

        // given
        final var staffName = "Fred Smith";

        final var bookmarkBeforeIfAny = transactionService.callTransactional(Propagation.REQUIRED, () -> {
            final var staffMember = staffMemberRepository.findByName(staffName);
            return bookmarkService.bookmarkFor(staffMember);
        }).valueAsNonNullElseFail();

        final var departmentName = "Classics";
        final var departmentBookmark = transactionService.callTransactional(Propagation.REQUIRED, () -> {
            final var staffMember = departmentRepository.findByName(departmentName);
            return bookmarkService.bookmarkFor(staffMember).orElseThrow();
        }).valueAsNonNullElseFail();

        assertThat(bookmarkBeforeIfAny).isEmpty();

        final var photoEncoded = readFileAndEncodeAsBlob("StaffMember-photo-Bar.pdf");
        String departmentHref = asRelativeHref(departmentBookmark);
        Invocation.Builder departmentRequest = restfulClient.request(departmentHref);
        Response departmentResponse = departmentRequest.get();
        assertThat(departmentResponse.getStatusInfo().getFamily()).isEqualTo(Response.Status.Family.SUCCESSFUL);

        final var requestBuilder = restfulClient.request("services/university.dept.Staff/actions/createStaffMemberWithPhoto/invoke");


        final var body = new Body(staffName, asAbsoluteHref(departmentBookmark), photoEncoded);
        final var bodyJson = gsonBuilder.create().toJson(body);

        // when
        val response = requestBuilder.post(Entity.entity(bodyJson, "application/json"));

        // then
        val entity = response.readEntity(String.class);
        assertThat(response.getStatusInfo().getFamily()).isEqualTo(Response.Status.Family.SUCCESSFUL);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        // and also json response
        // Approvals.verify(entity, jsonOptions());

        // and also object is created in database
        final var bookmarkAfterIfAny = transactionService.callTransactional(Propagation.REQUIRED, () -> {
            final var staffMember = staffMemberRepository.findByName(staffName);
            return bookmarkService.bookmarkFor(staffMember);
        }).valueAsNonNullElseFail();
        assertThat(bookmarkAfterIfAny).isNotEmpty();
    }

    private String asRelativeHref(Bookmark bookmark) {
        return String.format("objects/%s/%s", bookmark.getLogicalTypeName(), bookmark.getIdentifier());
    }

    private String asAbsoluteHref(Bookmark bookmark) {
        return String.format("%s%s", restfulClient.getConfig().getRestfulBaseUrl(), asRelativeHref(bookmark));
    }

    private String readFileAndEncodeAsBlob(String fileName) throws IOException, URISyntaxException {
        byte[] bytes = Resources.toByteArray(Resources.getResource(Abstract_IntegTest.class, fileName));
        String photoEncoded = encodePdf(fileName, bytes);
        return photoEncoded;
    }

    private String encodePdf(final String fileName, final byte[] pdfBytes) throws URISyntaxException {
        final String pdfBytesEncoded = Base64.getEncoder().encodeToString(pdfBytes);
        final String encodedBlob = String.format("%s:%s:%s", fileName, "application/pdf", pdfBytesEncoded);
        return encodedBlob;
    }

    @Getter
    static class Body {

        /**
         * @param nameValue
         * @param departmentHrefValue
         * @param blobValue - is the Blob encoded format: "filename.pdf:application/pdf:pdfBytesBase64Encoded"
         */
        Body(String nameValue, String departmentHrefValue, String blobValue) {
            photo = new org.apache.causeway.viewer.restfulobjects.test.scenarios.staff.Body.Blob(blobValue);
            name = new org.apache.causeway.viewer.restfulobjects.test.scenarios.staff.Body.Name(nameValue);
            department = new org.apache.causeway.viewer.restfulobjects.test.scenarios.staff.Body.Department(new org.apache.causeway.viewer.restfulobjects.test.scenarios.staff.Body.Value(departmentHrefValue));
        }

        private org.apache.causeway.viewer.restfulobjects.test.scenarios.staff.Body.Name name;

        private org.apache.causeway.viewer.restfulobjects.test.scenarios.staff.Body.Department department;

        private org.apache.causeway.viewer.restfulobjects.test.scenarios.staff.Body.Blob photo;

        @lombok.Value
        static class Name {
            private String value;
        }

        @lombok.Value
        static class Department {
            private org.apache.causeway.viewer.restfulobjects.test.scenarios.staff.Body.Value value;
        }

        @lombok.Value
        static class Value {
            private String href;
        }

        @lombok.Value
        static class Blob {
            private String value;
        }

    }


}


