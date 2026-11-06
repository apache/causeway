/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.causeway.regressiontests.tracing;

import java.sql.DriverManager;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.priming.PrimingRegistrar;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.services.priming.PrimingRegistryDefault;
import org.apache.causeway.core.metamodel.services.priming.PrimingService;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.viewer.wicket.ui.pages.obj.PrimerViewTracingSupport;

/** Real finalized registry and request admission with controlled synchronous H2 callbacks. */
final class PrimerObservationTracingFixture {
    static final String ACTION_TYPE = "causeway.TracingFixture";
    static final String VIEW_TYPE = "fixture.UpperCaseOwner";
    private final CausewayObservationIntegration integration;
    private final boolean agent;
    private final String failureKind;
    private final ObjectSpecification actionSpec;
    private final ManagedObject viewRoot;
    private final ServiceRegistry services = mock(ServiceRegistry.class);
    private final PrimingRegistryDefault registry;
    private boolean failingRequest;
    private final java.util.concurrent.atomic.AtomicInteger actions = new java.util.concurrent.atomic.AtomicInteger();
    private final java.util.concurrent.atomic.AtomicInteger views = new java.util.concurrent.atomic.AtomicInteger();

    PrimerObservationTracingFixture(final CausewayObservationIntegration integration, final boolean agent, final String failureKind) {
        this.integration = integration;
        this.agent = agent;
        this.failureKind = failureKind;
        actionSpec = spec(MicrometerTracingAgentFixture.JdbcAction.class, ACTION_TYPE);
        var action = mock(ObjectAction.class);
        when(action.getFeatureIdentifier()).thenReturn(Identifier.actionIdentifier(
                LogicalType.eager(MicrometerTracingAgentFixture.JdbcAction.class, ACTION_TYPE), "executeJdbc"));
        when(actionSpec.getAction("executeJdbc", MixedIn.INCLUDED)).thenReturn(Optional.of(action));
        var viewSpec = spec(ViewRoot.class, VIEW_TYPE);
        viewRoot = ManagedObject.other(viewSpec, new ViewRoot());
        var loader = mock(SpecificationLoader.class);
        doReturn(Can.of(actionSpec, viewSpec)).when(loader).snapshotSpecifications();
        registry = new PrimingRegistryDefault(loader, services);
        when(services.lookupService(CausewayObservationIntegration.class)).thenReturn(Optional.of(integration));
        when(services.lookupService(PrimingService.class)).thenReturn(Optional.of(registry));
        PrimingRegistrar registrar = registrations -> {
            for (int i = 0; i < 2; i++) {
                registrations.action(MicrometerTracingAgentFixture.JdbcAction.class, "executeJdbc", (target, args) -> callback("action"));
                registrations.view(ViewRoot.class, target -> callback("view"));
            }
        };
        when(services.select(PrimingRegistrar.class)).thenReturn(Can.of(registrar));
        registry.onMetamodelLoaded();
    }

    void beginRequest(final boolean fail) { failingRequest = fail; }
    void primeAction(final MicrometerTracingAgentFixture.JdbcAction target) {
        var previous = integration.observationRegistry().getCurrentObservation();
        try { registry.primeAction(actionSpec, "executeJdbc", target, List.of()); }
        finally {
            if (previous != integration.observationRegistry().getCurrentObservation()) throw new AssertionError("action primer scope leaked");
        }
    }
    void primeView(final Object page) {
        var previous = integration.observationRegistry().getCurrentObservation();
        try { PrimerViewTracingSupport.prime(page, viewRoot, services); }
        finally {
            if (previous != integration.observationRegistry().getCurrentObservation()) throw new AssertionError("view primer scope leaked");
        }
    }
    void verifyTotals() {
        int expectedActions = failureKind.equals("action") ? 5 : 4;
        int expectedViews = failureKind.equals("action") ? 6 : 5;
        if (actions.get() != expectedActions || views.get() != expectedViews) throw new AssertionError(
                "Unexpected primer counts action=" + actions.get() + " view=" + views.get());
    }

    private void callback(final String kind) {
        if (kind.equals("action")) actions.incrementAndGet(); else views.incrementAndGet();
        if (agent) executeSql();
        else integration.createNotStarted(PrimerObservationTracingFixture.class, "fixture.primer.jdbc").observe(PrimerObservationTracingFixture::executeSql);
        if (failingRequest && kind.equals(failureKind)) throw new IllegalStateException(kind + " primer fixture failure");
    }
    private static ObjectSpecification spec(final Class<?> type, final String name) {
        var spec = mock(ObjectSpecification.class, RETURNS_DEEP_STUBS);
        doReturn(type).when(spec).correspondingClass();
        when(spec.logicalTypeName()).thenReturn(name);
        when(spec.getSpecificationLoader().specForType(type)).thenReturn(Optional.of(spec));
        return spec;
    }
    private static void executeSql() {
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:primer;DB_CLOSE_DELAY=-1");
                var statement = connection.createStatement(); var result = statement.executeQuery("SELECT 1")) {
            if (!result.next()) throw new AssertionError("missing primer JDBC result");
        } catch (java.sql.SQLException ex) { throw new IllegalStateException(ex); }
    }
    private static final class ViewRoot {
        @Override public String toString() { return "sentinel-view-title"; }
    }
}
