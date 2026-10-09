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
package org.apache.causeway.regressiontests.tracing;

import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.quartz.JobBuilder;
import org.quartz.JobExecutionContext;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Profiles;

import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.services.inject.ServiceInjector;
import org.apache.causeway.applib.services.xactn.TransactionState;
import org.apache.causeway.core.config.observation.CausewayObservationAutoConfiguration;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration;
import org.apache.causeway.core.config.observation.CausewayTraceClassifier;
import org.apache.causeway.core.interaction.scope.InteractionScopeBeanFactoryPostProcessor;
import org.apache.causeway.core.interaction.scope.InteractionScopeLifecycleHandler;
import org.apache.causeway.core.metamodel.execution.ExecutionContext;
import org.apache.causeway.core.runtimeservices.ia.InteractionServiceDefault;
import org.apache.causeway.core.runtimeservices.transaction.TransactionServiceSpring;
import org.apache.causeway.core.security.authentication.InteractionContextFactory;
import org.apache.causeway.core.webapp.modules.observation.WebObservationConfiguration;
import org.apache.causeway.extensions.commandlog.applib.job.RunBackgroundCommandsJob;

import io.micrometer.tracing.Tracer;

/** Real servlet/Quartz entry points; business collaborators are controlled fixtures. */
@SpringBootConfiguration(proxyBeanMethods = false)
@EnableAutoConfiguration
@Import({CausewayObservationAutoConfiguration.class, WebObservationConfiguration.class})
public class EntryPointTracingFixture {
    static InteractionServiceDefault interactions;
    static CausewayObservationIntegration integration;
    static CausewayTraceClassifier classifier;
    static Tracer tracer;
    static boolean agent;
    static final CountDownLatch jobFinished = new CountDownLatch(1);
    static volatile Throwable jobFailure;
    static final UUID REPLAY_ID = UUID.fromString("12345678-1234-1234-1234-123456789abc");

    public static void main(String[] args) throws Exception {
        try (var context = new SpringApplicationBuilder(EntryPointTracingFixture.class)
                .properties("server.port=0", "server.tomcat.threads.max=1", "spring.main.banner-mode=off",
                        "management.otlp.metrics.export.enabled=false", "spring.quartz.auto-startup=false")
                .run(args)) {
            integration = context.getBean(CausewayObservationIntegration.class);
            classifier = context.getBean(CausewayTraceClassifier.class);
            tracer = context.getBean(Tracer.class);
            agent = context.getEnvironment().acceptsProfiles(Profiles.of("agent"));
            var beanFactory = mock(ConfigurableBeanFactory.class);
            var scope = mock(Scope.class, withSettings().extraInterfaces(InteractionScopeLifecycleHandler.class));
            when(beanFactory.getRegisteredScope(InteractionScopeBeanFactoryPostProcessor.SCOPE_NAME)).thenReturn(scope);
            var transactions = mock(TransactionServiceSpring.class);
            when(transactions.currentTransactionState()).thenReturn(TransactionState.MUST_ABORT);
            var executionContext = mock(ExecutionContext.class, RETURNS_DEEP_STUBS);
            when(executionContext.idGenerator().interactionId()).thenAnswer(__ -> UUID.randomUUID());
            interactions = new InteractionServiceDefault(beanFactory, mock(ServiceInjector.class), transactions,
                    mock(ClockService.class), () -> null, executionContext, integration);
            int port = Integer.parseInt(context.getEnvironment().getProperty("local.server.port"));
            for (String path : List.of("/wicket/fail", "/graphql", "/restful/test", "/static/test.js")) {
                var connection = (java.net.HttpURLConnection) URI.create("http://127.0.0.1:" + port + path).toURL().openConnection();
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(30000);
                int expected = path.endsWith("fail") ? 500 : 204;
                if (connection.getResponseCode() != expected) throw new AssertionError("unexpected status");
                connection.disconnect();
            }
            var properties = new Properties();
            properties.setProperty("org.quartz.scheduler.instanceName", "classification-fixture");
            properties.setProperty("org.quartz.threadPool.threadCount", "1");
            properties.setProperty("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
            var scheduler = new StdSchedulerFactory(properties).getScheduler();
            try {
                scheduler.start();
                scheduler.scheduleJob(JobBuilder.newJob(EntryJob.class).withIdentity("background-fixture").build(),
                        TriggerBuilder.newTrigger().startNow().build());
                if (!jobFinished.await(30, TimeUnit.SECONDS)) throw new AssertionError("job timeout");
            } finally { scheduler.shutdown(true); }
            if (jobFailure != null) throw new AssertionError("job failed", jobFailure);
            System.out.println("CAUSEWAY_TRACING_FIXTURE_OK");
        }
    }

    @Bean
    ServletRegistrationBean<HttpServlet> fixtureServlet() {
        return new ServletRegistrationBean<>(new HttpServlet() {
            @Override protected void service(HttpServletRequest request, HttpServletResponse response) {
                if (interactions.isInInteraction()) throw new AssertionError("leaked interaction");
                try {
                    // A security-like child makes incorrect filter ordering visible in export assertions.
                    integration.createNotStarted(EntryPointTracingFixture.class, "fixture.security").observe(() ->
                        interactions.run(InteractionContextFactory.testing(), () -> {
                            if (request.getRequestURI().endsWith("fail")) throw new IllegalStateException("fixture failure");
                        }));
                    response.setStatus(204);
                } catch (IllegalStateException expected) { response.setStatus(500); }
                if (interactions.isInInteraction()) throw new AssertionError("leaked interaction after request");
            }
        }, "/*");
    }

    public static class EntryJob extends RunBackgroundCommandsJob {
        @Override public void execute(JobExecutionContext context) {
            // Boot has no automatic Quartz entry instrumentation in this fixture.
            // Agent mode must use the actual span opened around Quartz execution.
            var entry = agent ? null : tracer.nextSpan().name("fixture.quartz").start();
            try (var scope = entry == null ? null : tracer.withSpan(entry)) {
                // Execute the production job and command executor. Persistence and
                // domain dispatch are controlled; the real replay path replaces identity.
                set(this, "traceClassifier", classifier);
                set(this, "interactionService", interactions);
                set(this, "backgroundCommandsJobControl",
                        mock(org.apache.causeway.extensions.commandlog.applib.job.BackgroundCommandsJobControl.class));
                set(this, "listeners", List.of());
                var dto = new org.apache.causeway.schema.cmd.v2.CommandDto();
                dto.setInteractionId(REPLAY_ID.toString());
                var transactions = mock(org.apache.causeway.applib.services.xactn.TransactionService.class);
                when(transactions.callTransactional(any(org.springframework.transaction.annotation.Propagation.class), any())).thenReturn(
                        org.apache.causeway.commons.functional.Try.success(List.of(dto)));
                when(transactions.callWithinCurrentTransactionElseCreateNew(any())).thenReturn(
                        org.apache.causeway.commons.functional.Try.empty());
                set(this, "transactionService", transactions);
                var repository = mock(org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository.class);
                when(repository.findByInteractionId(REPLAY_ID)).thenReturn(java.util.Optional.of(
                        mock(org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry.class)));
                set(this, "commandLogEntryRepository", repository);
                var clock = mock(ClockService.class, RETURNS_DEEP_STUBS);
                set(this, "clockService", clock);
                var publisher = mock(org.apache.causeway.core.metamodel.services.publishing.CommandPublisher.class);
                var executor = new org.apache.causeway.core.runtimeservices.command.CommandExecutorServiceDefault(
                        null, null, clock, transactions, interactions, null, null, () -> publisher, null, null);
                set(this, "commandExecutorService", executor);
                super.execute(context);
            } catch (Throwable failure) { jobFailure = failure; }
            finally {
                if (entry != null) entry.end();
                jobFinished.countDown();
            }
        }
        private static void set(Object target, String field, Object value) throws Exception {
            var f = RunBackgroundCommandsJob.class.getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        }
    }
}
