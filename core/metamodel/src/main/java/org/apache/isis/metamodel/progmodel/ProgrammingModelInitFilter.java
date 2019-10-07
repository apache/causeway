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

import javax.annotation.Nullable;

import org.apache.isis.commons.internal.functions._Predicates;
import org.apache.isis.metamodel.facets.FacetFactory;
import org.apache.isis.metamodel.progmodel.ProgrammingModel.Marker;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidator;

import static org.apache.isis.commons.internal.base._NullSafe.isEmpty;

import lombok.val;

/**
 * 
 * @since 2.0
 *
 */
public interface ProgrammingModelInitFilter {

    boolean acceptFactoryType(
            Class<? extends FacetFactory> factoryType, 
            ProgrammingModel.Marker[] markersIfAny);
    
    boolean acceptValidator(
            Class<? extends MetaModelValidator> validatorType, 
            ProgrammingModel.Marker[] markersIfAny);
    
    boolean acceptPostProcessor(
            Class<? extends ObjectSpecificationPostProcessor> postProcessorType, 
            ProgrammingModel.Marker[] markersIfAny);    
    
    // -- PREDEFINED PREDICATES
    
    public static Predicate<Marker[]> excludingNone() {
        return _Predicates.alwaysTrue();
    }
    
    public static Predicate<Marker[]> excluding(@Nullable EnumSet<Marker> excludingMarkers) {
        if(excludingMarkers==null) {
            return excludingNone();
        }
        return markersOnFactory -> {
            if(isEmpty(markersOnFactory)) {
                return true; // accept
            }
            for(val markerOnFactory : markersOnFactory) {
                if(excludingMarkers.contains(markerOnFactory)) {
                    return true; // don't  accept
                }
            }
            return true; // accept
        };
    }





}
