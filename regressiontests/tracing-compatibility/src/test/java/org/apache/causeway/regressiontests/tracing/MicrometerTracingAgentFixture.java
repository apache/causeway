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

import java.net.InetSocketAddress;
import java.net.URI;
import java.sql.DriverManager;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.services.inject.ServiceInjector;
import org.apache.causeway.applib.services.xactn.TransactionState;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.reflection._MethodFacades;
import org.apache.causeway.core.config.observation.CausewayObservationAutoConfiguration;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration;
import org.apache.causeway.core.interaction.scope.InteractionScopeBeanFactoryPostProcessor;
import org.apache.causeway.core.interaction.scope.InteractionScopeLifecycleHandler;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.execution.ActionExecutor;
import org.apache.causeway.core.metamodel.execution.ExecutionContext;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetAbstract;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.runtimeservices.executor.MemberExecutorServiceDefault;
import org.apache.causeway.core.runtimeservices.ia.InteractionServiceDefault;
import org.apache.causeway.core.runtimeservices.transaction.TransactionServiceSpring;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.handler.DefaultTracingObservationHandler;
import io.micrometer.tracing.otel.bridge.OtelBaggageManager;
import io.micrometer.tracing.otel.bridge.OtelCurrentTraceContext;
import io.micrometer.tracing.otel.bridge.OtelTracer;
import io.opentelemetry.api.GlobalOpenTelemetry;

/** Child JVM fixture: real framework boundaries with mocked non-telemetry collaborators. */
@SpringBootConfiguration(proxyBeanMethods = false)
@EnableAutoConfiguration
@Import({CausewayObservationAutoConfiguration.class, MicrometerTracingAgentFixture.AgentRegistry.class})
public class MicrometerTracingAgentFixture {
    public static void main(String[] args) throws Exception {
        try (var context = new SpringApplicationBuilder(MicrometerTracingAgentFixture.class)
                .web(WebApplicationType.NONE).logStartupInfo(false)
                .properties("spring.main.banner-mode=off").run(args)) {
            var integration = context.getBean(CausewayObservationIntegration.class);
            var beanFactory = mock(ConfigurableBeanFactory.class);
            var scope = mock(Scope.class, withSettings().extraInterfaces(InteractionScopeLifecycleHandler.class));
            when(beanFactory.getRegisteredScope(InteractionScopeBeanFactoryPostProcessor.SCOPE_NAME)).thenReturn(scope);
            var transactions = mock(TransactionServiceSpring.class);
            when(transactions.currentTransactionState()).thenReturn(TransactionState.MUST_ABORT);
            var executionContext = mock(ExecutionContext.class, RETURNS_DEEP_STUBS);
            when(executionContext.idGenerator().interactionId()).thenAnswer(__ -> UUID.randomUUID());
            var interactions = new InteractionServiceDefault(beanFactory, mock(ServiceInjector.class), transactions,
                    mock(ClockService.class), () -> null, executionContext, integration);
            var constructor = MemberExecutorServiceDefault.class.getDeclaredConstructors()[0];
            constructor.setAccessible(true);
            var members = (MemberExecutorServiceDefault) constructor.newInstance(
                    interactions, null, null, null, null, null, null, integration);
            var action = action(executionContext, integration, context.getEnvironment().acceptsProfiles(org.springframework.core.env.Profiles.of("agent")));
            var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            // One worker proves that subsequent requests cannot inherit stale thread-local scopes.
            var executor = Executors.newSingleThreadExecutor();
            server.setExecutor(executor);
            server.createContext("/trace", exchange -> {
                int status = 204;
                try {
                    if (integration.observationRegistry().getCurrentObservation() != null)
                        throw new AssertionError("stale Causeway observation");
                    interactions.call(org.apache.causeway.applib.services.iactn.InteractionContext.ofUserWithSystemDefaults(
                            org.apache.causeway.applib.services.user.UserMemento.ofName("sentinel-user")
                                    .withMultiTenancyToken("sentinel-tenant")), () -> {
                        members.invokeAction(action);
                        if (exchange.getRequestURI().getQuery() != null) throw new IllegalStateException("fixture failure");
                        return null;
                    });
                } catch (IllegalStateException expected) { status = 500; }
                catch (Throwable failure) { failure.printStackTrace(); status = 599; }
                exchange.sendResponseHeaders(status, -1);
                exchange.close();
            });
            server.start();
            try {
                for (String suffix : List.of("?fail", "")) {
                    var connection = (java.net.HttpURLConnection) URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/trace" + suffix).toURL().openConnection();
                    connection.setConnectTimeout(10000);
                    connection.setReadTimeout(30000);
                    if (connection.getResponseCode() != (suffix.isEmpty() ? 204 : 500))
                        throw new AssertionError("unexpected HTTP result: " + connection.getResponseCode());
                    connection.disconnect();
                }
                var shortSpan = integration.withTimeThreshold(integration.createNotStarted(MicrometerTracingAgentFixture.class, "threshold-success"), java.time.Duration.ofDays(1));
                shortSpan.observe(() -> {});
                var failedSpan = integration.withTimeThreshold(integration.createNotStarted(MicrometerTracingAgentFixture.class, "threshold-failure"), java.time.Duration.ofDays(1));
                try { failedSpan.observe(() -> { throw new IllegalStateException("short failure"); }); }
                catch (IllegalStateException expected) { }
                System.out.println("CAUSEWAY_TRACING_FIXTURE_OK");
            } finally { server.stop(0); executor.shutdownNow(); }
        }
    }
    private static ActionExecutor action(ExecutionContext executionContext, CausewayObservationIntegration integration, boolean agent) throws Exception {
        var specification = mock(org.apache.causeway.core.metamodel.spec.ObjectSpecification.class);
        var loader = mock(org.apache.causeway.core.metamodel.specloader.SpecificationLoader.class);
        when(specification.getSpecificationLoader()).thenReturn(loader);
        when(loader.specForType(JdbcAction.class)).thenReturn(java.util.Optional.of(specification));
        var target = ManagedObject.other(specification, new JdbcAction(integration, agent));
        var head = mock(InteractionHead.class);
        when(head.target()).thenReturn(target);
        var objectManager = mock(ObjectManager.class);
        when(objectManager.adapt("compatible")).thenReturn(ManagedObject.unspecified());
        var holder = mock(FacetHolder.class);
        when(holder.getObjectManager()).thenReturn(objectManager);
        var action = mock(ObjectAction.class);
        when(action.getFeatureIdentifier()).thenReturn(Identifier.actionIdentifier(LogicalType.eager(JdbcAction.class, "causeway.TracingFixture"), "executeJdbc"));
        return new ActionExecutor(executionContext, holder, InteractionInitiatedBy.PASS_THROUGH, action,
                _MethodFacades.testing.regular(JdbcAction.class.getDeclaredMethod("executeJdbc")), head, Can.empty(),
                mock(ActionInvocationFacetAbstract.class), integration.provider(ActionExecutor.class));
    }
    public static class JdbcAction {
        private final org.apache.causeway.core.metamodel.facets.object.entity.EntityFacet jpa;
        public JdbcAction(CausewayObservationIntegration integration, boolean agent) {
            jpa = org.apache.causeway.persistence.jpa.integration.entity.JpaObservationFixture.create(integration,
                    () -> {
                        if (agent) executeSql(); // actual automatic agent JDBC instrumentation
                        else integration.createNotStarted(JdbcAction.class, "fixture.jdbc")
                                .lowCardinalityKeyValue("db.system", "h2").observe(JdbcAction::executeSql);
                    });
        }
        public String executeJdbc() {
            jpa.persist(new org.apache.causeway.persistence.jpa.integration.entity.JpaObservationFixture.Entity());
            return "compatible";
        }
        private static void executeSql() {
            try (var connection = DriverManager.getConnection("jdbc:h2:mem:tracing;DB_CLOSE_DELAY=-1");
                    var statement = connection.createStatement();
                    var result = statement.executeQuery("SELECT 1")) {
                if (!result.next()) throw new AssertionError("missing JDBC result");
                // The Boot fixture explicitly instruments JDBC; agent mode owns its automatic JDBC spans.
            } catch (java.sql.SQLException ex) { throw new IllegalStateException(ex); }
        }
    }
    @Configuration(proxyBeanMethods = false)
    @Profile("agent")
    static class AgentRegistry {
        @Bean io.micrometer.tracing.Tracer agentTracer() {
            var currentContext = new OtelCurrentTraceContext();
            return new OtelTracer(GlobalOpenTelemetry.getTracer("org.apache.causeway"), currentContext,
                    event -> {}, new OtelBaggageManager(currentContext, List.of(), List.of()));
        }
        @Bean
        ObservationRegistry agentObservationRegistry(io.micrometer.tracing.Tracer tracer) {
            var registry = ObservationRegistry.create();
            registry.observationConfig().observationHandler(new DefaultTracingObservationHandler(tracer));
            return registry;
        }
    }
}
