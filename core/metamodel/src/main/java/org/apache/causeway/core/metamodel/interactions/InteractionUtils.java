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

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.environment.DeploymentType;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants;
import org.apache.causeway.core.metamodel.consent.Consent;
import org.apache.causeway.core.metamodel.consent.InteractionAdvisor;
import org.apache.causeway.core.metamodel.consent.InteractionResult;
import org.apache.causeway.core.metamodel.consent.InteractionResultSet;
import org.apache.causeway.core.metamodel.consent.VetoUtil;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.DomainEventFacetAbstract;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

@UtilityClass
@Log4j2
public final class InteractionUtils {

    public InteractionResult isVisibleResult(final FacetHolder facetHolder, final VisibilityContext context) {

        var iaResult = new InteractionResult(context.createInteractionEvent());

        // depending on the ifHiddenPolicy, we may do no vetoing here (instead, it moves into the usability check).
        var ifHiddenPolicy = context.getRenderPolicy().getIfHiddenPolicy();
        switch (ifHiddenPolicy) {
            case HIDE:
                facetHolder.streamFacets(HidingInteractionAdvisor.class)
                .filter(advisor->compatible(advisor, context))
                .forEach(advisor->{
                    var hidingReasonString = advisor.hides(context);
                    var hidingReason = Optional.ofNullable(hidingReasonString)
                            .map(Consent.VetoReason::explicit)
                            .orElse(null);

                    iaResult.advise(hidingReason, advisor);
                });
                break;
            case SHOW_AS_DISABLED:
            case SHOW_AS_DISABLED_WITH_DIAGNOSTICS:
            default:
                break;
        }

        return iaResult;
    }

    public InteractionResult isUsableResult(final FacetHolder facetHolder, final UsabilityContext context) {

        var isResult = new InteractionResult(context.createInteractionEvent());

        // depending on the ifHiddenPolicy, we additionally may disable using a hidden advisor
        var ifHiddenPolicy = context.getRenderPolicy().getIfHiddenPolicy();
        switch (ifHiddenPolicy) {
            case HIDE:
                break;
            case SHOW_AS_DISABLED:
            case SHOW_AS_DISABLED_WITH_DIAGNOSTICS:
                var visibilityContext = context.asVisibilityContext();
                facetHolder.streamFacets(HidingInteractionAdvisor.class)
                        .filter(advisor->compatible(advisor, context))
                        .forEach(advisor->{
                            String hidingReasonString = advisor.hides(visibilityContext);
                            Consent.VetoReason hidingReason = Optional.ofNullable(hidingReasonString)
                                    .map(Consent.VetoReason::explicit)
                                    .orElse(null);
                            if(hidingReason != null
                                    && ifHiddenPolicy.isShowAsDisabledWithDiagnostics()) {
                                hidingReason = VetoUtil.withAdvisorAsDiagnostic(hidingReason, advisor);
                            }
                            isResult.advise(hidingReason, advisor);
                        });
                break;
        }

        var ifDisabledPolicy = context.getRenderPolicy().getIfDisabledPolicy();
        facetHolder.streamFacets(DisablingInteractionAdvisor.class)
        .filter(advisor->compatible(advisor, context))
        .forEach(advisor->{
            Consent.VetoReason disablingReason = advisor.disables(context).orElse(null);
            if(disablingReason != null
                    && ifDisabledPolicy.isShowAsDisabledWithDiagnostics()) {
                disablingReason = VetoUtil.withAdvisorAsDiagnostic(disablingReason, advisor);
            }
            isResult.advise(disablingReason, advisor);
        });

        return isResult;
    }

    public InteractionResult isValidResult(final FacetHolder facetHolder, final ValidityContext context) {

        var iaResult = new InteractionResult(context.createInteractionEvent());

        facetHolder.streamFacets(ValidatingInteractionAdvisor.class)
        .filter(advisor->compatible(advisor, context))
        .forEach(advisor->{
            var invalidatingReasonString =
                    guardAgainstEmptyReasonString(advisor.invalidates(context), context.getIdentifier());

            var invalidatingReason = Optional.ofNullable(invalidatingReasonString)
                    .map(Consent.VetoReason::explicit)
                    .orElse(null);
            iaResult.advise(invalidatingReason, advisor);
        });

        return iaResult;
    }

    public InteractionResultSet isValidResultSet(
            final FacetHolder facetHolder,
            final ValidityContext context,
            final InteractionResultSet resultSet) {

        return resultSet.add(isValidResult(facetHolder, context));
    }

    public RenderPolicy renderPolicy(final ManagedObject ownerAdapter) {
        return new RenderPolicy(
                determineIfHiddenPolicyFrom(ownerAdapter),
                determineIfDisabledPolicyFrom(ownerAdapter));
    }

    // -- HELPER

    /**
     * [CAUSEWAY-3554] an empty String most likely is wrong use of the programming model,
     * we should generate a message,
     * explaining what was going wrong and hinting developers at a possible resolution
     */
    private String guardAgainstEmptyReasonString(
            final @Nullable String reason, final @NonNull Identifier identifier) {
        if("".equals(reason)) {
            var msg = ProgrammingModelConstants.MessageTemplate.INVALID_USE_OF_VALIDATION_SUPPORT_METHOD.builder()
                .addVariable("className", identifier.getClassName())
                .addVariable("memberName", identifier.getMemberLogicalName())
                .buildMessage();
            log.error(msg);
            return msg;
        }
        return reason;
    }

    private static boolean compatible(final InteractionAdvisor advisor, final InteractionContext ic) {
        if(ic.getInitiatedBy().isPassThrough()
                && isDomainEventAdvisor(advisor)) {
            //[CAUSEWAY-3810] when pass-through, then don't trigger any domain events
            return false;
        }
        if(advisor instanceof ActionDomainEventFacet) {
            return ic instanceof ActionInteractionContext;
        }
        return true;
    }

    private static boolean isDomainEventAdvisor(final InteractionAdvisor advisor) {
        return advisor instanceof DomainEventFacetAbstract;
    }

    private CausewayConfiguration.Prototyping.IfHiddenPolicy determineIfHiddenPolicyFrom(final ManagedObject ownerAdapter) {
        DeploymentType deploymentType = ownerAdapter.getSystemEnvironment().getDeploymentType();
        switch (deploymentType) {
            case PROTOTYPING:
                return ownerAdapter.getConfiguration().getPrototyping().getIfHiddenPolicy();
            case PRODUCTION:
            default:
                return CausewayConfiguration.Prototyping.IfHiddenPolicy.HIDE;
        }
    }

    private CausewayConfiguration.Prototyping.IfDisabledPolicy determineIfDisabledPolicyFrom(final ManagedObject ownerAdapter) {
        DeploymentType deploymentType = ownerAdapter.getSystemEnvironment().getDeploymentType();
        switch (deploymentType) {
            case PROTOTYPING:
                return ownerAdapter.getConfiguration().getPrototyping().getIfDisabledPolicy();
            case PRODUCTION:
            default:
                return CausewayConfiguration.Prototyping.IfDisabledPolicy.DISABLE;
        }
    }
}
