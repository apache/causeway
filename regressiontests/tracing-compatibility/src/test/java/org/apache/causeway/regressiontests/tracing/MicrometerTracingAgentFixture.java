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

import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

import javax.inject.Provider;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.inject.ServiceInjector;
import org.apache.causeway.applib.services.xactn.TransactionState;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.reflection._MethodFacades;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.observation.CausewayObservationConfiguration;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration;
import org.apache.causeway.core.interaction.scope.InteractionScopeBeanFactoryPostProcessor;
import org.apache.causeway.core.interaction.scope.InteractionScopeLifecycleHandler;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.execution.ActionExecutor;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetAbstract;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
import org.apache.causeway.core.metamodel.services.publishing.CommandPublisher;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.core.runtime.events.MetamodelEventService;
import org.apache.causeway.core.runtimeservices.executor.MemberExecutorServiceDefault;
import org.apache.causeway.core.runtimeservices.session.InteractionIdGenerator;
import org.apache.causeway.core.runtimeservices.session.InteractionServiceDefault;
import org.apache.causeway.core.runtimeservices.transaction.TransactionServiceSpring;
import org.apache.causeway.core.webapp.modules.observation.CausewayForegroundTraceFilter;
import org.apache.causeway.viewer.wicket.ui.observation.WicketRenderObservationDescriptor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Child-process application launched by {@link MicrometerTracingCompatibilityTest}.
 *
 * <p>The fixture needs a {@code main} method because the automated JUnit test starts a new JVM
 * with the real OpenTelemetry {@code -javaagent} attached before application classes load.</p>
 */
@SpringBootConfiguration(proxyBeanMethods = false)
@EnableAutoConfiguration
@Import({
        CausewayObservationConfiguration.class,
        CausewayForegroundTraceFilter.class,
        MicrometerTracingAgentFixture.SemanticTracingController.class,
})
public final class MicrometerTracingAgentFixture {

    static final String ROOT_INTERACTION_NAME = "causeway.root.interaction";
    static final String ACTION_INVOCATION_NAME = "invoke execute-jdbc on tracing-fixture";
    static final String PAGE_RENDER_NAME = "render tracing-fixture";
    static final String PROMPT_RENDER_NAME = "prompt execute-jdbc on tracing-fixture";
    static final String ACTION_ID = "causeway.TracingFixture#executeJdbc()";
    static final String OBJECT_TYPE = "causeway.TracingFixture";
    static final String SUCCESS_MARKER = "CAUSEWAY_TRACING_FIXTURE_OK";

    private MicrometerTracingAgentFixture() {
    }

    public static void main(final String[] args) throws Exception {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(
                MicrometerTracingAgentFixture.class)
                .web(WebApplicationType.SERVLET)
                .profiles("observation")
                .logStartupInfo(false)
                .properties(
                        "server.port=0",
                        "spring.main.banner-mode=off")
                .run(args)) {
            executeHttpRequest(context);
            System.out.println(SUCCESS_MARKER);
        }
    }

    private static void executeHttpRequest(
            final ConfigurableApplicationContext context) throws Exception {
        final int port = context.getEnvironment().getProperty("local.server.port", Integer.class);
        final URL url = new URL("http://127.0.0.1:" + port + "/trace");
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        if (connection.getResponseCode() != 204) {
            throw new IllegalStateException("Semantic tracing HTTP fixture failed");
        }
        connection.disconnect();
    }

    @RestController
    public static final class SemanticTracingController {

        private final CausewayObservationIntegration observationIntegration;
        private final InteractionServiceDefault interactionService;
        private final MemberExecutorServiceDefault memberExecutorService;
        private final ActionExecutor actionExecutor;

        public SemanticTracingController(
                final CausewayObservationIntegration observationIntegration) throws Exception {
            this.observationIntegration = observationIntegration;
            this.interactionService = interactionService(observationIntegration);
            this.memberExecutorService = memberExecutorService(observationIntegration);
            this.actionExecutor = actionExecutor();
        }

        @GetMapping("/trace")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void trace() {
            interactionService.call(
                    mock(InteractionContext.class),
                    () -> {
                        renderPageAndPrompt();
                        return memberExecutorService.invokeAction(actionExecutor);
                    });
        }

        private void renderPageAndPrompt() {
            final WicketRenderObservationDescriptor page =
                    WicketRenderObservationDescriptor.page(OBJECT_TYPE);
            final WicketRenderObservationDescriptor prompt =
                    WicketRenderObservationDescriptor.actionPrompt(
                            OBJECT_TYPE, ACTION_ID, "executeJdbc");
            page.customize(observationIntegration.createNotStarted(
                    getClass(), page.getRegion().getObservationName()))
                    .observe(() -> prompt.customize(observationIntegration.createNotStarted(
                            getClass(), prompt.getRegion().getObservationName()))
                            .observe(() -> {}));
        }
    }

    private static InteractionServiceDefault interactionService(
            final CausewayObservationIntegration observationIntegration) {
        final ConfigurableBeanFactory beanFactory = mock(ConfigurableBeanFactory.class);
        final Scope interactionScope = mock(
                Scope.class,
                withSettings().extraInterfaces(InteractionScopeLifecycleHandler.class));
        when(beanFactory.getRegisteredScope(InteractionScopeBeanFactoryPostProcessor.SCOPE_NAME))
                .thenReturn(interactionScope);

        final TransactionServiceSpring transactionService = mock(TransactionServiceSpring.class);
        when(transactionService.currentTransactionState()).thenReturn(TransactionState.MUST_ABORT);
        final InteractionIdGenerator interactionIdGenerator = mock(InteractionIdGenerator.class);
        when(interactionIdGenerator.interactionId()).thenReturn(UUID.randomUUID());
        @SuppressWarnings("unchecked")
        final Provider<CommandPublisher> commandPublisherProvider = mock(Provider.class);

        return new InteractionServiceDefault(
                mock(MetamodelEventService.class),
                mock(SpecificationLoader.class),
                mock(ServiceInjector.class),
                transactionService,
                mock(ClockService.class),
                commandPublisherProvider,
                beanFactory,
                interactionIdGenerator,
                observationIntegration);
    }

    private static MemberExecutorServiceDefault memberExecutorService(
            final CausewayObservationIntegration observationIntegration) {
        @SuppressWarnings("unchecked")
        final Provider<CommandPublisher> commandPublisherProvider = mock(Provider.class);
        return new MemberExecutorServiceDefault(
                null,
                mock(CausewayConfiguration.class),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                commandPublisherProvider,
                observationIntegration);
    }

    private static ActionExecutor actionExecutor() throws Exception {
        final JdbcAction targetPojo = new JdbcAction();
        final ManagedObject target = mock(ManagedObject.class);
        when(target.getPojo()).thenReturn(targetPojo);
        final InteractionHead head = mock(InteractionHead.class);
        when(head.getTarget()).thenReturn(target);

        final ManagedObject adaptedResult = mock(ManagedObject.class);
        final ObjectManager objectManager = mock(ObjectManager.class);
        when(objectManager.adapt("compatible")).thenReturn(adaptedResult);
        final FacetHolder facetHolder = mock(FacetHolder.class);
        when(facetHolder.getObjectManager()).thenReturn(objectManager);

        final ObjectAction owningAction = mock(ObjectAction.class);
        when(owningAction.getFeatureIdentifier()).thenReturn(Identifier.actionIdentifier(
                LogicalType.eager(JdbcAction.class, OBJECT_TYPE),
                "executeJdbc"));

        return new ActionExecutor(
                mock(MetaModelContext.class),
                facetHolder,
                InteractionInitiatedBy.PASS_THROUGH,
                owningAction,
                _MethodFacades.testing.regular(JdbcAction.class.getDeclaredMethod("executeJdbc")),
                head,
                Can.empty(),
                mock(ActionInvocationFacetAbstract.class));
    }

    public static final class JdbcAction {
        public String executeJdbc() throws Exception {
            Class.forName("org.h2.Driver");
            try (Connection connection = DriverManager.getConnection(
                    "jdbc:h2:mem:causeway-tracing;DB_CLOSE_DELAY=-1")) {
                try (Statement statement = connection.createStatement()) {
                    statement.execute("create table trace_probe (id integer primary key, name varchar(32))");
                    statement.executeUpdate("insert into trace_probe (id, name) values (1, 'compatible')");
                    try (ResultSet resultSet = statement.executeQuery(
                            "select name from trace_probe where id = 1")) {
                        if (!resultSet.next() || !"compatible".equals(resultSet.getString(1))) {
                            throw new IllegalStateException("Unexpected JDBC probe result");
                        }
                    }
                }
            }
            return "compatible";
        }
    }
}
