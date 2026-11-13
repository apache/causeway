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
package org.apache.causeway.core.metamodel.services.priming;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.events.metamodel.MetamodelListener;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.priming.ActionArguments;
import org.apache.causeway.applib.services.priming.ActionPrimer;
import org.apache.causeway.applib.services.priming.PrimingRegistrar;
import org.apache.causeway.applib.services.priming.PrimingRegistry;
import org.apache.causeway.applib.services.priming.ViewPrimer;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.commons.internal.observation.ObservationClosure;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration.ObservationProvider;
import org.apache.causeway.core.config.observation.CausewayObservationNaming;
import org.apache.causeway.core.metamodel.CausewayModuleCoreMetamodel;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

/** Validates registrations at metamodel completion and atomically publishes immutable lookups. */
@Service
@Named(CausewayModuleCoreMetamodel.NAMESPACE + ".PrimingRegistryDefault")
@Priority(PriorityPrecedence.MIDPOINT)
public final class PrimingRegistryDefault implements PrimingRegistry, PrimingService, MetamodelListener {
    static final String ACTION_OBSERVATION_NAME = "causeway.priming.action";
    static final String VIEW_OBSERVATION_NAME = "causeway.priming.view";

    private final SpecificationLoader specificationLoader;
    private final ServiceRegistry serviceRegistry;
    private volatile Snapshot snapshot = new Snapshot(Map.of(), Map.of());
    private Map<String, List<RegisteredActionPrimer<?>>> pendingActions;
    private Map<String, List<RegisteredViewPrimer<?>>> pendingViews;
    private boolean acceptingRegistrations;
    private String activeRegistrar = "<none>";

    @Inject
    public PrimingRegistryDefault(final SpecificationLoader specificationLoader, final ServiceRegistry serviceRegistry) {
        this.specificationLoader = specificationLoader;
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public synchronized void onMetamodelLoaded() {
        pendingActions = new LinkedHashMap<>();
        pendingViews = new LinkedHashMap<>();
        acceptingRegistrations = true;
        try {
            serviceRegistry.select(PrimingRegistrar.class).forEach(registrar -> {
                activeRegistrar = registrar.getClass().getName();
                try {
                    registrar.register(this);
                } catch (RuntimeException ex) {
                    throw new IllegalStateException("Invalid priming registration contributed by " + activeRegistrar, ex);
                }
            });
            snapshot = new Snapshot(immutableCopy(pendingActions), immutableCopy(pendingViews));
        } finally {
            acceptingRegistrations = false;
            activeRegistrar = "<none>";
            pendingActions = null;
            pendingViews = null;
        }
    }

    @Override
    public synchronized <T> void action(final Class<T> domainType, final String actionLogicalName,
            final ActionPrimer<? super T> primer) {
        actions(domainType, Collections.singletonList(actionLogicalName), primer);
    }

    @Override
    public synchronized <T> void actions(final Class<T> domainType, final Collection<String> actionLogicalNames,
            final ActionPrimer<? super T> primer) {
        ensureAcceptingRegistrations();
        Objects.requireNonNull(actionLogicalNames, "actionLogicalNames");
        Objects.requireNonNull(primer, "primer");
        if (actionLogicalNames.isEmpty()) throw invalid("At least one local action name is required", domainType, null);
        final var specification = exactSpecification(domainType);
        for (String name : actionLogicalNames) {
            if (name == null || name.isBlank() || !name.equals(name.trim()) || name.indexOf('#') >= 0) {
                throw invalid("Invalid local action logical name", domainType, name);
            }
            final var action = specification.getAction(name, MixedIn.INCLUDED)
                    .filter(candidate -> name.equals(candidate.getFeatureIdentifier().memberLogicalName()))
                    .orElseThrow(() -> invalid("Unknown local action logical name", domainType, name));
            final var memberKey = actionKey(specification, name);
            final var canonicalId = Identifier.actionIdentifier(LogicalType.eager(domainType, specification.logicalTypeName()),
                    name, action.getFeatureIdentifier().memberParameterClassNames()).getLogicalIdentityString("#");
            pendingActions.computeIfAbsent(memberKey, __ -> new ArrayList<>())
                    .add(new RegisteredActionPrimer<>(domainType, primer, specification.logicalTypeName(), memberKey, canonicalId));
        }
    }

    @Override
    public synchronized <T> void view(final Class<T> domainType, final ViewPrimer<? super T> primer) {
        ensureAcceptingRegistrations();
        Objects.requireNonNull(primer, "primer");
        final var specification = exactSpecification(domainType);
        pendingViews.computeIfAbsent(specification.logicalTypeName(), __ -> new ArrayList<>())
                .add(new RegisteredViewPrimer<>(domainType, primer, specification.logicalTypeName()));
    }

    @Override
    public void primeAction(final ObjectSpecification targetSpecification, final String actionLogicalName,
            final Object target, final List<Object> arguments) {
        final var matching = snapshot.actions().get(actionKey(targetSpecification, actionLogicalName));
        if (matching == null) return;
        final var snapshotArguments = ActionArguments.of(arguments);
        final var provider = observationProvider();
        matching.forEach(registered -> registered.prime(target, snapshotArguments, provider));
    }

    @Override
    public void primeView(final ObjectSpecification targetSpecification, final Object target) {
        final var matching = snapshot.views().get(targetSpecification.logicalTypeName());
        if (matching == null) return;
        final var provider = observationProvider();
        matching.forEach(registered -> registered.prime(target, provider));
    }

    private ObjectSpecification exactSpecification(final Class<?> domainType) {
        Objects.requireNonNull(domainType, "domainType");
        return specificationLoader.snapshotSpecifications().stream()
                .filter(spec -> spec.correspondingClass().equals(domainType))
                .filter(spec -> spec.logicalTypeName() != null && !spec.logicalTypeName().isBlank())
                .findFirst().orElseThrow(() -> invalid("No exact metamodel specification and logical type", domainType, null));
    }

    private void ensureAcceptingRegistrations() {
        if (!acceptingRegistrations) throw new IllegalStateException(
                "Priming registration is closed; use a PrimingRegistrar during metamodel initialization");
    }

    private IllegalArgumentException invalid(final String message, final Class<?> domainType, final String name) {
        return new IllegalArgumentException(message + " (registrar=" + activeRegistrar + ", domainType="
                + (domainType != null ? domainType.getName() : "null") + ", actionLogicalName=" + name + ")");
    }

    private static String actionKey(final ObjectSpecification spec, final String name) {
        return spec.logicalTypeName() + "#" + name;
    }

    private static <T> Map<String, List<T>> immutableCopy(final Map<String, List<T>> source) {
        final Map<String, List<T>> copy = new LinkedHashMap<>();
        source.forEach((key, values) -> copy.put(key, List.copyOf(values)));
        return Collections.unmodifiableMap(copy);
    }

    private record Snapshot(Map<String, List<RegisteredActionPrimer<?>>> actions,
            Map<String, List<RegisteredViewPrimer<?>>> views) { }

    private @Nullable ObservationProvider observationProvider() {
        return serviceRegistry.lookupService(CausewayObservationIntegration.class)
                .filter(integration -> !integration.isNoop())
                .map(integration -> integration.provider(getClass(),
                        CausewayObservationIntegration.withModuleName(CausewayModuleCoreMetamodel.NAMESPACE)))
                .orElse(null);
    }

    private static void observePrimer(final @Nullable ObservationProvider provider, final String logicalType,
            final @Nullable String memberKey, final @Nullable String canonicalId, final Runnable callback) {
        if (provider == null) {
            callback.run();
            return;
        }
        final var observation = provider.get(memberKey != null ? ACTION_OBSERVATION_NAME : VIEW_OBSERVATION_NAME)
                .contextualName(memberKey != null ? CausewayObservationNaming.forLogicalMember("prime action", memberKey)
                        : CausewayObservationNaming.forType("prime view", logicalType))
                .lowCardinalityKeyValue("causeway.object.type", logicalType);
        if (canonicalId != null) observation.lowCardinalityKeyValue("causeway.action.id", canonicalId);
        final var closure = new ObservationClosure();
        try {
            closure.startAndOpenScope(observation);
            callback.run();
        } catch (RuntimeException | Error failure) {
            closure.onError(failure);
            throw failure;
        } finally {
            closure.close();
        }
    }

    private record RegisteredActionPrimer<T>(Class<T> domainType, ActionPrimer<? super T> primer,
            String logicalType, String memberKey, String canonicalId) {
        void prime(final Object target, final ActionArguments arguments, final @Nullable ObservationProvider provider) {
            observePrimer(provider, logicalType, memberKey, canonicalId, () -> primer.prime(domainType.cast(target), arguments));
        }
    }
    private record RegisteredViewPrimer<T>(Class<T> domainType, ViewPrimer<? super T> primer, String logicalType) {
        void prime(final Object target, final @Nullable ObservationProvider provider) {
            observePrimer(provider, logicalType, null, null, () -> primer.prime(domainType.cast(target)));
        }
    }
}
