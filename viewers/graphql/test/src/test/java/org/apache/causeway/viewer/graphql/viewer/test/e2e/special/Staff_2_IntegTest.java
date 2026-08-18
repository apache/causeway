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
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import tools.jackson.databind.ObjectMapper;

import org.approvaltests.Approvals;
import org.approvaltests.reporters.DiffReporter;
import org.approvaltests.reporters.UseReporter;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ActiveProfiles;

import org.apache.causeway.viewer.graphql.viewer.test.e2e.Abstract_IntegTest;

import static org.assertj.core.api.Assertions.assertThat;

//NOT USING @Transactional since we are running server within same transaction otherwise
@Order(60)
@ActiveProfiles("test")
@ExtendWith(OutputCaptureExtension.class)
public class Staff_2_IntegTest extends Abstract_IntegTest {

    @Test
    @UseReporter(DiffReporter.class)
    void findsStaffMemberAndSafelyDownloadsResources(final CapturedOutput output) throws Exception {
        String response = submit();
        Approvals.verify(response, jsonOptions());

        var objectMapper = new ObjectMapper();
        var jsonNodeRoot = objectMapper.readTree(response);
        var resultPath = "/data/rich/university_dept_Staff/findStaffMemberByName/invoke/results";
        var gridUrl = jsonNodeRoot.at(resultPath + "/_meta/grid").stringValue();
        assertThat(gridUrl).matches("/graphql/object/university.dept.StaffMember:(\\d+)/_meta/grid");
        var id = gridUrl.substring(
                gridUrl.indexOf(':') + 1,
                gridUrl.indexOf("/_meta/grid"));
        var gridResponse = submitReturningBytes(gridUrl);
        assertSuccessfulAttachment(gridResponse, "application/xml");
        assertThat(new String(gridResponse.body(), StandardCharsets.UTF_8)).isNotBlank();

        var photoBytesUrl = jsonNodeRoot.at(resultPath + "/photo/get/bytes").stringValue();
        assertThat(photoBytesUrl).matches("/graphql/object/university.dept.StaffMember:(\\d+)/photo/blobBytes");
        var photoBytesResponse = submitReturningBytes(photoBytesUrl);
        assertSuccessfulAttachment(photoBytesResponse, "application/pdf");
        assertThat(photoBytesResponse.body()).isNotEmpty();

        var profileCharsUrl = jsonNodeRoot.at(resultPath + "/profile/get/chars").stringValue();
        assertThat(profileCharsUrl).matches("/graphql/object/university.dept.StaffMember:(\\d+)/profile/clobChars");
        var profileCharsResponse = submitReturningBytes(profileCharsUrl);
        assertSuccessfulAttachment(profileCharsResponse, "text/plain");
        assertThat(new String(profileCharsResponse.body(), StandardCharsets.UTF_8))
                .isEqualTo("Profile for Gerry Jones");

        var iconBytesUrl = jsonNodeRoot.at(resultPath + "/_meta/icon").stringValue();
        assertThat(iconBytesUrl).matches("/graphql/object/university.dept.StaffMember:(\\d+)/_meta/icon");
        var iconBytesResponse = submitReturningBytes(iconBytesUrl);
        assertSuccessfulAttachment(iconBytesResponse, "image/png");
        assertThat(iconBytesResponse.body()).isNotEmpty();

        assertNotFoundWithoutDisclosure(String.format(
                "/graphql/object/university.dept.StaffMember:%s/hiddenPhoto/blobBytes",
                id));
        assertNotFoundWithoutDisclosure(
                "/graphql/object/university.dept.StaffMember:diagnostic-secret-id/photo/blobBytes");
        assertNotFoundWithoutDisclosure(String.format(
                "/graphql/object/university.dept.StaffMember:%s/not-meta/grid",
                id));

        assertThat(output.getAll())
                .doesNotContain("CONFIDENTIAL_RESOURCE_CONTENT")
                .doesNotContain("diagnostic-secret-id");
    }

    private void assertNotFoundWithoutDisclosure(final String url) throws Exception {
        var response = submitReturningBytes(url);
        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.body()).isEmpty();
        assertThat(response.headers().firstValue("Cache-Control").orElseThrow())
                .isEqualTo("private, no-store");
    }

    private static void assertSuccessfulAttachment(
            final HttpResponse<byte[]> response,
            final String expectedMediaType) {
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("Content-Type").orElseThrow())
                .startsWith(expectedMediaType);
        assertThat(response.headers().firstValue("Content-Length").orElseThrow())
                .isEqualTo(Integer.toString(response.body().length));
        assertThat(response.headers().firstValue("Cache-Control").orElseThrow())
                .isEqualTo("private, no-store");
        assertThat(response.headers().firstValue("Content-Disposition").orElseThrow())
                .startsWith("attachment;");
        assertThat(response.headers().firstValue("X-Content-Type-Options").orElseThrow())
                .isEqualTo("nosniff");
    }

    private HttpResponse<byte[]> submitReturningBytes(final String url)
            throws IOException, InterruptedException {
        var baseUri = URI.create(String.format("http://0.0.0.0:%d", port));
        var uri = baseUri.resolve(url);
        var httpRequest = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();
        return HttpClient.newHttpClient().send(
                httpRequest,
                HttpResponse.BodyHandlers.ofByteArray());
    }
}
