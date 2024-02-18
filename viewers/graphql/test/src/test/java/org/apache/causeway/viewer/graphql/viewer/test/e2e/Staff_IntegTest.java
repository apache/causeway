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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.approvaltests.Approvals;
import org.approvaltests.integrations.junit5.JupiterApprovals;
import org.approvaltests.reporters.DiffReporter;
import org.approvaltests.reporters.UseReporter;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
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
public class Staff_IntegTest extends Abstract_IntegTest {

    @TestFactory
    Iterable<DynamicTest> each() throws IOException, URISyntaxException {

        val integClassName = getClass().getSimpleName();
        val classUrl = getClass().getResource(integClassName + ".class");
        Path classPath = Paths.get(classUrl.toURI());
        Path directoryPath = classPath.getParent();

        return Files.walk(directoryPath)
                .filter(Files::isRegularFile)
                .filter(file -> {
                    String fileName = file.getFileName().toString();
                    return fileName.startsWith(integClassName) && fileName.endsWith("._.gql");
                })
                .map(file -> {
                    String fileName = file.getFileName().toString();
                    String testName = fileName.substring(integClassName.length() + ".each.".length()).replace("._.gql", "");
                    return JupiterApprovals.dynamicTest(
                            testName,
                            options -> {
                                Approvals.verify(submitFileNamed(fileName), jsonOptions(options));
                                afterEach();
                                beforeEach();
                            });
                })
                .collect(Collectors.toList());
    }


}
