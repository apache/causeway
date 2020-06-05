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

package org.apache.isis.core.metamodel.interactions;

import org.apache.isis.core.metamodel.consent.InteractionAdvisor;
import org.apache.isis.core.metamodel.consent.InteractionResult;
import org.apache.isis.core.metamodel.consent.InteractionResultSet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacet;

import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class InteractionUtils {

    public static InteractionResult isVisibleResult(FacetHolder facetHolder, VisibilityContext context) {
        
        val iaResult = new InteractionResult(context.createInteractionEvent());
        
        facetHolder.streamFacets(HidingInteractionAdvisor.class)
        .filter(advisor->compatible(advisor, context))
        .forEach(advisor->{
            val hidingReason = advisor.hides(context);
            iaResult.advise(hidingReason, advisor);
        });
        
        return iaResult;
    }


    public static InteractionResult isUsableResult(FacetHolder facetHolder, UsabilityContext context) {
        
        val isResult = new InteractionResult(context.createInteractionEvent());
        
        facetHolder.streamFacets(DisablingInteractionAdvisor.class)
        .filter(advisor->compatible(advisor, context))
        .forEach(advisor->{
            val disablingReason = advisor.disables(context);
            isResult.advise(disablingReason, advisor);
        });
        
        return isResult;
    }

    public static InteractionResult isValidResult(FacetHolder facetHolder, ValidityContext context) {
        
        val iaResult = new InteractionResult(context.createInteractionEvent());
        
        facetHolder.streamFacets(ValidatingInteractionAdvisor.class)
        .filter(advisor->compatible(advisor, context))
        .forEach(advisor->{
            val invalidatingReason = advisor.invalidates(context); 
            iaResult.advise(invalidatingReason, advisor);
        });
        
        return iaResult;
    }

    public static InteractionResultSet isValidResultSet(
            FacetHolder facetHolder, 
            ValidityContext context, 
            InteractionResultSet resultSet) {
        
        return resultSet.add(isValidResult(facetHolder, context));
    }
    
    private static boolean compatible(InteractionAdvisor advisor, InteractionContext ic) {
        
        if(advisor instanceof ActionDomainEventFacet) {
            return ic instanceof ActionInteractionContext;
        }
        
        return true;
    }
  
}
