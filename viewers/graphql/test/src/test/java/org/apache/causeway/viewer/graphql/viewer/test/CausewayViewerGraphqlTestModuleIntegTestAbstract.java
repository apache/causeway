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
package org.apache.causeway.viewer.graphql.viewer.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.approvaltests.Approvals;
import org.approvaltests.core.Options;
import org.approvaltests.integrations.junit5.JupiterApprovals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.lang.Nullable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.commons.internal.resources._Resources;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;
import org.apache.causeway.persistence.jpa.eclipselink.CausewayModulePersistenceJpaEclipselink;
import org.apache.causeway.security.bypass.CausewayModuleSecurityBypass;
import org.apache.causeway.testing.fixtures.applib.CausewayModuleTestingFixturesApplib;
import org.apache.causeway.viewer.graphql.viewer.CausewayModuleViewerGraphqlViewer;
import org.apache.causeway.viewer.graphql.viewer.integration.ExecutionGraphQlServiceForCauseway;
import org.apache.causeway.viewer.graphql.viewer.integration.GraphQlSourceForCauseway;
import org.apache.causeway.viewer.graphql.viewer.test.domain.UniversityModule;
import org.apache.causeway.viewer.graphql.viewer.test.e2e.Abstract_IntegTest;

import static org.apache.causeway.commons.internal.assertions._Assert.assertNotNull;

import lombok.SneakyThrows;
import lombok.Value;
import lombok.val;

@SpringBootTest(
        classes = {
                CausewayViewerGraphqlTestModuleIntegTestAbstract.TestApp.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "causeway.viewer.graphql.api-variant=QUERY_WITH_MUTATIONS_NON_SPEC_COMPLIANT"
        }
)
@AutoConfigureHttpGraphQlTester
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public abstract class CausewayViewerGraphqlTestModuleIntegTestAbstract {

    private final String suffix;

    protected CausewayViewerGraphqlTestModuleIntegTestAbstract(String suffix) {
        this.suffix = suffix;
    }
    protected CausewayViewerGraphqlTestModuleIntegTestAbstract() {
        this("._.gql");
    }

    /**
     * Compared to the production app manifest <code>domainapp.webapp.AppManifest</code>,
     * here we in effect disable security checks, and we exclude any web/UI modules.
     */
    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EnableJpaRepositories
    @Import({

            CausewayModuleCoreRuntimeServices.class,
            CausewayModuleSecurityBypass.class,
            CausewayModulePersistenceJpaEclipselink.class,
            CausewayModuleTestingFixturesApplib.class,
            CausewayModuleViewerGraphqlViewer.class,

            UniversityModule.class
    })
    @PropertySources({
            @PropertySource(CausewayPresets.H2InMemory_withUniqueSchema),
            @PropertySource(CausewayPresets.UseLog4j2Test),
            @PropertySource(CausewayPresets.SilenceMetaModel),
            @PropertySource(CausewayPresets.SilenceProgrammingModel),
    })
    public static class TestApp {

    }

    @Inject protected CausewaySystemEnvironment causewaySystemEnvironment;
    @Inject protected SpecificationLoader specificationLoader;
    @Inject protected TransactionService transactionService;
    @Inject protected GraphQlSourceForCauseway graphQlSourceForCauseway;
    @Inject protected ExecutionGraphQlServiceForCauseway executionGraphQlServiceForCauseway;

    @LocalServerPort
    protected int port;

    @BeforeEach
    void init(final TestInfo testInfo) {
        this.testInfo = testInfo;
        assertNotNull(causewaySystemEnvironment);
        assertNotNull(specificationLoader);
        assertNotNull(transactionService);
        assertNotNull(graphQlSourceForCauseway);
        assertNotNull(executionGraphQlServiceForCauseway);
    }


    /**
     * Populated automatically by JUnit5
     */
    protected TestInfo testInfo;

    private ObjectMapper objectMapper = new ObjectMapper();

    protected HttpGraphQlTester graphQlTester() {
        WebTestClient client =
                WebTestClient.bindToServer()
                        .baseUrl(String.format("http://0.0.0.0:%d/graphql", port))
                        .build();
        return HttpGraphQlTester.create(client);
    }


    /**
     * Builds an HTTP request based on the current {@link TestInfo}, with suffix <code>.submit.gql</code>,
     * and unmarshalls the response as a string
     *
     * @return the response body as a string
     * @throws Exception if an error occurs during the submission
     */
    protected String submit() {
        return submit(Collections.emptyMap());
    }

    @SneakyThrows
    protected String submit(Map<String,String> replacements) {
        val httpRequest = buildRequest(testInfo, "._.gql", replacements);
        return submitRequest(httpRequest);
    }

    protected String submit(String variant) {
        return submit(variant, Collections.emptyMap());
    }

    @SneakyThrows
    protected String submit(String variant, Map<String,String> replacements) {
        val httpRequest = buildRequest(testInfo, "._." +variant + ".gql", replacements);
        return submitRequest(httpRequest);
    }

    @SneakyThrows
    protected String submitFileNamed(final String fileName) {
        val httpRequest = buildRequest(fileName, Collections.emptyMap());
        return submitRequest(httpRequest);
    }

    @Value
    protected static class GqlBody {
        String query;
    }

    @SneakyThrows
    protected HttpRequest buildRequest(
            final TestInfo testInfo,
            final String resourceSuffix,
            final Map<String, String> replacements) {

        val testMethodName = testInfo.getTestMethod().map(Method::getName).get();
        val resourceName = getClass().getSimpleName() + "." + testMethodName + resourceSuffix;
        return buildRequest(resourceName, replacements);
    }

    @SneakyThrows
    protected HttpRequest buildRequest(String resourceName, Map<String, String> replacements) {
        String resourceContents = readResource(resourceName);
        String resourceContent = replace(resourceContents, replacements);

        val uri = URI.create(String.format("http://0.0.0.0:%d/graphql", port));

        val gqlBody = new GqlBody(resourceContent);
        val gqlBodyStr = objectMapper.writeValueAsString(gqlBody);
        val bodyPublisher = HttpRequest.BodyPublishers.ofString(gqlBodyStr);

        return HttpRequest.newBuilder().
                uri(uri).
                POST(bodyPublisher).
                setHeader("Content-Type", "application/json").
                build();
    }

    private static String replace(String str, Map<String, String> replacements) {
        val builder = new StringBuilder(str);
        replacements.forEach((key, value) -> {
            int index;
            int numMatches = 0;
            while ((index = builder.indexOf(key)) != -1) {
                builder.replace(index, index + key.length(), value);
                numMatches++;
            }
            if (numMatches == 0) {
                throw new IllegalArgumentException("Could not find '" + key + "' to replace");
            }
        });
        return builder.toString();
    }

    private String submitRequest(final HttpRequest request) throws IOException, InterruptedException {
        val responseBodyHandler = HttpResponse.BodyHandlers.ofString();
        val httpClient = HttpClient.newBuilder().build();
        val httpResponse = httpClient.send(request, responseBodyHandler);
        return httpResponse.body();
    }

    private String readResource(final String resourceName) throws IOException {
        return _Resources.loadAsString(getClass(), resourceName, StandardCharsets.UTF_8);
    }

    public enum BookmarkOptions {
        SCRUB,
        PRESERVE,
        ;
    }

    protected Options jsonOptions() {
        return jsonOptions(null, BookmarkOptions.SCRUB);
    }

    protected Options jsonOptions(BookmarkOptions bookmarkOptions) {
        return jsonOptions(null, bookmarkOptions);
    }

    public Options jsonOptions(Options options) {
        return jsonOptions(options, BookmarkOptions.SCRUB);
    }

    public Options jsonOptions(@Nullable Options options, BookmarkOptions bookmarkOptions) {
        if (options == null) {
            options = new Options();
        }
        return options.withScrubber(s -> {
                    try {
                        String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(s));
                        if (bookmarkOptions == BookmarkOptions.SCRUB) {
                            prettyJson = prettyJson.replaceAll(":\\d+/", ":NNN/");
                        }
                        return prettyJson;
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .forFile().withExtension(".json");
    }



    protected Iterable<DynamicTest> each() throws IOException, URISyntaxException {

        val integClassName = getClass().getSimpleName();
        val classUrl = getClass().getResource(integClassName + ".class");
        Path classPath = Paths.get(classUrl.toURI());
        Path directoryPath = classPath.getParent();

        return Files.walk(directoryPath)
                .filter(Files::isRegularFile)
                .filter(file -> {
                    String fileName = file.getFileName().toString();
                    return fileName.startsWith(integClassName) && fileName.endsWith(suffix);
                })
                .map(file -> {
                    String fileName = file.getFileName().toString();
                    String testName = fileName.substring(integClassName.length() + ".each.".length()).replace(suffix, "");
                    return JupiterApprovals.dynamicTest(
                            testName,
                            options -> {
                                try {
                                    Approvals.verify(submitFileNamed(fileName), jsonOptions(options));
                                } finally {
                                    afterEach();
                                    beforeEach();
                                }
                            });
                })
                .collect(Collectors.toList());
    }

    protected void beforeEach() {}

    protected void afterEach() {}

    protected static Blob asPdfBlob(String fileName) {
        val bytes = toBytes(fileName);
        return new Blob(fileName, "application/pdf", bytes);
    }

    @SneakyThrows
    protected static byte[] toBytes(String fileName){
        InputStream inputStream = new ClassPathResource(fileName, Abstract_IntegTest.class).getInputStream();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        return buffer.toByteArray();
    }


}
