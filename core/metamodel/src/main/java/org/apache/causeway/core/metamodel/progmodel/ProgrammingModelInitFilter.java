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
package org.apache.causeway.core.metamodel.progmodel;

import java.util.EnumSet;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.commons.internal.functions._Predicates;
import org.apache.causeway.core.metamodel.facets.FacetFactory;
import org.apache.causeway.core.metamodel.postprocessors.MetaModelPostProcessor;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModel.Marker;
import org.apache.causeway.core.metamodel.specloader.validator.MetaModelValidator;

import static org.apache.causeway.commons.internal.base._NullSafe.isEmpty;

/**
 * @since 2.0
 */
public interface ProgrammingModelInitFilter {

    public static ProgrammingModelInitFilter noop() {
        return new ProgrammingModelInitFilter() {
            @Override public boolean acceptValidator(final Class<? extends MetaModelValidator> validatorType, final Marker[] markersIfAny) {
                return true;
            }
            @Override public boolean acceptPostProcessor(final Class<? extends MetaModelPostProcessor> postProcessorType,
                    final Marker[] markersIfAny) {
                return true;
            }
            @Override public boolean acceptFactoryType(final Class<? extends FacetFactory> factoryType, final Marker[] markersIfAny) {
                return true;
            }
        };
    }

    boolean acceptFactoryType(
            Class<? extends FacetFactory> factoryType,
            ProgrammingModel.Marker[] markersIfAny);

    boolean acceptValidator(
            Class<? extends MetaModelValidator> validatorType,
            ProgrammingModel.Marker[] markersIfAny);

    boolean acceptPostProcessor(
            Class<? extends MetaModelPostProcessor> postProcessorType,
            ProgrammingModel.Marker[] markersIfAny);

    // -- PREDEFINED PREDICATES

    public static Predicate<ProgrammingModel.Marker[]> excludingNone() {
        return _Predicates.alwaysTrue();
    }

    public static Predicate<ProgrammingModel.Marker[]> excluding(final @Nullable EnumSet<ProgrammingModel.Marker> excludingMarkers) {
        if(excludingMarkers==null)  return excludingNone();

        return markersOnFactory -> {
            if(isEmpty(markersOnFactory)) return true; // accept
            for(var markerOnFactory : markersOnFactory) {
                if(excludingMarkers.contains(markerOnFactory)) return false; // don't accept
            }
            return true; // accept
        };
    }

}
