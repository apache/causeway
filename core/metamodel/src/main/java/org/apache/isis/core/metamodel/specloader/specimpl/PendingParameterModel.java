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
package org.apache.isis.core.metamodel.specloader.specimpl;

import java.util.function.Function;

import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.metamodel.facets.param.defaults.ActionParameterDefaultsFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Model used to negotiate the paramValues of an action by means of a UI dialog.
 *  
 * @since 2.0.0
 */
@Getter 
@RequiredArgsConstructor(staticName = "of")
public class PendingParameterModel {

    @NonNull private final ObjectAction action;
    @NonNull private final ManagedObject actionOwner;
    
    /** 
     * typically equal to {@code actionOwner}, except for mixins, where {@code actionTarget}
     * is the mixin instance 
     */
    @NonNull private final ManagedObject actionTarget; 
    
    /**
     * Has special meaning when empty, that is, this instance is only used
     * to initialize phase 1 in step 1 of 'Fill in defaults' see
     * <a href="https://cwiki.apache.org/confluence/display/ISIS/ActionParameterNegotiation">
     * ActionParameterNegotiation (wiki)
     * </a> 
     */
    @NonNull private final Can<ManagedObject> paramValues;
    
    public final boolean isPopulated() {
        return !paramValues.isEmpty();
    }
    

    public PendingParameterModel defaultsFixedPointSearch() {
        
        val paramDefaultProviders = getParameterDefaultProviders();
        
        val initialDefaults = paramDefaultProviders
        .map(paramDefaultProvider->paramDefaultProvider.getDefault(this));
        
        //TODO do fixed point search

        return PendingParameterModel.of(action, actionOwner, actionTarget, initialDefaults);
    }
    
    // -- HELPER
    
    @RequiredArgsConstructor(staticName = "of")
    private static final class ParameterDefaultProvider {
        
        final ObjectSpecification paramSpec;
        final Function<PendingParameterModel, Object> defaultPojoProvider;
        
        ManagedObject getDefault(PendingParameterModel ppm) {
            return ManagedObject.of(paramSpec, defaultPojoProvider.apply(ppm));
        }
    }
    
    private Can<ParameterDefaultProvider> getParameterDefaultProviders() {
        return action.getParameters().stream()
        .map(objectActionParameter->{
            val paramSpec = objectActionParameter.getSpecification();
            val paramDefaultFacet = objectActionParameter.getFacet(ActionParameterDefaultsFacet.class);
            return (paramDefaultFacet != null && !paramDefaultFacet.isFallback()) 
                        ? ParameterDefaultProvider.of(paramSpec, ppm->paramDefaultFacet.getDefault(ppm))
                        : ParameterDefaultProvider.of(paramSpec, ppm->null);
        })
        .collect(Can.toCan());
    }
    
    
    
}
