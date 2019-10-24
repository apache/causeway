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

package org.apache.isis.metamodel.interactions;

import org.apache.isis.metamodel.consent.InteractionResult;
import org.apache.isis.metamodel.consent.InteractionResultSet;
import org.apache.isis.metamodel.facetapi.FacetHolder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InteractionUtils {

    public static InteractionResult isVisibleResult(FacetHolder facetHolder, VisibilityContext<?> context) {
        
        val iaResult = new InteractionResult(context.createInteractionEvent());
        
        facetHolder.streamFacets(HidingInteractionAdvisor.class)
        .forEach(advisor->{
            iaResult.advise(advisor.hides(context), advisor);
        });
        
        return iaResult;
    }

    public static InteractionResult isUsableResult(FacetHolder facetHolder, UsabilityContext<?> context) {
        
        val isResult = new InteractionResult(context.createInteractionEvent());
        
        facetHolder.streamFacets(DisablingInteractionAdvisor.class)
        .forEach(advisor->{
            final String disables = advisor.disables(context);
            isResult.advise(disables, advisor);
        });
        
        return isResult;
    }

    public static InteractionResult isValidResult(FacetHolder facetHolder, ValidityContext<?> context) {
        
        val iaResult = new InteractionResult(context.createInteractionEvent());
        
        facetHolder.streamFacets(ValidatingInteractionAdvisor.class)
        .forEach(advisor->{
            iaResult.advise(advisor.invalidates(context), advisor);
        });
        
        return iaResult;
    }

    public static InteractionResultSet isValidResultSet(
            FacetHolder facetHolder, 
            ValidityContext<?> context, 
            InteractionResultSet resultSet) {
        
        return resultSet.add(isValidResult(facetHolder, context));
    }
  
  
}
