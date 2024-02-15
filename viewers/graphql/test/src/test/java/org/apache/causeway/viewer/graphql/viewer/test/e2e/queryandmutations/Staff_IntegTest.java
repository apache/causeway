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
package org.apache.causeway.viewer.graphql.viewer.test.e2e.queryandmutations;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.approvaltests.Approvals;
import org.approvaltests.reporters.DiffReporter;
import org.approvaltests.reporters.UseReporter;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.viewer.graphql.viewer.test.domain.dept.StaffMember;
import org.apache.causeway.viewer.graphql.viewer.test.e2e.Abstract_IntegTest;

import lombok.val;


//NOT USING @Transactional since we are running server within same transaction otherwise
@Order(60)
@ActiveProfiles("test")
@DisabledIfEnvironmentVariable(named = "PROJECT_ROOT_PATH", matches = ".*isis") // disable for isis build
public class Staff_IntegTest extends Abstract_IntegTest {

    @Test
    @UseReporter(DiffReporter.class)
    void list_all_staff_members() throws Exception {

        // when, then
        Approvals.verify(submit(), jsonOptions());
    }

    @Test
    @UseReporter(DiffReporter.class)
    void find_staff_member_by_name_and_edit_grade_choices() throws Exception {

        // when, then
        Approvals.verify(submit(), jsonOptions());
    }

    @Test
    @UseReporter(DiffReporter.class)
    void find_staff_member_by_name_and_edit() throws Exception {

        // given
        final Optional<Bookmark> bookmarkIfAny =
                transactionService.callTransactional(
                        Propagation.REQUIRED,
                        () -> {
                            StaffMember pojo = staffMemberRepository.findByName("Gerry Jones");
                            return bookmarkService.bookmarkFor(pojo);
                        }
                ).valueAsNullableElseFail();

        assertThat(bookmarkIfAny).isPresent();

        // when, then
        Approvals.verify(submit(), jsonOptions());

        // and in the database...
        final Optional<StaffMember> staffMemberIfAny =
                transactionService.callTransactional(
                        Propagation.REQUIRED,
                        () -> bookmarkService.lookup(bookmarkIfAny.get(), StaffMember.class)
                ).valueAsNullableElseFail();

        assertThat(staffMemberIfAny).isPresent();
        assertThat(staffMemberIfAny.get()).extracting(StaffMember::getName).isEqualTo("Gerald Johns");
    }

    @Test
    @UseReporter(DiffReporter.class)
    void staff_member_name_validate() throws Exception {

        // when, then
        Approvals.verify(submit(), jsonOptions());
    }

    @Test
    @UseReporter(DiffReporter.class)
    void staff_member_name_edit_invalid() throws Exception {

        // when, then
        Approvals.verify(submit(), jsonOptions());
    }

    @Test
    @UseReporter(DiffReporter.class)
    void create_staff_member_with_department() throws Exception {

        Approvals.verify(submit(), jsonOptions());

    }

    @Test
    @UseReporter(DiffReporter.class)
    void find_staff_member_by_name_and_download_photo() throws Exception {

        String response = submit();
        Approvals.verify(response, jsonOptions());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response);

        String url = root
                .at("/data/university_dept_Staff/findStaffMemberByName/invoke/photo/get/bytes")
                .asText();

        assertThat(url).matches("///graphql/object/university.dept.StaffMember:(\\d+)/photo/blobBytes");

        val httpResponse = submitReturningBytes(url);

        assertThat(httpResponse.statusCode()).isEqualTo(200);
        byte[] bytes = httpResponse.body();
        assertThat(bytes).isNotEmpty();

    }

    private HttpResponse<byte[]> submitReturningBytes(String url) throws IOException, InterruptedException {

        val urlSuffix = url.substring(3); // strip off the '///' prefix
        val uri = URI.create(String.format("http://0.0.0.0:%d/%s", port, urlSuffix));

        HttpRequest httpRequest = HttpRequest.newBuilder().
                uri(uri).
                GET().
                build();

        HttpClient httpClient = HttpClient.newHttpClient();
        return httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
    }

}
