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
package org.apache.causeway.core.metamodel.interactions;

import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants;
import org.apache.causeway.core.metamodel.consent.Consent;
import org.apache.causeway.core.metamodel.consent.InteractionAdvisor;
import org.apache.causeway.core.metamodel.consent.InteractionResult;
import org.apache.causeway.core.metamodel.consent.InteractionResultSet;
import org.apache.causeway.core.metamodel.consent.VetoUtil;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.DomainEventFacetAbstract;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacet;
import org.apache.causeway.core.metamodel.interactions.use.UsabilityContext;
import org.apache.causeway.core.metamodel.interactions.val.ValidityContext;
import org.apache.causeway.core.metamodel.interactions.vis.VisibilityContext;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public final class InteractionUtils {

    public InteractionResult isVisibleResult(final FacetHolder facetHolder, final VisibilityContext context) {

        var builder = InteractionResult.builder(context.createInteractionEvent());

        // depending on the ifHiddenPolicy, we may do no vetoing here (instead, it moves into the usability check).
        var ifHiddenPolicy = context.renderPolicy().ifHiddenPolicy();
        switch (ifHiddenPolicy) {
            case HIDE:
                facetHolder.streamFacets(HidingInteractionAdvisor.class)
                .filter(advisor->compatible(advisor, context))
                .forEach(advisor->{
                    _Strings.nonEmpty(advisor.hides(context))
                        .map(Consent.VetoReason::explicit)
                        .ifPresent(hidingReason->builder.addAdvise(hidingReason, advisor));
                });
                break;
            case SHOW_AS_DISABLED:
            case SHOW_AS_DISABLED_WITH_DIAGNOSTICS:
            default:
                break;
        }

        return builder.build();
    }

    public InteractionResult isUsableResult(final FacetHolder facetHolder, final UsabilityContext context) {

        var builder = InteractionResult.builder(context.createInteractionEvent());

        // depending on the ifHiddenPolicy, we additionally may disable using a hidden advisor
        var ifHiddenPolicy = context.renderPolicy().ifHiddenPolicy();
        switch (ifHiddenPolicy) {
            case HIDE:
                break;
            case SHOW_AS_DISABLED:
            case SHOW_AS_DISABLED_WITH_DIAGNOSTICS:
                var visibilityContext = context.asVisibilityContext();
                facetHolder.streamFacets(HidingInteractionAdvisor.class)
                    .filter(advisor->compatible(advisor, context))
                    .forEach(advisor->{
                        _Strings.nonEmpty(advisor.hides(visibilityContext))
                            .map(Consent.VetoReason::explicit)
                            .ifPresent(hidingReason->{
                                if(ifHiddenPolicy.isShowAsDisabledWithDiagnostics()) {
                                    hidingReason = VetoUtil.withAdvisorAsDiagnostic(hidingReason, advisor);
                                }
                                builder.addAdvise(hidingReason, advisor);
                            });
                    });
                break;
        }

        var ifDisabledPolicy = context.renderPolicy().ifDisabledPolicy();
        facetHolder.streamFacets(DisablingInteractionAdvisor.class)
            .filter(advisor->compatible(advisor, context))
            .forEach(advisor->{
                advisor.disables(context)
                    .ifPresent(disablingReason->{
                        if(ifDisabledPolicy.isShowAsDisabledWithDiagnostics()) {
                            disablingReason = VetoUtil.withAdvisorAsDiagnostic(disablingReason, advisor);
                        }
                        builder.addAdvise(disablingReason, advisor);
                    });
            });

        return builder.build();
    }

    public InteractionResult isValidResult(final FacetHolder facetHolder, final ValidityContext context) {

        var builder = InteractionResult.builder(context.createInteractionEvent());

        facetHolder.streamFacets(ValidatingInteractionAdvisor.class)
        .filter(advisor->compatible(advisor, context))
        .forEach(advisor->{
            guardAgainstEmptyReasonString(advisor.invalidates(context), context.identifier())
                .map(Consent.VetoReason::explicit)
                .ifPresent(invalidatingReason->builder.addAdvise(invalidatingReason, advisor));
        });

        return builder.build();
    }

    public InteractionResultSet isValidResultSet(
            final FacetHolder facetHolder,
            final ValidityContext context,
            final InteractionResultSet resultSet) {

        return resultSet.add(isValidResult(facetHolder, context));
    }

    // -- HELPER

    /**
     * [CAUSEWAY-3554] an empty String most likely is wrong use of the programming model,
     * we should generate a message,
     * explaining what was going wrong and hinting developers at a possible resolution
     */
    private Optional<String> guardAgainstEmptyReasonString(
            final @Nullable String reason, final @NonNull Identifier identifier) {
        if("".equals(reason)) {
            var msg = ProgrammingModelConstants.MessageTemplate.INVALID_USE_OF_VALIDATION_SUPPORT_METHOD.builder()
                .addVariable("className", identifier.className())
                .addVariable("memberName", identifier.memberLogicalName())
                .buildMessage();
            log.error(msg);
            return Optional.of(msg);
        }
        return Optional.ofNullable(reason);
    }

    private static boolean compatible(final InteractionAdvisor advisor, final InteractionContext ic) {
        if(ic.iConstraint().initiatedBy().isPassThrough()
                && isDomainEventAdvisor(advisor))
			//[CAUSEWAY-3810] when pass-through, then don't trigger any domain events
            return false;
        if(advisor instanceof ActionDomainEventFacet)
			return ic instanceof ActionInteractionContext;
        return true;
    }

    private static boolean isDomainEventAdvisor(final InteractionAdvisor advisor) {
        return advisor instanceof DomainEventFacetAbstract;
    }

}
