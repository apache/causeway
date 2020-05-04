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

import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * The 'head' of a {@link PendingParameterModel}, which in contrast to the (owning) 
 * {@link PendingParameterModel} holds no parameter values.
 *  
 * @since 2.0.0
 */
@Getter 
@RequiredArgsConstructor(staticName = "of")
@Log4j2
public class PendingParameterModelHead {

    @NonNull private final ObjectAction action;
    @NonNull private final ManagedObject actionOwner;
    
    /** 
     * typically equal to {@code actionOwner}, except for mixins, 
     * where {@code actionTarget} is the mixin instance 
     */
    @NonNull private final ManagedObject actionTarget; 
    
    /**  
     * Immutable tuple of ManagedObjects, each representing {@code null} and each holding 
     * the corresponding parameter's {@code ObjectSpecification}.
     * <p>
     * The size of the tuple corresponds to the number of parameters.
     */
    public Can<ManagedObject> getEmptyParameterValues() {
        return getAction().getParameters().stream()
        .map(objectActionParameter->
            ManagedObject.empty(objectActionParameter.getSpecification()))
        .collect(Can.toCan());
    }
    
    public PendingParameterModel model(
            @NonNull Can<ManagedObject> paramValues) {
        return PendingParameterModel.of(this, paramValues);
    }
    
    public PendingParameterModel emptyModel() {
        return PendingParameterModel.of(this, getEmptyParameterValues());
    }
    
    /**
     * See step 1 'Fill in defaults' in
     * <a href="https://cwiki.apache.org/confluence/display/ISIS/ActionParameterNegotiation">
     * ActionParameterNegotiation (wiki)
     * </a> 
     */
    public PendingParameterModel defaults() {
        
        // first pass to calculate proposed fixed point
        // second pass to verify we have found a fixed point
        final int maxIterations = 2;  
        
        val params = getAction().getParameters();
        
        // init defaults with empty pending-parameter values 
        val emptyModel = emptyModel();
        val initialDefaults = params
                .map(param->param.getDefault(emptyModel));
        
        // could be a fixed point search here, but we assume, params can only depend on params with lower index
        
        Can<ManagedObject> old_pl, pl = initialDefaults;
        for(int i=0; i<maxIterations; ++i) {
            val ppm = model(pl);
            old_pl = pl;
            pl = params
                    .map(param->param.getDefault(ppm));
            
            if(equals(old_pl, pl)) {
                // fixed point found, return the latest iteration 
                return model(pl);
            }
            
        }
        
        log.warn("Cannot find an initial fixed point for action "
                + "parameter defaults on action %s.", getAction());
        
        return model(pl);
        
    }

    // -- HELPER
    
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
