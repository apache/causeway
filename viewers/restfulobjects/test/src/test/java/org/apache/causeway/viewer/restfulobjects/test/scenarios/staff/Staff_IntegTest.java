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

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.core.metamodel.facets.param.choices.ActionParameterChoicesFacetFromAction;

import org.apache.causeway.viewer.restfulobjects.test.domain.dom.Department;

import org.approvaltests.Approvals;
import org.approvaltests.reporters.DiffReporter;
import org.approvaltests.reporters.UseReporter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.causeway.viewer.restfulobjects.test.scenarios.Abstract_IntegTest;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.GsonBuilderUtils;
import org.springframework.transaction.annotation.Propagation;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Optional;

import com.google.common.io.Resources;
import com.google.gson.GsonBuilder;

public class Staff_IntegTest extends Abstract_IntegTest {

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

        assertThat(bookmarkBeforeIfAny).isEmpty();

        final var photoEncoded = readFileAndEncodeAsBlob("StaffMember-photo-Bar.pdf");
        final var requestBuilder = restfulClient.request("services/university.dept.Staff/actions/createStaffMemberWithPhoto/invoke");

        final var body = new Body(staffName, "Classics", photoEncoded);
        final var bodyJson = gsonBuilder.create().toJson(body);

        // when
        val response = requestBuilder.post(Entity.entity(bodyJson, "application/json"));

        // then
        assertThat(response.getStatusInfo().getFamily()).isEqualTo(Response.Status.Family.SUCCESSFUL);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

//        // and also json response
//        val entity = response.readEntity(String.class);
//        assertThat(response)
//                .extracting(Response::getStatus)
//                .isEqualTo(Response.Status.OK.getStatusCode());
//        Approvals.verify(entity, jsonOptions());

        // and also object is created in database
        final var bookmarkAfterIfAny = transactionService.callTransactional(Propagation.REQUIRED, () -> {
            final var staffMember = staffMemberRepository.findByName(staffName);
            return bookmarkService.bookmarkFor(staffMember);
        }).valueAsNonNullElseFail();
        assertThat(bookmarkAfterIfAny).isNotEmpty();
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

}


@Getter
class Body {

    /**
     * @param nameValue
     * @param departmentValue
     * @param blobValue - is the Blob encoded format: "filename.pdf:application/pdf:pdfBytesBase64Encoded"
     */
    Body(String nameValue, String departmentValue, String blobValue) {
        photo = new Blob();
        photo.value = blobValue;
        name = new Name();
        name.value = nameValue;
        department = new Department();
        department.value = departmentValue;
    }

    private Name name;

    private Department department;

    private Blob photo;

    @Getter
    static class Name {
        private String value;
    }

    @Getter
    static class Department {
        private String value;
    }

    @Getter
    static class Blob {
        private String value;
    }

}

