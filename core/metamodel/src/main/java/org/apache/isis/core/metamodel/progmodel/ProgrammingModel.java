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

package org.apache.isis.core.metamodel.progmodel;

import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.isis.commons.internal.functions._Functions;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorVisiting;

import lombok.NonNull;

public interface ProgrammingModel {

    // -- ENUM TYPES
    
    enum Marker {
        DEPRECATED, 
        INCUBATING,
        JDO,
        JPA,
    }
    
    /**
     * Processing order for registered facet factories
     * 
     * @apiNote Prefixes are without any semantic meaning, just to make the ordering 
     * transparent to the human reader. 
     * Order is defined by {@link FacetProcessingOrder#ordinal()}
     *
     */
    enum FacetProcessingOrder {
        
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
    enum ValidationOrder {
        
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
    enum PostProcessingOrder {
        
        A0_BEFORE_BUILTIN,
        A1_BUILTIN,
        A2_AFTER_BUILTIN,
        
    }
    
    
    // -- INTERFACE
    
    <T extends FacetFactory> void addFactory(
            FacetProcessingOrder order, 
            T instance, 
            Marker ... markers);
    
    <T extends MetaModelValidator> void addValidator(
            ValidationOrder order, 
            T instance, 
            Marker ... markers);
    
    <T extends ObjectSpecificationPostProcessor> void addPostProcessor(
            PostProcessingOrder order, 
            T instance, 
            Marker ... markers);
    
    
    Stream<FacetFactory> streamFactories();
    Stream<MetaModelValidator> streamValidators();
    Stream<ObjectSpecificationPostProcessor> streamPostProcessors();
    
    // -- SHORTCUTS
    
    /** shortcut for see {@link #addFactory(FacetProcessingOrder, FacetFactory, Marker...)}*/
    default <T extends FacetFactory> void addFactory(
            FacetProcessingOrder order, 
            Class<T> type, 
            Marker ... markers) {
        
        final Supplier<FacetFactory> supplier = _Functions.uncheckedSupplier(type::newInstance);
        addFactory(order, supplier.get(), markers);
    }
    
    /** shortcut for see {@link #addValidator(ValidationOrder, MetaModelValidator, Marker...)} */
    default void addValidator(
            MetaModelValidator validator, 
            Marker ... markers) {
        
        final Supplier<MetaModelValidator> supplier = ()->validator;
        addValidator(ValidationOrder.A2_AFTER_BUILTIN, supplier.get(), markers);
    }

    /** do not include managed beans */
    default void addValidatorIncludeManagedBeans(MetaModelValidatorVisiting.Visitor visitor, Marker ... markers) {
        addValidator(MetaModelValidatorVisiting.of(visitor, 
                    spec -> !spec.getBeanSort().isUnknown()),
                markers);
    }
    
    /** do not include managed beans */
    default void addValidatorSkipManagedBeans(MetaModelValidatorVisiting.Visitor visitor, Marker ... markers) {
        addValidator(MetaModelValidatorVisiting.of(visitor, 
                    spec -> !spec.isManagedBean() && !spec.getBeanSort().isUnknown()),
                markers);
    }
    
    default void addValidator(final MetaModelValidatorVisiting.Visitor visitor,
                              final @NonNull Predicate<ObjectSpecification> specPredicate,
                              final Marker ... markers) {
        addValidator(MetaModelValidatorVisiting.of(visitor, specPredicate), markers);
    }

    /** shortcut for see {@link #addPostProcessor(PostProcessingOrder, ObjectSpecificationPostProcessor, Marker...)}*/
    default <T extends ObjectSpecificationPostProcessor> void addPostProcessor(
            PostProcessingOrder order, 
            Class<T> type, 
            Marker ... markers) {
        
        final Supplier<ObjectSpecificationPostProcessor> supplier = _Functions.uncheckedSupplier(type::newInstance);
        addPostProcessor(order, supplier.get(), markers);
    }

    
}
