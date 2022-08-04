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
import java.util.stream.IntStream;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.runtimeservices.IsisModuleCoreRuntimeServices;
import org.apache.isis.core.webapp.IsisModuleCoreWebapp;
import org.apache.isis.extensions.executionoutbox.applib.dom.ExecutionOutboxEntry;
import org.apache.isis.extensions.executionoutbox.applib.dom.ExecutionOutboxEntryRepository;
import org.apache.isis.extensions.executionoutbox.applib.integtest.model.CounterRepository;
import org.apache.isis.extensions.executionoutbox.applib.integtest.model.Counter_bumpUsingMixin;
import org.apache.isis.extensions.executionoutbox.applib.integtest.model.Counter_bumpUsingMixinWithExecutionPublishingDisabled;
import org.apache.isis.extensions.executionoutbox.jpa.IsisModuleExtExecutionOutboxPersistenceJpa;
import org.apache.isis.extensions.executionoutbox.jpa.integtests.model.Counter;
import org.apache.isis.extensions.executionoutbox.restclient.api.OutboxClient;
import org.apache.isis.persistence.jpa.eclipselink.IsisModulePersistenceJpaEclipselink;
import org.apache.isis.schema.ixn.v2.InteractionDto;
import org.apache.isis.security.bypass.IsisModuleSecurityBypass;
import org.apache.isis.testing.fixtures.applib.IsisModuleTestingFixturesApplib;
import org.apache.isis.viewer.restfulobjects.jaxrsresteasy4.IsisModuleViewerRestfulObjectsJaxrsResteasy4;

@SpringBootTest(
        classes = OutboxRestClient_IntegTest.AppManifest.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
public class OutboxRestClient_IntegTest  {

    @EnableAutoConfiguration
    @Configuration
    @Import({
            IsisModuleCoreRuntimeServices.class,
            IsisModuleSecurityBypass.class,
            IsisModulePersistenceJpaEclipselink.class,
            IsisModuleTestingFixturesApplib.class,
            IsisModuleExtExecutionOutboxPersistenceJpa.class,
            IsisModuleViewerRestfulObjectsJaxrsResteasy4.class,
            IsisModuleCoreWebapp.class,

            // mixins
            Counter_bumpUsingMixin.class,
            Counter_bumpUsingMixinWithExecutionPublishingDisabled.class
    })
    @PropertySources({
            @PropertySource(IsisPresets.UseLog4j2Test)
    })
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

    OutboxClient outboxClient;

    @BeforeEach
    void beforeEach() {
        interactionService.runAnonymous(() -> {
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
        });

        outboxClient = restEndpointService.newClient(port, "any", "any-password-because-security-bypass-module-is-configured")
                                          .withReadTimeoutInSecs(3000); // for debugging

    }

    @Test
    void pending_when_none() {

        List<InteractionDto> pending = outboxClient.pending();
        assertThat(pending).hasSize(0);
    }

    @Test
    void pending_when_many() {

        // given
        interactionService.runAnonymous(() -> {
            counter1 = counterRepository.findByName("counter-1");

            wrapperFactory.wrapMixin(Counter_bumpUsingMixin.class, counter1).act();
            wrapperFactory.wrap(counter1).bumpUsingDeclaredAction();
            wrapperFactory.wrap(counter1).setNum(99L);
        });
        interactionService.runAnonymous(() -> {
            List<? extends ExecutionOutboxEntry> all = executionOutboxEntryRepository.findOldest();
            assertThat(all).hasSize(3);
        });

        List<InteractionDto> pending = outboxClient.pending();
        assertThat(pending).hasSize(3);
    }

    @Test
    void scenario() {

        // given
        interactionService.runAnonymous(() -> {
            counter1 = counterRepository.findByName("counter-1");

            bump(counter1, 1);
        });

        interactionService.runAnonymous(() -> {
            List<? extends ExecutionOutboxEntry> all = repositoryService.allInstances(org.apache.isis.extensions.executionoutbox.jpa.dom.ExecutionOutboxEntry.class);
            assertThat(all).hasSize(1);
            List<? extends ExecutionOutboxEntry> oldest = executionOutboxEntryRepository.findOldest();
            assertThat(oldest).hasSize(1);
        });

        // when
        List<InteractionDto> pending = outboxClient.pending();

        // then
        assertThat(pending).hasSize(1);

        // given
        String interactionId = pending.get(0).getInteractionId();
        int sequence = pending.get(0).getExecution().getSequence();

        // when
        outboxClient.delete(interactionId, sequence);

        // then
        interactionService.runAnonymous(() -> {
            List<? extends ExecutionOutboxEntry> all = repositoryService.allInstances(org.apache.isis.extensions.executionoutbox.jpa.dom.ExecutionOutboxEntry.class);
            assertThat(all).hasSize(0);
            List<? extends ExecutionOutboxEntry> oldest = executionOutboxEntryRepository.findOldest();
            assertThat(oldest).hasSize(0);
        });

        // given
        interactionService.runAnonymous(() -> {
            counter1 = counterRepository.findByName("counter-1");
            counter2 = counterRepository.findByName("counter-2");

            bump(counter1, 30);
            bump(counter2, 30);
            bump(counter1, 40);
            bump(counter2, 40);
        });

        interactionService.runAnonymous(() -> {
            List<? extends ExecutionOutboxEntry> all = repositoryService.allInstances(org.apache.isis.extensions.executionoutbox.jpa.dom.ExecutionOutboxEntry.class);
            assertThat(all).hasSize(140);
            List<? extends ExecutionOutboxEntry> oldest = executionOutboxEntryRepository.findOldest();
            assertThat(oldest).hasSize(100); // limited to 100
        });

        // when
        List<InteractionDto> pending2 = outboxClient.pending();

        // then
        assertThat(pending2).hasSize(100); // limited to 100

        // given
        List<InteractionDto> interactionsToDelete = pending2.subList(0, 50);

        // when
        outboxClient.deleteMany(interactionsToDelete);

        // then
        interactionService.runAnonymous(() -> {
            List<? extends ExecutionOutboxEntry> all = repositoryService.allInstances(org.apache.isis.extensions.executionoutbox.jpa.dom.ExecutionOutboxEntry.class);
            assertThat(all).hasSize(90); // the original 140, subtract 50 tha were deleted.
        });

        // when
        List<InteractionDto> pending3 = outboxClient.pending();

        // then
        assertThat(pending3).hasSize(90); // all 90 are returned.

    }

    private void bump(org.apache.isis.extensions.executionoutbox.applib.integtest.model.Counter counter, int numberOfTimes) {
        IntStream.range(0, numberOfTimes).forEach(x -> {
            wrapperFactory.wrapMixin(Counter_bumpUsingMixin.class, counter).act();
        });
    }


    @Inject RestEndpointService restEndpointService;

    @Inject ExecutionOutboxEntryRepository<? extends ExecutionOutboxEntry> executionOutboxEntryRepository;
    @Inject InteractionService interactionService;
    @Inject RepositoryService repositoryService;
    @Inject CounterRepository counterRepository;
    @Inject WrapperFactory wrapperFactory;
    @Inject TransactionService transactionService;

}
