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

import lombok.Value;
import lombok.val;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import javax.inject.Inject;

import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.commons.internal.resources._Resources;

import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.viewer.graphql.viewer.source.GraphQlServiceForCauseway;
import org.apache.causeway.viewer.graphql.viewer.source.GraphQlSourceForCauseway;

import org.approvaltests.core.Options;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;
import org.apache.causeway.persistence.jpa.eclipselink.CausewayModulePersistenceJpaEclipselink;
import org.apache.causeway.security.bypass.CausewayModuleSecurityBypass;
import org.apache.causeway.testing.fixtures.applib.CausewayModuleTestingFixturesApplib;
import org.apache.causeway.viewer.graphql.viewer.CausewayModuleIncViewerGraphqlViewer;
import org.apache.causeway.viewer.graphql.viewer.test.domain.UniversityModule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.apache.causeway.commons.internal.assertions._Assert.assertNotNull;

@SpringBootTest(
        classes = {
                CausewayViewerGraphqlTestModuleIntegTestAbstract.TestApp.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureHttpGraphQlTester
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public abstract class CausewayViewerGraphqlTestModuleIntegTestAbstract {

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
            CausewayModuleIncViewerGraphqlViewer.class,

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
    @Inject protected GraphQlServiceForCauseway graphQlServiceForCauseway;

    @LocalServerPort
    protected int port;

    @BeforeEach
    void init(final TestInfo testInfo) {
        this.testInfo = testInfo;
        assertNotNull(causewaySystemEnvironment);
        assertNotNull(specificationLoader);
        assertNotNull(transactionService);
        assertNotNull(graphQlSourceForCauseway);
        assertNotNull(graphQlServiceForCauseway);
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
    protected String submit() throws Exception{
        val httpRequest = buildRequest(testInfo, "._.gql");
        return submitRequest(httpRequest);
    }

    @Value
    protected static class GqlBody {
        String query;
    }

    protected HttpRequest buildRequest(
            final TestInfo testInfo,
            final String resourceSuffix) throws IOException {

        val testMethodName = testInfo.getTestMethod().map(Method::getName).get();
        val resourceName = getClass().getSimpleName() + "." + testMethodName + resourceSuffix;
        val resourceContents = readResource(resourceName);

        val uri = URI.create(String.format("http://0.0.0.0:%d/graphql", port));

        val gqlBody = new GqlBody(resourceContents);
        val gqlBodyStr = objectMapper.writeValueAsString(gqlBody);
        val bodyPublisher = HttpRequest.BodyPublishers.ofString(gqlBodyStr);

        return HttpRequest.newBuilder().
                uri(uri).
                POST(bodyPublisher).
                setHeader("Content-Type", "application/json").
                build();
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

    protected Options jsonOptions() {
        return new Options().withScrubber(s -> {
            try {
                return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(s));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        })
        .forFile().withExtension(".json");
    }


}
