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
import java.util.*;
import jakarta.servlet.http.*;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.*;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.observation.*;
import org.apache.causeway.core.webapp.modules.observation.WebObservationConfiguration;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.viewer.wicket.ui.observation.*;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.util.tester.WicketTester;

/** Real servlet entry, runtime nomination and Wicket rendering; business collaborators are controlled. */
@SpringBootConfiguration(proxyBeanMethods = false)
@EnableAutoConfiguration
@Import({CausewayObservationAutoConfiguration.class, WebObservationConfiguration.class})
public class SemanticTraceTracingFixture {
    static CausewayObservationIntegration integration;
    static CausewayConfiguration config;
    static final String TYPE = "fixture.UpperCaseOwner", ACTION = TYPE + "#UpdateName(java.lang.String)";

    public static void main(String[] args) throws Exception {
        try (var context = new SpringApplicationBuilder(SemanticTraceTracingFixture.class)
                .properties("server.port=0", "server.tomcat.threads.max=1", "spring.main.banner-mode=off",
                        "management.otlp.metrics.export.enabled=false").run(args)) {
            integration = context.getBean(CausewayObservationIntegration.class);
            config = new CausewayConfiguration(context.getEnvironment(), Optional.empty(),
                    org.springframework.boot.context.properties.bind.Binder.get(context.getEnvironment()).bindOrCreate("causeway",
                            org.springframework.boot.context.properties.bind.Bindable.of(CausewayConfiguration.Causeway.class)));
            int port = Integer.parseInt(context.getEnvironment().getProperty("local.server.port"));
            for (String path : List.of("/view", "/prompt", "/action", "/fail", "/plain", "/ajax")) {
                // Raw HTTP avoids the agent's client instrumentation replacing
                // our explicit remote parent with a newly generated client span.
                try (var socket = new java.net.Socket("127.0.0.1", port)) {
                    socket.setSoTimeout(30000);
                    socket.getOutputStream().write(("GET " + path + " HTTP/1.1\r\nHost: localhost\r\n"
                            + "traceparent: 00-0123456789abcdef0123456789abcdef-0123456789abcdef-01\r\n"
                            + "Connection: close\r\n\r\n").getBytes(java.nio.charset.StandardCharsets.US_ASCII));
                    var status = new java.io.BufferedReader(new java.io.InputStreamReader(socket.getInputStream(), java.nio.charset.StandardCharsets.US_ASCII)).readLine();
                    int expected = path.equals("/fail") ? 500 : 204;
                    if (status == null || !status.startsWith("HTTP/1.1 " + expected + " ")) throw new AssertionError("unexpected HTTP status: " + status);
                }
            }
            System.out.println("CAUSEWAY_TRACING_FIXTURE_OK");
        }
    }

    @Bean ServletRegistrationBean<HttpServlet> fixtureServlet() {
        return new ServletRegistrationBean<>(new HttpServlet() {
            protected void service(HttpServletRequest request, HttpServletResponse response) {
                try {
                    integration.createNotStarted(SemanticTraceTracingFixture.class, "fixture.security").observe(() -> {
                        String path = request.getRequestURI();
                        if (path.equals("/view")) render(false, false);
                        if (path.equals("/prompt")) render(true, false);
                        if (path.equals("/action") || path.equals("/fail")) {
                            render(true, false); // action must outrank prompt and view
                            invokeAction();
                            CausewaySemanticTraceNamer.nominateAction(TYPE + "#Later()"); // equal-priority tie
                            if (path.equals("/fail")) throw new IllegalStateException("deliberate failure");
                        }
                        if (path.equals("/ajax")) render(false, true);
                    });
                    response.setStatus(204);
                } catch (IllegalStateException expected) { response.setStatus(500); }
            }
        }, "/*");
    }

    private static void render(boolean prompt, boolean subtree) {
        var tester = new WicketTester();
        tester.getApplication().getRequestCycleListeners().add(new org.apache.wicket.request.cycle.IRequestCycleListener() {
            public void onEndRequest(org.apache.wicket.request.cycle.RequestCycle cycle) { WicketRenderObservationTracker.cleanup(cycle, null); }
        });
        try {
            var region = new Region("component", subtree ? WicketRenderObservationDescriptor.property(TYPE, TYPE + "#name")
                    : WicketRenderObservationDescriptor.page(TYPE));
            if (prompt) region.add(new Region("prompt", WicketRenderObservationDescriptor.actionPrompt(TYPE, ACTION, "UpdateName")));
            tester.startComponentInPage(region, Markup.of("<div wicket:id='component'>" + (prompt ? "<div wicket:id='prompt'></div>" : "") + "</div>"));
        } finally { tester.destroy(); }
    }

    static final class Region extends WebMarkupContainer implements HasMetaModelContext {
        Region(String id, WicketRenderObservationDescriptor descriptor) { super(id); WicketRenderObservationBehavior.addTo(this, descriptor); }
        public <T> Optional<T> lookupService(Class<T> type) {
            return type == CausewayObservationIntegration.class ? Optional.of(type.cast(integration))
                    : type == CausewayConfiguration.class ? Optional.of(type.cast(config)) : Optional.empty();
        }
    }

    private static void invokeAction() {
        var executor = mock(org.apache.causeway.core.metamodel.execution.ActionExecutor.class);
        var action = mock(org.apache.causeway.core.metamodel.spec.feature.ObjectAction.class);
        var id = org.apache.causeway.applib.Identifier.actionIdentifier(org.apache.causeway.applib.id.LogicalType.eager(Region.class, TYPE), "UpdateName", String.class);
        when(action.getFeatureIdentifier()).thenReturn(id);
        var ownerSpec = mock(org.apache.causeway.core.metamodel.spec.ObjectSpecification.class);
        var loader = mock(org.apache.causeway.core.metamodel.specloader.SpecificationLoader.class);
        when(ownerSpec.getSpecificationLoader()).thenReturn(loader);
        when(loader.specForType(Object.class)).thenReturn(Optional.of(ownerSpec));
        when(ownerSpec.logicalType()).thenReturn(id.logicalType());
        when(ownerSpec.logicalTypeName()).thenReturn(TYPE);
        when(ownerSpec.beanSort()).thenReturn(org.apache.causeway.applib.services.metamodel.BeanSort.VIEW_MODEL);
        when(action.getDeclaringType()).thenReturn(ownerSpec);
        var target = org.apache.causeway.core.metamodel.object.ManagedObject.other(ownerSpec, new Object());
        var head = new org.apache.causeway.core.metamodel.interactions.InteractionHead(target, target);
        when(executor.head()).thenReturn(head); when(executor.owningAction()).thenReturn(action);
        when(executor.arguments()).thenReturn(org.apache.causeway.commons.collections.Can.empty());
        when(executor.mixedInAssociation()).thenReturn(Optional.empty());
        when(executor.interactionInitiatedBy()).thenReturn(org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy.USER);
        when(executor.facetHolder()).thenReturn(mock(org.apache.causeway.core.metamodel.facetapi.FacetHolder.class));
        var carrier = mock(org.apache.causeway.core.metamodel.execution.InteractionCarrier.InteractionCarrierForTesting.class);
        var command = mock(org.apache.causeway.applib.services.command.Command.class);
        when(command.getLogicalMemberIdentifier()).thenReturn(TYPE + "#UpdateName");
        when(carrier.command()).thenReturn(command);
        var tracker = mock(org.apache.causeway.core.metamodel.execution.InteractionLayerTracker.class);
        when(tracker.currentInteractionCarrier()).thenReturn(Optional.of(carrier));
        // Dispatch/result collaborators are controlled; the real member service
        // determines eligibility and nominates before executing domain work.
        var interactionMock = mock(org.apache.causeway.applib.services.iactn.Interaction.class);
        when(interactionMock.getCommand()).thenReturn(command);
        when(carrier.interaction()).thenReturn(interactionMock);
        when(carrier.execute(any(), any())).thenAnswer(invocation -> {
            var execution = (org.apache.causeway.applib.services.iactn.ActionInvocation) invocation.getArgument(0);
            doReturn(execution).when(interactionMock).getPriorExecution();
            return null;
        });
        var transactions = mock(org.apache.causeway.applib.services.xactn.TransactionService.class);
        when(transactions.callWithinCurrentTransactionElseCreateNew(any())).thenAnswer(invocation ->
                org.apache.causeway.commons.functional.Try.call((java.util.concurrent.Callable<?>) invocation.getArgument(0)));
        var manager = mock(org.apache.causeway.core.metamodel.objectmanager.ObjectManager.class);
        when(manager.adapt(any(), any(java.util.function.Supplier.class))).thenReturn(org.apache.causeway.core.metamodel.object.ManagedObject.unspecified());
        try {
            var constructor = org.apache.causeway.core.runtimeservices.executor.MemberExecutorServiceDefault.class.getDeclaredConstructors()[0];
            constructor.setAccessible(true);
            var members = (org.apache.causeway.core.runtimeservices.executor.MemberExecutorServiceDefault) constructor.newInstance(
                    tracker, config, manager, mock(org.apache.causeway.core.metamodel.services.ixn.InteractionDtoFactory.class),
                    null, transactions, (jakarta.inject.Provider<org.apache.causeway.core.metamodel.services.publishing.CommandPublisher>) () -> mock(org.apache.causeway.core.metamodel.services.publishing.CommandPublisher.class), integration);
            members.invokeAction(executor);
        } catch (ReflectiveOperationException ex) { throw new AssertionError(ex); }
    }
}
