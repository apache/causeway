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
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants;
import org.apache.causeway.core.metamodel.consent.Consent;
import org.apache.causeway.core.metamodel.consent.InteractionAdvisor;
import org.apache.causeway.core.metamodel.consent.InteractionResult;
import org.apache.causeway.core.metamodel.consent.InteractionResultSet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacet;

import lombok.NonNull;
import lombok.val;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

@UtilityClass
@Log4j2
public final class InteractionUtils {

    public static InteractionResult isVisibleResult(final FacetHolder facetHolder, final VisibilityContext context) {

        val iaResult = new InteractionResult(context.createInteractionEvent());

        facetHolder.streamFacets(HidingInteractionAdvisor.class)
        .filter(advisor->compatible(advisor, context))
        .forEach(advisor->{
            val hidingReasonString = advisor.hides(context);
            val hidingReason = Optional.ofNullable(hidingReasonString)
                    .map(Consent.VetoReason::explicit)
                    .orElse(null);

            iaResult.advise(hidingReason, advisor);
        });

        return iaResult;
    }


    public static InteractionResult isUsableResult(final FacetHolder facetHolder, final UsabilityContext context) {

        val isResult = new InteractionResult(context.createInteractionEvent());

        facetHolder.streamFacets(DisablingInteractionAdvisor.class)
        .filter(advisor->compatible(advisor, context))
        .forEach(advisor->{
            val disablingReason = advisor.disables(context).orElse(null);
            isResult.advise(disablingReason, advisor);
        });

        return isResult;
    }

    public static InteractionResult isValidResult(final FacetHolder facetHolder, final ValidityContext context) {

        val iaResult = new InteractionResult(context.createInteractionEvent());

        facetHolder.streamFacets(ValidatingInteractionAdvisor.class)
        .filter(advisor->compatible(advisor, context))
        .forEach(advisor->{
            val invalidatingReasonString = 
                    guardAgainstEmptyReasonString(advisor.invalidates(context), context.getIdentifier());
            
            val invalidatingReason = Optional.ofNullable(invalidatingReasonString)
                    .map(Consent.VetoReason::explicit)
                    .orElse(null);
            iaResult.advise(invalidatingReason, advisor);
        });

        return iaResult;
    }
    
    public static InteractionResultSet isValidResultSet(
            final FacetHolder facetHolder,
            final ValidityContext context,
            final InteractionResultSet resultSet) {

        return resultSet.add(isValidResult(facetHolder, context));
    }
    
    /**
     * [CAUSEWAY-3554] an empty String most likely is wrong use of the programming model, 
     * we should generate a message, 
     * explaining what was going wrong and hinting developers at a possible resolution
     */
    private static String guardAgainstEmptyReasonString(
            final @Nullable String reason, final @NonNull Identifier identifier) {
        if("".equals(reason)) {
            val msg = ProgrammingModelConstants.Violation.INVALID_USE_OF_VALIDATION_SUPPORT_METHOD.builder()
                .addVariable("className", identifier.getClassName())
                .addVariable("memberName", identifier.getMemberLogicalName())
                .buildMessage();
            log.error(msg);
            return msg;
        }
        return reason;
    }

    private static boolean compatible(final InteractionAdvisor advisor, final InteractionContext ic) {
        if(advisor instanceof ActionDomainEventFacet) {
            return ic instanceof ActionInteractionContext;
        }
        return true;
    }

}
