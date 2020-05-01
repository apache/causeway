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

import java.util.Objects;
import java.util.function.Function;

import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.internal.assertions._Assert;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
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
    
    public Can<ManagedObject> getEmptyValues() {
        return getAction().getParameters().stream()
        .map(objectActionParameter->
            ManagedObject.empty(objectActionParameter.getSpecification()))
        .collect(Can.toCan());
    }

    public PendingParameterModel defaultsFixedPointSearch() {
        
        _Assert.assertTrue(isPopulated()); // don't start this algorithm with a populated model 
        
        final int maxIterations = getAction().getParameterCount();
        val paramDefaultProviders = getParameterDefaultProviders();
        
        val initialDefaults = paramDefaultProviders
        .map(paramDefaultProvider->paramDefaultProvider.getDefault(this));
        
        // fixed point search
        
        Can<ManagedObject> old_pl, pl = initialDefaults;
        for(int i=0; i<maxIterations; ++i) {
            val ppm = PendingParameterModel.of(action, actionOwner, actionTarget, pl);
            old_pl = pl;
            pl = paramDefaultProviders
                    .map(paramDefaultProvider->paramDefaultProvider.getDefault(ppm));
            
            if(equals(old_pl, pl)) {
                // fixed point found, return the latest iteration 
                return PendingParameterModel.of(action, actionOwner, actionTarget, pl);
            }
            
        }
        
        throw _Exceptions.unrecoverableFormatted("Cannot find an initial fixed point for action "
                + "parameter defaults on action %s.", getAction());
        
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
        return getAction().getParameters().stream()
        .map(objectActionParameter->{
            val paramSpec = objectActionParameter.getSpecification();
            val paramDefaultFacet = objectActionParameter.getFacet(ActionParameterDefaultsFacet.class);
            return (paramDefaultFacet != null && !paramDefaultFacet.isFallback()) 
                        ? ParameterDefaultProvider.of(paramSpec, ppm->paramDefaultFacet.getDefault(ppm))
                        : ParameterDefaultProvider.of(paramSpec, ppm->null);
        })
        .collect(Can.toCan());
    }
    
    private boolean equals(Can<ManagedObject> left, Can<ManagedObject> right) {
        // equal length is guaranteed as used only local to this class
        val leftIt = left.iterator();
        for(val r : right) {
            val leftPojo = leftIt.next().getPojo();
            val rightPojo = r.getPojo();
            if(!Objects.equals(leftPojo, rightPojo)){
                return false;        
            }
        }
        return true;
    }



    
}
