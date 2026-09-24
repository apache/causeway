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
package org.apache.causeway.core.metamodel.services.priming;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.events.metamodel.MetamodelListener;
import org.apache.causeway.applib.services.priming.ActionArguments;
import org.apache.causeway.applib.services.priming.ActionPrimer;
import org.apache.causeway.applib.services.priming.PrimingRegistrar;
import org.apache.causeway.applib.services.priming.PrimingRegistry;
import org.apache.causeway.applib.services.priming.ViewPrimer;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel.CausewayModuleCoreMetamodel;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

import lombok.RequiredArgsConstructor;

/**
 * Metamodel-backed implementation of the application priming registry.
 *
 * @since 2.2
 */
@Service
@Named(CausewayModuleCoreMetamodel.NAMESPACE + ".PrimingRegistryDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@RequiredArgsConstructor(onConstructor_ = @Inject)
public final class PrimingRegistryDefault
implements PrimingRegistry, PrimingService, MetamodelListener {

    private final SpecificationLoader specificationLoader;
    private final ServiceRegistry serviceRegistry;

    private Map<String, List<RegisteredActionPrimer<?>>> actionPrimers = Collections.emptyMap();
    private Map<String, List<RegisteredViewPrimer<?>>> viewPrimers = Collections.emptyMap();
    private boolean acceptingRegistrations;
    private String activeRegistrar = "<none>";

    @Override
    public synchronized void onMetamodelAboutToBeLoaded() {
        actionPrimers = new LinkedHashMap<>();
        viewPrimers = new LinkedHashMap<>();
        acceptingRegistrations = true;
    }

    @Override
    public synchronized void onMetamodelLoaded() {
        if(!acceptingRegistrations) {
            onMetamodelAboutToBeLoaded();
        }
        try {
            serviceRegistry.select(PrimingRegistrar.class).forEach(registrar -> {
                activeRegistrar = registrar.getClass().getName();
                try {
                    registrar.register(this);
                } catch (RuntimeException ex) {
                    throw new IllegalStateException(
                            "Invalid priming registration contributed by " + activeRegistrar,
                            ex);
                }
            });
            actionPrimers = immutableCopy(actionPrimers);
            viewPrimers = immutableCopy(viewPrimers);
            acceptingRegistrations = false;
        } finally {
            activeRegistrar = "<none>";
        }
    }

    @Override
    public synchronized <T> void action(
            final Class<T> domainType,
            final String actionLogicalName,
            final ActionPrimer<? super T> primer) {
        actions(domainType, Collections.singletonList(actionLogicalName), primer);
    }

    @Override
    public synchronized <T> void actions(
            final Class<T> domainType,
            final Collection<String> actionLogicalNames,
            final ActionPrimer<? super T> primer) {

        ensureAcceptingRegistrations();
        Objects.requireNonNull(actionLogicalNames, "actionLogicalNames");
        Objects.requireNonNull(primer, "primer");
        if(actionLogicalNames.isEmpty()) {
            throw invalid("At least one local action logical name is required", domainType, null);
        }

        final ObjectSpecification specification = exactSpecification(domainType);
        for (String actionLogicalName : actionLogicalNames) {
            validateLocalActionName(specification, domainType, actionLogicalName);
            final String key = actionKey(specification, actionLogicalName);
            mutableActionPrimers().computeIfAbsent(key, __ -> new ArrayList<>())
                    .add(new RegisteredActionPrimer<>(domainType, primer));
        }
    }

    @Override
    public synchronized <T> void view(
            final Class<T> domainType,
            final ViewPrimer<? super T> primer) {

        ensureAcceptingRegistrations();
        Objects.requireNonNull(primer, "primer");
        final ObjectSpecification specification = exactSpecification(domainType);
        mutableViewPrimers().computeIfAbsent(specification.logicalTypeName(), __ -> new ArrayList<>())
                .add(new RegisteredViewPrimer<>(domainType, primer));
    }

    @Override
    public void primeAction(
            final ObjectSpecification targetSpecification,
            final String actionLogicalName,
            final Object target,
            final List<Object> arguments) {

        final List<RegisteredActionPrimer<?>> matching = actionPrimers.get(
                actionKey(targetSpecification, actionLogicalName));
        if(matching == null) {
            return;
        }
        final ActionArguments actionArguments = ActionArguments.of(arguments);
        matching.forEach(registered -> registered.prime(target, actionArguments));
    }

    @Override
    public void primeView(
            final ObjectSpecification targetSpecification,
            final Object target) {

        final List<RegisteredViewPrimer<?>> matching = viewPrimers.get(
                targetSpecification.logicalTypeName());
        if(matching == null) {
            return;
        }
        matching.forEach(registered -> registered.prime(target));
    }

    private <T> ObjectSpecification exactSpecification(final Class<T> domainType) {
        Objects.requireNonNull(domainType, "domainType");
        return specificationLoader.snapshotSpecifications().stream()
                .filter(specification -> specification.getCorrespondingClass().equals(domainType))
                .findFirst()
                .orElseThrow(() -> invalid(
                        "No exact metamodel specification and logical type exists for class "
                                + domainType.getName(),
                        domainType,
                        null));
    }

    private <T> void validateLocalActionName(
            final ObjectSpecification specification,
            final Class<T> domainType,
            final String actionLogicalName) {

        if(_Strings.isNullOrEmpty(actionLogicalName)
                || !actionLogicalName.equals(actionLogicalName.trim())
                || actionLogicalName.indexOf('#') >= 0) {
            throw invalid("Invalid local action logical name", domainType, actionLogicalName);
        }
        final boolean resolvesExactly = specification
                .getAction(actionLogicalName, MixedIn.INCLUDED)
                .map(action -> actionLogicalName.equals(
                        action.getFeatureIdentifier().memberLogicalName()))
                .orElse(false);
        if(!resolvesExactly) {
            throw invalid("Unknown local action logical name", domainType, actionLogicalName);
        }
    }

    private void ensureAcceptingRegistrations() {
        if(!acceptingRegistrations) {
            throw new IllegalStateException(
                    "Priming registration is closed; registrations are accepted only during metamodel initialization");
        }
    }

    private IllegalArgumentException invalid(
            final String message,
            final Class<?> domainType,
            final String actionLogicalName) {
        return new IllegalArgumentException(String.format(
                "%s (registrar=%s, domainType=%s%s)",
                message,
                activeRegistrar,
                domainType != null ? domainType.getName() : "null",
                actionLogicalName != null ? ", actionLogicalName=" + actionLogicalName : ""));
    }

    @SuppressWarnings("unchecked")
    private Map<String, List<RegisteredActionPrimer<?>>> mutableActionPrimers() {
        return (Map<String, List<RegisteredActionPrimer<?>>>) actionPrimers;
    }

    @SuppressWarnings("unchecked")
    private Map<String, List<RegisteredViewPrimer<?>>> mutableViewPrimers() {
        return (Map<String, List<RegisteredViewPrimer<?>>>) viewPrimers;
    }

    private static String actionKey(
            final ObjectSpecification specification,
            final String actionLogicalName) {
        return specification.logicalTypeName() + "#" + actionLogicalName;
    }

    private static <T> Map<String, List<T>> immutableCopy(final Map<String, List<T>> source) {
        final Map<String, List<T>> copy = new LinkedHashMap<>();
        source.forEach((key, value) -> copy.put(
                key,
                Collections.unmodifiableList(new ArrayList<>(value))));
        return Collections.unmodifiableMap(copy);
    }

    private static final class RegisteredActionPrimer<T> {
        private final Class<T> domainType;
        private final ActionPrimer<? super T> primer;

        private RegisteredActionPrimer(
                final Class<T> domainType,
                final ActionPrimer<? super T> primer) {
            this.domainType = domainType;
            this.primer = primer;
        }

        private void prime(final Object target, final ActionArguments arguments) {
            primer.prime(domainType.cast(target), arguments);
        }
    }

    private static final class RegisteredViewPrimer<T> {
        private final Class<T> domainType;
        private final ViewPrimer<? super T> primer;

        private RegisteredViewPrimer(
                final Class<T> domainType,
                final ViewPrimer<? super T> primer) {
            this.domainType = domainType;
            this.primer = primer;
        }

        private void prime(final Object target) {
            primer.prime(domainType.cast(target));
        }
    }
}
