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
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.functions._Functions;
import org.apache.isis.commons.internal.functions._Predicates;
import org.apache.isis.metamodel.facets.FacetFactory;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidatorVisiting;

import static org.apache.isis.commons.internal.base._NullSafe.isEmpty;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import lombok.val;

public interface ProgrammingModel {

    // -- TYPES
    
    /**
     * Processing order for registered facet factories
     * 
     * @apiNote Prefixes are without any semantic meaning, just to make the ordering 
     * transparent to the human reader. 
     * Order is defined by {@link ProcessingOrder#ordinal()}
     *
     */
    static enum ProcessingOrder {
        
        A1_FALLBACK_DEFAULTS,
        A2_AFTER_FALLBACK_DEFAULTS,
        
        B1_OBJECT_NAMING,
        B2_AFTER_OBJECT_NAMING,
        
        C1_METHOD_REMOVING,
        C2_AFTER_METHOD_REMOVING,
        
        D1_MANDATORY_SUPPORT,
        D2_AFTER_MANDATORY_SUPPORT,
        
        E1_MEMBER_MODELLING,
        E2_AFTER_MEMBER_MODELLING,
        
        F1_LAYOUT,
        F2_AFTER_LAYOUT,
        
        G1_VALUE_TYPES, 
        G2_AFTER_VALUE_TYPES,
        
        Z0_BEFORE_FINALLY,
        Z1_FINALLY, 
        Z2_AFTER_FINALLY,
    }
    
    static enum Marker {
        DEPRECATED, 
        INCUBATING,
    }
    
    @Value(staticConstructor = "of") @EqualsAndHashCode(of = "type")
    static final class FactoryEntry<T extends FacetFactory> {
        @NonNull Class<T> type;
        @NonNull Supplier<? extends T> supplier; 
        Marker[] markers;
        @Getter(lazy = true) final T factoryInstance = supplier.get();
    }
    
    // -- INTERFACE
    
    List<ObjectSpecificationPostProcessor> getPostProcessors();
    void addValidator(MetaModelValidator validator);
    
    <T extends FacetFactory> void add(
            ProcessingOrder order, 
            Class<T> type, 
            Supplier<? extends T> supplier, 
            Marker ... markers);
    
    
    Stream<FacetFactory> stream();
    
    // -- SHORTCUTS
    
    default <T extends FacetFactory> void add(
            ProcessingOrder order, 
            Class<T> type, 
            Marker ... markers) {
        
        final Supplier<FacetFactory> supplier = _Functions.uncheckedSupplier(type::newInstance);
        add(order, type, _Casts.uncheckedCast(supplier), markers);
    }
    
    default void addValidator(MetaModelValidatorVisiting.Visitor visitor) {
        addValidator(MetaModelValidatorVisiting.of(visitor));
    }
    
    // -- PREDICATES
    
    public static Predicate<FactoryEntry<?>> excludingNone() {
        return _Predicates.alwaysTrue();
    }
    
    public static Predicate<FactoryEntry<?>> excluding(@Nullable EnumSet<Marker> excludingMarkers) {
        if(excludingMarkers==null) {
            return excludingNone();
        }
        return factoryEntry -> {
            val markersOnFactory = factoryEntry.getMarkers();
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
