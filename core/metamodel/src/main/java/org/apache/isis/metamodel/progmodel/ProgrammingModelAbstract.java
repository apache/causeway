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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Multimaps;
import org.apache.isis.commons.internal.collections._Multimaps.SetMultimap;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.metamodel.facets.FacetFactory;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidatorComposite;

import lombok.Getter;
import lombok.val;

public abstract class ProgrammingModelAbstract implements ProgrammingModel {

    @Getter(onMethod = @__(@Override))
    private final List<ObjectSpecificationPostProcessor> postProcessors = _Lists.newArrayList();
    
    private List<FacetFactory> unmodifiableFactories;

    @Override
    public <T extends FacetFactory> void add(
            ProcessingOrder order, 
            Class<T> type, 
            Supplier<? extends T> supplier, 
            Marker ... markers) {
        
        assertNotInitialized();
        
        val factoryEntry = FactoryEntry.of(type, supplier, markers);
        factoryEntriesByOrder.putElement(order, factoryEntry);
    }

    @Override
    public void init(
            Predicate<FactoryEntry<?>> filter, 
            MetaModelValidatorComposite metaModelValidator) {
        
        assertNotInitialized();
        
        for (val facetFactory : snapshot(filter)) {
            if(facetFactory instanceof MetaModelValidatorRefiner) {
                val metaModelValidatorRefiner = (MetaModelValidatorRefiner) facetFactory;
                metaModelValidatorRefiner.refineMetaModelValidator(metaModelValidator);
            }
        }
        
        this.unmodifiableFactories = 
                Collections.unmodifiableList(snapshot(filter));
    }

    @Override
    public Stream<FacetFactory> stream() {
        if(unmodifiableFactories==null) {
            return Stream.empty();
        }
        return unmodifiableFactories.stream();
    }
    
    // -- HELPER

    private boolean isInitialized() {
        return unmodifiableFactories!=null;
    }
    
    private void assertNotInitialized() {
        if(isInitialized()) {
            throw _Exceptions.unrecoverable(
                    "The programming-model was already initialized, it cannot be altered.");
        }
    }
    
    private final SetMultimap<ProcessingOrder, FactoryEntry<?>> 
        factoryEntriesByOrder = _Multimaps.newSetMultimap(LinkedHashSet::new);
    
    private List<FacetFactory> snapshot(Predicate<FactoryEntry<?>> filter) {
        val factories = _Lists.<FacetFactory>newArrayList();
        for(val processinOrder : ProcessingOrder.values()) {
            val factoryEntrySet = factoryEntriesByOrder.get(processinOrder);
            if(factoryEntrySet==null) {
                continue;
            }
            for(val factoryEntry : factoryEntrySet) {
                if(filter.test(factoryEntry)) {
                    factories.add(factoryEntry.getFactoryInstance());
                }
            }
        }
        return factories;
    }

}
