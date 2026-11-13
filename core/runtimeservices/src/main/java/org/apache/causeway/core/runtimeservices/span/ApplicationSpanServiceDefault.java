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
package org.apache.causeway.core.runtimeservices.span;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.iactn.ActionInvocation;
import org.apache.causeway.applib.services.iactn.Execution;
import org.apache.causeway.applib.services.iactn.Interaction;
import org.apache.causeway.applib.services.iactn.InteractionService;
import org.apache.causeway.applib.services.span.ApplicationSpanService;
import org.apache.causeway.commons.functional.ThrowingRunnable;
import org.apache.causeway.commons.internal.observation.ObservationClosure;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration.ObservationProvider;
import org.apache.causeway.core.config.observation.CausewayObservationNaming;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;

import lombok.SneakyThrows;

/** Default synchronous application tracing regions, using the selected framework registry. */
@Service
@Named(CausewayModuleCoreRuntimeServices.NAMESPACE + ".ApplicationSpanServiceDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
public final class ApplicationSpanServiceDefault implements ApplicationSpanService {
    public static final String OBSERVATION_NAME = "causeway.application.span";
    public static final String MEMBER_ID_ATTRIBUTE = "causeway.member.id";
    public static final String SUFFIX_ATTRIBUTE = "causeway.application.span.suffix";

    private final InteractionService interactionService;
    private final ObservationProvider observationProvider;

    @Inject
    public ApplicationSpanServiceDefault(final InteractionService interactionService,
            final CausewayObservationIntegration observationIntegration) {
        this.interactionService = interactionService;
        this.observationProvider = observationIntegration.provider(getClass(),
                CausewayObservationIntegration.withModuleName(CausewayModuleCoreRuntimeServices.NAMESPACE));
    }

    @Override @SneakyThrows
    public <T> T call(final String suffix, final Callable<T> callable) {
        final var member = currentLogicalMemberIdentifier();
        final var name = CausewayObservationNaming.forApplicationSpan(member.orElse(null), suffix);
        Objects.requireNonNull(callable, "callable");
        final var observation = observationProvider.get(OBSERVATION_NAME).contextualName(name)
                .lowCardinalityKeyValue(SUFFIX_ATTRIBUTE, suffix);
        member.ifPresent(id -> observation.lowCardinalityKeyValue(MEMBER_ID_ATTRIBUTE, id));
        final var closure = new ObservationClosure();
        try {
            closure.startAndOpenScope(observation);
            return callable.call();
        } catch (Throwable failure) {
            closure.onError(failure);
            throw failure;
        } finally {
            closure.close();
        }
    }

    @Override
    public void run(final String suffix, final ThrowingRunnable runnable) {
        Objects.requireNonNull(runnable, "runnable");
        call(suffix, () -> { runnable.run(); return null; });
    }

    private Optional<String> currentLogicalMemberIdentifier() {
        return interactionService.currentInteraction().map(Interaction::getCurrentExecution)
                .map(ApplicationSpanServiceDefault::domainFacingIdentifier)
                .map(id -> id.logicalTypeName() + "#" + id.memberLogicalName());
    }

    private static Identifier domainFacingIdentifier(final Execution<?, ?> execution) {
        return execution instanceof ActionInvocation action
                ? action.getDomainFacingLogicalMemberIdentifier() : execution.getLogicalMemberIdentifier();
    }
}
