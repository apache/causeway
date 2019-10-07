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
package org.apache.isis.metamodel.progmodel;

import java.util.EnumSet;
import java.util.function.Predicate;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.stereotype.Component;

import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.metamodel.facets.FacetFactory;
import org.apache.isis.metamodel.progmodel.ProgrammingModel.Marker;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidator;

import lombok.val;

/**
 * 
 * @since 2.0
 *
 */
@Component
public class ProgrammingModelInitFilterDefault implements ProgrammingModelInitFilter {

    @Inject private IsisConfiguration configuration;
    
    private Predicate<Marker[]> filterOnMarker = ProgrammingModelInitFilter.excludingNone(); 
            
    @PostConstruct
    public void init() {
        val isIgnoreDeprecated = 
                configuration.getReflector().getFacets().isIgnoreDeprecated();
        
        this.filterOnMarker = isIgnoreDeprecated 
                ? ProgrammingModelInitFilter.excluding(EnumSet.of(ProgrammingModel.Marker.DEPRECATED)) 
                        : ProgrammingModelInitFilter.excludingNone(); 
    }

    @Override
    public boolean acceptFactoryType(
            Class<? extends FacetFactory> factoryType, 
            Marker[] markersIfAny) {
        
        return filterOnMarker.test(markersIfAny);
    }
    
    @Override
    public boolean acceptValidator(
            Class<? extends MetaModelValidator> validatorType, 
            Marker[] markersIfAny) {
        
        return filterOnMarker.test(markersIfAny);
    }

    @Override
    public boolean acceptPostProcessor(
            Class<? extends ObjectSpecificationPostProcessor> postProcessorType,
            Marker[] markersIfAny) {
        
        return filterOnMarker.test(markersIfAny);
    }



}
