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
package org.apache.isis.extensions.executionoutbox.restclient.integtests;

import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.core.config.RestEasyConfiguration;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.config.viewer.web.WebAppContextPath;
import org.apache.isis.core.runtimeservices.IsisModuleCoreRuntimeServices;
import org.apache.isis.core.webapp.IsisModuleCoreWebapp;
import org.apache.isis.extensions.executionoutbox.applib.dom.ExecutionOutboxEntry;
import org.apache.isis.extensions.executionoutbox.applib.dom.ExecutionOutboxEntryRepository;
import org.apache.isis.extensions.executionoutbox.applib.integtest.model.CounterRepository;
import org.apache.isis.extensions.executionoutbox.applib.integtest.model.Counter_bumpUsingMixin;
import org.apache.isis.extensions.executionoutbox.jpa.IsisModuleExtExecutionOutboxPersistenceJpa;
import org.apache.isis.extensions.executionoutbox.jpa.integtests.model.Counter;
import org.apache.isis.extensions.executionoutbox.restclient.api.OutboxClient;
import org.apache.isis.persistence.jpa.eclipselink.IsisModulePersistenceJpaEclipselink;
import org.apache.isis.schema.ixn.v2.InteractionDto;
import org.apache.isis.security.bypass.IsisModuleSecurityBypass;
import org.apache.isis.testing.fixtures.applib.IsisModuleTestingFixturesApplib;
import org.apache.isis.viewer.restfulobjects.jaxrsresteasy4.IsisModuleViewerRestfulObjectsJaxrsResteasy4;
import org.apache.isis.viewer.restfulobjects.viewer.IsisModuleViewerRestfulObjectsViewer;

import lombok.val;

//@SpringBootTest(
//        classes = OutboxRestClient_IntegTest.AppManifest.class,
//        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
//)
@SpringBootTest(
        classes = {RestEndpointService.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(IsisPresets.UseLog4j2Test)
@Import({
        OutboxRestClient_IntegTest.AppManifest.class,
        IsisModuleViewerRestfulObjectsJaxrsResteasy4.class
})
@ActiveProfiles("test")
public class OutboxRestClient_IntegTest  {

//    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Configuration
    @Import({
            IsisModuleCoreRuntimeServices.class,
            IsisModuleSecurityBypass.class,
            IsisModulePersistenceJpaEclipselink.class,
            IsisModuleTestingFixturesApplib.class,
            IsisModuleExtExecutionOutboxPersistenceJpa.class,
//            IsisModuleViewerRestfulObjectsJaxrsResteasy4.class,
//            IsisModuleCoreWebapp.class
    })
//    @PropertySources({
//            @PropertySource(IsisPresets.UseLog4j2Test)
//    })
    @EntityScan(basePackageClasses = {Counter.class})
    @ComponentScan(basePackageClasses = {AppManifest.class, Counter.class})
    public static class AppManifest {
    }

    @LocalServerPort
    protected int port;

    @BeforeAll
    static void beforeAll() {
        IsisPresets.forcePrototyping();
    }

    org.apache.isis.extensions.executionoutbox.applib.integtest.model.Counter counter1;
    org.apache.isis.extensions.executionoutbox.applib.integtest.model.Counter counter2;

    @BeforeEach
    void beforeEach() {
        transactionService.runTransactional(Propagation.REQUIRED, () -> {
            counterRepository.removeAll();
            executionOutboxEntryRepository.removeAll();

            assertThat(counterRepository.find()).isEmpty();

            counter1 = counterRepository.persist(Counter.builder().name("counter-1").build());
            counter2 = counterRepository.persist(Counter.builder().name("counter-2").build());

            assertThat(counterRepository.find()).hasSize(2);

            List<? extends ExecutionOutboxEntry> all = executionOutboxEntryRepository.findOldest();
            assertThat(all).isEmpty();
        });

    }

    @Test
    void invoke_many() {

        // given
        transactionService.runTransactional(Propagation.REQUIRED, () -> {
            counter1 = counterRepository.findByName("counter-1");

            wrapperFactory.wrapMixin(Counter_bumpUsingMixin.class, counter1).act();
            wrapperFactory.wrap(counter1).bumpUsingDeclaredAction();
            wrapperFactory.wrap(counter1).setNum(99L);

            interactionService.closeInteractionLayers();    // to flush
            interactionService.openInteraction();

            List<? extends ExecutionOutboxEntry> all = executionOutboxEntryRepository.findOldest();
            assertThat(all).hasSize(3);
        });

        OutboxClient outboxClient = restEndpointService.newClient(port, "any", "any-password-because-security-bypass-module-is-configured");

        List<InteractionDto> pending = outboxClient.pending();
        assertThat(pending).hasSize(3);
    }


    @Inject RestEndpointService restEndpointService;

    @Inject ExecutionOutboxEntryRepository<? extends ExecutionOutboxEntry> executionOutboxEntryRepository;
    @Inject InteractionService interactionService;
    @Inject CounterRepository counterRepository;
    @Inject WrapperFactory wrapperFactory;
    @Inject TransactionService transactionService;

}
