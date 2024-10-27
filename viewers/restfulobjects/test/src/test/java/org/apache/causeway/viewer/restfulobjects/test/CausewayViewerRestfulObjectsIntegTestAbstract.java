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
package org.apache.causeway.viewer.restfulobjects.test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.approvaltests.Approvals;
import org.approvaltests.core.Options;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.io.ClassPathResource;
import org.springframework.lang.Nullable;
import org.springframework.test.context.ActiveProfiles;

import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;
import org.apache.causeway.security.bypass.CausewayModuleSecurityBypass;
import org.apache.causeway.testing.fixtures.applib.CausewayModuleTestingFixturesApplib;
import org.apache.causeway.viewer.restfulobjects.client.AuthenticationMode;
import org.apache.causeway.viewer.restfulobjects.client.RestfulClient;
import org.apache.causeway.viewer.restfulobjects.client.RestfulClientConfig;
import org.apache.causeway.viewer.restfulobjects.jaxrsresteasy.CausewayModuleViewerRestfulObjectsJaxrsResteasy;

import static org.apache.causeway.commons.internal.assertions._Assert.assertNotNull;

import lombok.SneakyThrows;

/**
 * Intended as a base class for integration testing.
 *
 * <p>
 *     Subclass and {@link Import} the Spring {@link org.springframework.context.annotation.Configuration}s (modules)
 *     that hold the domain model.
 * </p>
 *
 * <p>
 *     The class will use {@link Approvals approval} tests to assert the returned response is correct.
 * </p>
 *
 *
 * @since 2.0 {@index}
 */
@SpringBootTest(
        classes = {
                CausewayViewerRestfulObjectsIntegTestAbstract.TestApp.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public abstract class CausewayViewerRestfulObjectsIntegTestAbstract {

    private final Class<?> resourceBaseClazz;

    protected CausewayViewerRestfulObjectsIntegTestAbstract(
            final Class<?> resourceBaseClazz
    ) {
        this.resourceBaseClazz = resourceBaseClazz;
    }

    /**
     * Compared to the production app manifest <code>domainapp.webapp.AppManifest</code>,
     * here we in effect disable security checks, and we exclude any web/UI modules.
     */
    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({
            CausewayModuleCoreRuntimeServices.class,
            CausewayModuleSecurityBypass.class,
            CausewayModuleTestingFixturesApplib.class,
            CausewayModuleViewerRestfulObjectsJaxrsResteasy.class,
    })
    @PropertySources({
            @PropertySource(CausewayPresets.H2InMemory_withUniqueSchema),
            @PropertySource(CausewayPresets.UseLog4j2Test),
            @PropertySource(CausewayPresets.SilenceMetaModel),
            @PropertySource(CausewayPresets.SilenceProgrammingModel),
//            @PropertySource("classpath:/H2InMemory_withUniqueSchema.properties"),
//            @PropertySource("classpath:/SilenceMetaModel.properties"),
//            @PropertySource("classpath:/SilenceProgrammingModel.properties"),
//            @PropertySource("classpath:/UseLog4j2Test.properties"),
    })
    public static class TestApp {
    }

    @Inject protected CausewaySystemEnvironment causewaySystemEnvironment;
    @Inject protected SpecificationLoader specificationLoader;
    @Inject protected TransactionService transactionService;

    @LocalServerPort
    protected int port;

    @BeforeEach
    void init(final TestInfo testInfo) {
        this.testInfo = testInfo;
        assertNotNull(causewaySystemEnvironment);
        assertNotNull(specificationLoader);
        assertNotNull(transactionService);
    }

    /**
     * Populated automatically by JUnit5
     */
    protected TestInfo testInfo;

    private ObjectMapper objectMapper = new ObjectMapper();

    protected RestfulClient restfulClient() {
        var clientConfig = RestfulClientConfig.builder()
                .restfulBaseUrl(String.format("http://0.0.0.0:%d/restful/", port))
                .authenticationMode(AuthenticationMode.BASIC)
                .basicAuthUser("any")           // using bypass auth.
                .basicAuthPassword("any")
                .useRequestDebugLogging(true) // default = false
                .build();

        return RestfulClient.ofConfig(clientConfig);
    }

    public enum BookmarkOptions {
        SCRUB,
        PRESERVE,
        ;
    }

    protected Options jsonOptions() {
        return jsonOptions(null, BookmarkOptions.SCRUB);
    }

    protected Options jsonOptions(final BookmarkOptions bookmarkOptions) {
        return jsonOptions(null, bookmarkOptions);
    }

    public Options jsonOptions(final Options options) {
        return jsonOptions(options, BookmarkOptions.SCRUB);
    }

    public Options jsonOptions(@Nullable Options options, final BookmarkOptions bookmarkOptions) {
        if (options == null) {
            options = new Options();
        }
        return options.withScrubber(s -> {
                    try {
                        String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(s));
                        if (bookmarkOptions == BookmarkOptions.SCRUB) {
                            prettyJson = prettyJson.replaceAll(":\\d+/", ":NNN/");
                            prettyJson = prettyJson.replaceAll(":\\d+\"", ":NNN\"");    // "oid" : "university.dept.Department:33" ; "href" : "http://0.0.0.0:NNN/restful/objects/university.dept.Department:33",
                            prettyJson = prettyJson.replaceAll("/\\d+/", "/NNN/");
                            prettyJson = prettyJson.replaceAll("/\\d+\"", "/NNN\"");
                            prettyJson = prettyJson.replaceAll(": \"\\d+\"", ": \"NNN\""); // "instanceId" : "33",
                        }
                        return prettyJson;
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .forFile().withExtension(".json");
    }

    protected void beforeEach() {}

    protected void afterEach() {}

    protected Blob asPdfBlob(final String fileName) {
        var bytes = toBytes(fileName);
        return new Blob(fileName, "application/pdf", bytes);
    }

    @SneakyThrows
    protected byte[] toBytes(final String fileName){
        InputStream inputStream = new ClassPathResource(fileName, resourceBaseClazz).getInputStream();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        return buffer.toByteArray();
    }

}
