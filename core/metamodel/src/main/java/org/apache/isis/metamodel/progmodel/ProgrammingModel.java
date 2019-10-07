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

import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.functions._Functions;
import org.apache.isis.metamodel.facets.FacetFactory;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidatorVisiting;

import lombok.val;

public interface ProgrammingModel {

    // -- ENUM TYPES
    
    static enum Marker {
        DEPRECATED, 
        INCUBATING,
        JDO,
    }
    
    /**
     * Processing order for registered facet factories
     * 
     * @apiNote Prefixes are without any semantic meaning, just to make the ordering 
     * transparent to the human reader. 
     * Order is defined by {@link FacetProcessingOrder#ordinal()}
     *
     */
    static enum FacetProcessingOrder {
        
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

    /**
     * Processing order for registered meta-model validators
     * 
     * @apiNote Prefixes are without any semantic meaning, just to make the ordering 
     * transparent to the human reader. 
     * Order is defined by {@link ValidationOrder#ordinal()}
     *
     */
    static enum ValidationOrder {
        
        A0_BEFORE_BUILTIN,
        A1_BUILTIN,
        A2_AFTER_BUILTIN,
        
    }
    
    /**
     * Processing order for registered meta-model post-processors
     * 
     * @apiNote Prefixes are without any semantic meaning, just to make the ordering 
     * transparent to the human reader. 
     * Order is defined by {@link PostProcessingOrder#ordinal()}
     *
     */
    static enum PostProcessingOrder {
        
        A0_BEFORE_BUILTIN,
        A1_BUILTIN,
        A2_AFTER_BUILTIN,
        
    }
    
    
    // -- INTERFACE
    
    <T extends FacetFactory> void addFactory(
            FacetProcessingOrder order, 
            Class<T> type, 
            Supplier<? extends T> supplier, 
            Marker ... markers);
    
    <T extends MetaModelValidator> void addValidator(
            ValidationOrder order, 
            Class<T> type, 
            Supplier<? extends T> supplier, 
            Marker ... markers);
    
    <T extends ObjectSpecificationPostProcessor> void addPostProcessor(
            PostProcessingOrder order, 
            Class<T> type, 
            Supplier<? extends T> supplier, 
            Marker ... markers);
    
    
    Stream<FacetFactory> streamFactories();
    Stream<MetaModelValidator> streamValidators();
    Stream<ObjectSpecificationPostProcessor> streamPostProcessors();
    
    // -- SHORTCUTS
    
    /** shortcut for see {@link #addFactory(FacetProcessingOrder, Class, Supplier, Marker...)}*/
    default <T extends FacetFactory> void addFactory(
            FacetProcessingOrder order, 
            Class<T> type, 
            Marker ... markers) {
        
        final Supplier<FacetFactory> supplier = _Functions.uncheckedSupplier(type::newInstance);
        addFactory(order, type, _Casts.uncheckedCast(supplier), markers);
    }
    
    /** shortcut for see {@link #addValidator(ValidationOrder, Class, Supplier, Marker...)} */
    default void addValidator(
            MetaModelValidator validator, 
            Marker ... markers) {
        
        val type = validator.getClass();
        final Supplier<MetaModelValidator> supplier = ()->validator;
        addValidator(ValidationOrder.A2_AFTER_BUILTIN, type, _Casts.uncheckedCast(supplier), markers);
    }

    
    /** shortcut for see {@link #addValidator(ValidationOrder, Class, Supplier, Marker...)} */
    default void addValidator(MetaModelValidatorVisiting.Visitor visitor, Marker ... markers) {
        addValidator(MetaModelValidatorVisiting.of(visitor), markers);
    }
    
    /** shortcut for see {@link #addPostProcessor(PostProcessingOrder, Class, Supplier, Marker...)}*/
    default <T extends ObjectSpecificationPostProcessor> void addPostProcessor(
            PostProcessingOrder order, 
            Class<T> type, 
            Marker ... markers) {
        
        final Supplier<ObjectSpecificationPostProcessor> supplier = _Functions.uncheckedSupplier(type::newInstance);
        addPostProcessor(order, type, _Casts.uncheckedCast(supplier), markers);
    }

    
}
