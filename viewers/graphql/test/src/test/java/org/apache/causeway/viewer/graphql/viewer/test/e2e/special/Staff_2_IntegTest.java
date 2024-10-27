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
package org.apache.causeway.viewer.graphql.viewer.test.e2e.special;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.approvaltests.Approvals;
import org.approvaltests.reporters.DiffReporter;
import org.approvaltests.reporters.UseReporter;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.springframework.test.context.ActiveProfiles;

import org.apache.causeway.viewer.graphql.viewer.test.e2e.Abstract_IntegTest;

//NOT USING @Transactional since we are running server within same transaction otherwise
@Order(60)
@ActiveProfiles("test")
public class Staff_2_IntegTest extends Abstract_IntegTest {

    @Test
    @UseReporter(DiffReporter.class)
    void find_staff_member_by_name_and_download_photo() throws Exception {

        String response = submit();
        Approvals.verify(response, jsonOptions());

        var objectMapper = new ObjectMapper();
        var jsonNodeRoot = objectMapper.readTree(response);

        var gridUrl = jsonNodeRoot
                .at("/data/rich/university_dept_Staff/findStaffMemberByName/invoke/results/_meta/grid")
                .asText();

        assertThat(gridUrl).matches("///graphql/object/university.dept.StaffMember:(\\d+)/_meta/grid");
        var gridHttpResponse = submitReturningString(gridUrl);

        assertThat(gridHttpResponse.statusCode()).isEqualTo(200);
        var gridChars = gridHttpResponse.body();
        assertThat(gridChars).isNotEmpty();

        var photoBytesUrl = jsonNodeRoot
                .at("/data/rich/university_dept_Staff/findStaffMemberByName/invoke/results/photo/get/bytes")
                .asText();

        assertThat(photoBytesUrl).matches("///graphql/object/university.dept.StaffMember:(\\d+)/photo/blobBytes");
        var photoBytesResponse = submitReturningBytes(photoBytesUrl);
        assertThat(photoBytesResponse.statusCode()).isEqualTo(200);
        var photoBytes = photoBytesResponse.body();
        assertThat(photoBytes).isNotEmpty();

        var iconBytesUrl = jsonNodeRoot
                .at("/data/rich/university_dept_Staff/findStaffMemberByName/invoke/results/_meta/icon")
                .asText();

        assertThat(iconBytesUrl).matches("///graphql/object/university.dept.StaffMember:(\\d+)/_meta/icon");
        var iconBytesResponse = submitReturningBytes(iconBytesUrl);
        assertThat(iconBytesResponse.statusCode()).isEqualTo(200);
        var iconBytes = iconBytesResponse.body();
        assertThat(iconBytes).isNotEmpty();
    }

    private HttpResponse<byte[]> submitReturningBytes(String url) throws IOException, InterruptedException {
        return submitReturningResponseHandledBy(url, HttpResponse.BodyHandlers.ofByteArray());
    }

    private HttpResponse<String> submitReturningString(String url) throws IOException, InterruptedException {
        return submitReturningResponseHandledBy(url, HttpResponse.BodyHandlers.ofString());
    }

    private <T> HttpResponse<T> submitReturningResponseHandledBy(String url, HttpResponse.BodyHandler<T> responseBodyHandler) throws IOException, InterruptedException {
        var urlSuffix = url.substring(3); // strip off the '///' prefix
        var uri = URI.create(String.format("http://0.0.0.0:%d/%s", port, urlSuffix));

        HttpRequest httpRequest = HttpRequest.newBuilder().
                uri(uri).
                GET().
                build();

        HttpClient httpClient = HttpClient.newHttpClient();
        return httpClient.send(httpRequest, responseBodyHandler);
    }

}
