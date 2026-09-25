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
import java.util.Collections;
import java.util.Optional;
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
import org.apache.causeway.applib.services.priming.PrimingRegistrar;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.applib.services.span.ApplicationSpanService;
import org.apache.causeway.applib.services.xactn.TransactionState;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.reflection._MethodFacades;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.observation.CausewayObservationConfiguration;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration;
import org.apache.causeway.core.config.observation.CausewaySemanticTraceNamer;
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
import org.apache.causeway.core.metamodel.services.priming.PrimingRegistryDefault;
import org.apache.causeway.core.metamodel.services.publishing.CommandPublisher;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.core.runtime.events.MetamodelEventService;
import org.apache.causeway.core.runtimeservices.executor.MemberExecutorServiceDefault;
import org.apache.causeway.core.runtimeservices.session.InteractionIdGenerator;
import org.apache.causeway.core.runtimeservices.session.InteractionServiceDefault;
import org.apache.causeway.core.runtimeservices.span.ApplicationSpanServiceDefault;
import org.apache.causeway.core.runtimeservices.transaction.TransactionServiceSpring;
import org.apache.causeway.core.webapp.modules.observation.CausewayForegroundTraceFilter;
import org.apache.causeway.viewer.wicket.ui.observation.WicketRenderObservationDescriptor;

import static org.mockito.Mockito.doReturn;
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
    static final String ACTION_INVOCATION_NAME =
            "act causeway.TracingFixture#executeJdbc";
    static final String ENTITY_CHANGE_EVALUATION_NAME = "evaluate property changes";
    static final String AUDIT_TRAIL_WRITE_NAME = "write audit trail";
    static final String ACTION_PRIMER_NAME =
            "prime action causeway.TracingFixture#executeJdbc";
    static final String VIEW_PRIMER_NAME = "prime view causeway.TracingFixture";
    static final String PAGE_PREPARATION_NAME = "prepare causeway.TracingFixture";
    static final String COLLECTION_PREPARATION_NAME = "prepare collection roles";
    static final String ROW_PREPARATION_NAME = "prepare row causeway.TracingRole";
    static final String PAGE_RENDER_NAME = "render causeway.TracingFixture";
    static final String FIELDSET_RENDER_NAME = "render fieldset identity";
    static final String PROPERTY_RENDER_NAME = "render property emailAddress";
    static final String COLLECTION_RENDER_NAME = "render collection roles";
    static final String TABLE_RENDER_NAME = "render table roles";
    static final String TABLE_HEADER_RENDER_NAME = "render table header roles";
    static final String TABLE_BODY_RENDER_NAME = "render table body roles";
    static final String TABLE_FOOTER_RENDER_NAME = "render table footer roles";
    static final String ROW_RENDER_NAME = "render row causeway.TracingRole";
    static final String ROW_PROPERTY_RENDER_NAME = "render property name";
    static final String ACTION_RENDER_NAME = "render action executeJdbc";
    static final String ROW_ACTION_RENDER_NAME = "render action update";
    static final String PROMPT_RENDER_NAME =
            "prompt causeway.TracingFixture#executeJdbc";
    static final String VIEW_TRACE_NAME = "view causeway.TracingFixture";
    static final String APPLICATION_ACTION_NAME = "app jdbcWork";
    static final String APPLICATION_OUTER_NAME = "app outer";
    static final String APPLICATION_INNER_NAME = "app inner";
    static final String APPLICATION_FAILURE_NAME = "app failure";
    static final String ACTION_ID = "causeway.TracingFixture#executeJdbc()";
    static final String OBJECT_TYPE = "causeway.TracingFixture";
    static final String COLLECTION_ID = OBJECT_TYPE + "#roles";
    static final String ROW_OBJECT_TYPE = "causeway.TracingRole";
    static final String ROW_PROPERTY_ID = ROW_OBJECT_TYPE + "#name";
    static final String ROW_ACTION_ID = ROW_OBJECT_TYPE + "#update()";
    static final UUID INTERACTION_ID =
            UUID.fromString("12345678-1234-1234-1234-123456789abc");
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
            executeHttpRequest(context, "/trace", 204);
            executeHttpRequest(context, "/trace/prompt", 204);
            executeHttpRequest(context, "/trace/view", 204);
            executeHttpRequest(context, "/trace/application", 204);
            executeHttpRequest(context, "/trace/application/failure", 500);
            executeHttpRequest(context, "/trace/unsupported", 204);
            executeHttpRequest(context, "/trace/failure", 500);
            System.out.println(SUCCESS_MARKER);
        }
    }

    private static void executeHttpRequest(
            final ConfigurableApplicationContext context,
            final String path,
            final int expectedStatus) throws Exception {
        final int port = context.getEnvironment().getProperty("local.server.port", Integer.class);
        final URL url = new URL("http://127.0.0.1:" + port + path);
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        final int actualStatus = connection.getResponseCode();
        if (actualStatus != expectedStatus) {
            throw new IllegalStateException("Semantic tracing HTTP fixture failed for "
                    + path + ": expected " + expectedStatus + " but got " + actualStatus);
        }
        connection.disconnect();
    }

    @RestController
    public static final class SemanticTracingController {

        private final CausewayObservationIntegration observationIntegration;
        private final InteractionServiceDefault interactionService;
        private final MemberExecutorServiceDefault memberExecutorService;
        private final ApplicationSpanService applicationSpanService;
        private final ActionExecutor actionExecutor;
        private final JdbcAction actionTarget;
        private final PrimerFixture primerFixture;

        public SemanticTracingController(
                final CausewayObservationIntegration observationIntegration) throws Exception {
            this.observationIntegration = observationIntegration;
            this.interactionService = interactionService(observationIntegration);
            this.memberExecutorService = memberExecutorService(observationIntegration);
            this.applicationSpanService = new ApplicationSpanServiceDefault(
                    interactionService, observationIntegration);
            this.primerFixture = primerFixture(observationIntegration);
            this.actionTarget = new JdbcAction(
                    this::completeEntityChanges,
                    applicationSpanService,
                    primerFixture);
            this.actionExecutor = actionExecutor(
                    actionTarget, primerFixture.specification);
        }

        @GetMapping("/trace")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void trace() {
            interactionService.call(
                    mock(InteractionContext.class),
                    () -> {
                        renderPageAndPrompt();
                        CausewaySemanticTraceNamer.nominateAction(
                                "causeway.TracingFixture#executeJdbc");
                        CausewaySemanticTraceNamer.nominateAction(
                                "causeway.TracingFixture#ignoredEqualPriority");
                        return memberExecutorService.invokeAction(actionExecutor);
                    });
        }

        @GetMapping("/trace/prompt")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void prompt() {
            interactionService.call(
                    mock(InteractionContext.class),
                    () -> {
                        observe(
                                WicketRenderObservationDescriptor.actionPrompt(
                                        OBJECT_TYPE, ACTION_ID, "executeJdbc"),
                                () -> {});
                        return null;
                    });
        }

        @GetMapping("/trace/view")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void view() {
            interactionService.call(
                    mock(InteractionContext.class),
                    () -> {
                        observe(
                                WicketRenderObservationDescriptor.page(OBJECT_TYPE),
                                () -> {});
                        return null;
                    });
        }

        @GetMapping("/trace/application")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void application() {
            interactionService.run(
                    mock(InteractionContext.class),
                    () -> applicationSpanService.run(
                            "outer",
                            () -> applicationSpanService.call(
                                    "inner",
                                    () -> "done")));
        }

        @GetMapping("/trace/application/failure")
        public void applicationFailure() {
            interactionService.run(
                    mock(InteractionContext.class),
                    () -> applicationSpanService.run("failure", () -> {
                        throw new IllegalStateException("expected application span failure");
                    }));
        }

        @GetMapping("/trace/unsupported")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void unsupported() {
        }

        @GetMapping("/trace/failure")
        public void failure() {
            CausewaySemanticTraceNamer.nominateView(OBJECT_TYPE);
            throw new IllegalStateException("expected semantic tracing failure");
        }

        private void renderPageAndPrompt() {
            primerFixture.registry.primeView(
                    primerFixture.specification, actionTarget);
            final WicketRenderObservationDescriptor preparation =
                    WicketRenderObservationDescriptor.pagePreparation(OBJECT_TYPE);
            final WicketRenderObservationDescriptor collectionPreparation =
                    WicketRenderObservationDescriptor.collectionPreparation(
                            OBJECT_TYPE, COLLECTION_ID);
            final WicketRenderObservationDescriptor rowPreparation =
                    WicketRenderObservationDescriptor.rowPreparation(
                            ROW_OBJECT_TYPE, COLLECTION_ID);
            final WicketRenderObservationDescriptor page =
                    WicketRenderObservationDescriptor.page(OBJECT_TYPE);
            final WicketRenderObservationDescriptor fieldset =
                    WicketRenderObservationDescriptor.fieldset(OBJECT_TYPE, "identity");
            final WicketRenderObservationDescriptor property =
                    WicketRenderObservationDescriptor.property(
                            OBJECT_TYPE, OBJECT_TYPE + "#emailAddress");
            final WicketRenderObservationDescriptor collection =
                    WicketRenderObservationDescriptor.collection(
                            OBJECT_TYPE, COLLECTION_ID);
            final WicketRenderObservationDescriptor table =
                    WicketRenderObservationDescriptor.table(
                            OBJECT_TYPE, COLLECTION_ID);
            final WicketRenderObservationDescriptor tableHeader =
                    WicketRenderObservationDescriptor.tableHeader(
                            OBJECT_TYPE, COLLECTION_ID);
            final WicketRenderObservationDescriptor tableBody =
                    WicketRenderObservationDescriptor.tableBody(
                            OBJECT_TYPE, COLLECTION_ID);
            final WicketRenderObservationDescriptor tableFooter =
                    WicketRenderObservationDescriptor.tableFooter(
                            OBJECT_TYPE, COLLECTION_ID);
            final WicketRenderObservationDescriptor row =
                    WicketRenderObservationDescriptor.row(
                            ROW_OBJECT_TYPE, COLLECTION_ID);
            final WicketRenderObservationDescriptor rowProperty =
                    WicketRenderObservationDescriptor.property(
                            ROW_OBJECT_TYPE, ROW_PROPERTY_ID);
            final WicketRenderObservationDescriptor rowAction =
                    WicketRenderObservationDescriptor.action(
                            ROW_OBJECT_TYPE, ROW_ACTION_ID);
            final WicketRenderObservationDescriptor action =
                    WicketRenderObservationDescriptor.action(
                            OBJECT_TYPE, ACTION_ID);
            final WicketRenderObservationDescriptor prompt =
                    WicketRenderObservationDescriptor.actionPrompt(
                            OBJECT_TYPE, ACTION_ID, "executeJdbc");
            observe(preparation, () -> {
                executePreparationJdbc();
                observe(collectionPreparation,
                        () -> observe(rowPreparation, this::executePreparationJdbc));
            });
            observe(page, () -> {
                observe(fieldset, () -> {
                    observe(property, () -> {});
                    observe(action, () -> {});
                });
                observe(collection, () -> observe(table, () -> {
                    observe(tableHeader, () -> {});
                    observe(tableBody, () -> observe(row, () -> {
                        observe(rowProperty, () -> {});
                        observe(rowAction, () -> {});
                    }));
                    observe(tableFooter, () -> {});
                }));
                observe(prompt, () -> {});
            });
        }

        private void observe(
                final WicketRenderObservationDescriptor descriptor,
                final Runnable rendering) {
            descriptor.nominateSemanticTraceName();
            descriptor.customize(observationIntegration.createNotStarted(
                    getClass(), descriptor.getRegion().getObservationName()))
                    .observe(rendering);
        }

        private void completeEntityChanges() {
            evaluatePropertyChanges();
            writeAuditTrail();
        }

        private void evaluatePropertyChanges() {
            observationIntegration.provider(
                    getClass(),
                    CausewayObservationIntegration.withModuleName(
                            "causeway.persistence.commons"))
                    .get("causeway.entitychange.evaluate")
                    .contextualName(ENTITY_CHANGE_EVALUATION_NAME)
                    .observe(MicrometerTracingAgentFixture::executeEntityChangeJdbc);
        }

        private void writeAuditTrail() {
            observationIntegration.provider(
                    getClass(),
                    CausewayObservationIntegration.withModuleName("causeway.ext.auditTrail"))
                    .get("causeway.audittrail.write")
                    .contextualName(AUDIT_TRAIL_WRITE_NAME)
                    .observe(MicrometerTracingAgentFixture::executeAuditJdbc);
        }

        private void executePreparationJdbc() {
            try {
                Class.forName("org.h2.Driver");
                try (Connection connection = DriverManager.getConnection(
                        "jdbc:h2:mem:causeway-tracing;DB_CLOSE_DELAY=-1");
                        Statement statement = connection.createStatement();
                        ResultSet resultSet = statement.executeQuery("select 1")) {
                    if(!resultSet.next() || resultSet.getInt(1) != 1) {
                        throw new IllegalStateException("Unexpected preparation JDBC probe result");
                    }
                }
            } catch (Exception ex) {
                throw new IllegalStateException("Preparation JDBC probe failed", ex);
            }
        }
    }

    private static void executeEntityChangeJdbc() {
        try {
            Class.forName("org.h2.Driver");
            try (Connection connection = DriverManager.getConnection(
                    "jdbc:h2:mem:causeway-tracing;DB_CLOSE_DELAY=-1");
                    Statement statement = connection.createStatement()) {
                for (int id = 1; id <= 2; id++) {
                    try (ResultSet resultSet = statement.executeQuery(
                            "select name from trace_probe where id = " + id)) {
                        if(id == 1 && (!resultSet.next()
                                || !"compatible".equals(resultSet.getString(1)))) {
                            throw new IllegalStateException(
                                    "Unexpected entity-change JDBC probe result");
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Entity-change JDBC probe failed", ex);
        }
    }

    private static void executeAuditJdbc() {
        try {
            Class.forName("org.h2.Driver");
            try (Connection connection = DriverManager.getConnection(
                    "jdbc:h2:mem:causeway-tracing;DB_CLOSE_DELAY=-1");
                    Statement statement = connection.createStatement()) {
                statement.execute("create table audit_probe "
                        + "(id integer primary key, property_name varchar(32))");
                statement.executeUpdate(
                        "insert into audit_probe (id, property_name) values (1, 'status')");
                statement.executeUpdate(
                        "insert into audit_probe (id, property_name) values (2, 'total')");
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Audit JDBC probe failed", ex);
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
        when(interactionIdGenerator.interactionId()).thenReturn(INTERACTION_ID);
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

    private static ActionExecutor actionExecutor(
            final JdbcAction targetPojo,
            final ObjectSpecification specification) throws Exception {
        final ManagedObject target = mock(ManagedObject.class);
        when(target.getPojo()).thenReturn(targetPojo);
        when(target.objSpec()).thenReturn(specification);
        final InteractionHead head = mock(InteractionHead.class);
        when(head.getTarget()).thenReturn(target);
        when(head.getOwner()).thenReturn(target);

        final ManagedObject adaptedResult = mock(ManagedObject.class);
        final ObjectManager objectManager = mock(ObjectManager.class);
        when(objectManager.adapt("compatible")).thenReturn(adaptedResult);
        final FacetHolder facetHolder = mock(FacetHolder.class);
        when(facetHolder.getObjectManager()).thenReturn(objectManager);

        final ObjectSpecification declaringType = specification;
        final ObjectAction owningAction = mock(ObjectAction.class);
        when(owningAction.getDeclaringType()).thenReturn(declaringType);
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

    private static PrimerFixture primerFixture(
            final CausewayObservationIntegration observationIntegration) {
        final SpecificationLoader specificationLoader = mock(SpecificationLoader.class);
        final ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
        final ObjectSpecification specification = mock(ObjectSpecification.class);
        doReturn(JdbcAction.class).when(specification).getCorrespondingClass();
        when(specification.logicalTypeName()).thenReturn(OBJECT_TYPE);
        final ObjectAction action = mock(ObjectAction.class);
        when(action.getFeatureIdentifier()).thenReturn(Identifier.actionIdentifier(
                LogicalType.eager(JdbcAction.class, OBJECT_TYPE),
                "executeJdbc"));
        when(specification.getAction("executeJdbc", MixedIn.INCLUDED))
                .thenReturn(Optional.of(action));
        when(specificationLoader.snapshotSpecifications())
                .thenReturn(Can.of(specification));

        final PrimingRegistrar registrar = registry -> {
            registry.action(
                    JdbcAction.class,
                    "executeJdbc",
                    (target, arguments) -> executePrimerJdbc());
            registry.view(JdbcAction.class, target -> executePrimerJdbc());
        };
        when(serviceRegistry.select(PrimingRegistrar.class))
                .thenReturn(Can.of(registrar));
        doReturn(Optional.of(observationIntegration)).when(serviceRegistry)
                .lookupService(CausewayObservationIntegration.class);

        final PrimingRegistryDefault registry = new PrimingRegistryDefault(
                specificationLoader, serviceRegistry);
        registry.onMetamodelAboutToBeLoaded();
        registry.onMetamodelLoaded();
        return new PrimerFixture(registry, specification);
    }

    private static void executePrimerJdbc() {
        try {
            Class.forName("org.h2.Driver");
            try (Connection connection = DriverManager.getConnection(
                    "jdbc:h2:mem:causeway-tracing;DB_CLOSE_DELAY=-1");
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery("select 1")) {
                if(!resultSet.next() || resultSet.getInt(1) != 1) {
                    throw new IllegalStateException("Unexpected primer JDBC probe result");
                }
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Primer JDBC probe failed", ex);
        }
    }

    private static final class PrimerFixture {
        private final PrimingRegistryDefault registry;
        private final ObjectSpecification specification;

        private PrimerFixture(
                final PrimingRegistryDefault registry,
                final ObjectSpecification specification) {
            this.registry = registry;
            this.specification = specification;
        }
    }

    public static final class JdbcAction {

        private final Runnable auditTrailWrite;
        private final ApplicationSpanService applicationSpanService;
        private final PrimerFixture primerFixture;

        private JdbcAction(
                final Runnable auditTrailWrite,
                final ApplicationSpanService applicationSpanService,
                final PrimerFixture primerFixture) {
            this.auditTrailWrite = auditTrailWrite;
            this.applicationSpanService = applicationSpanService;
            this.primerFixture = primerFixture;
        }

        public String executeJdbc() {
            primerFixture.registry.primeAction(
                    primerFixture.specification,
                    "executeJdbc",
                    this,
                    Collections.emptyList());
            final String result = applicationSpanService.call("jdbcWork", () -> {
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
            });
            auditTrailWrite.run();
            return result;
        }
    }
}
