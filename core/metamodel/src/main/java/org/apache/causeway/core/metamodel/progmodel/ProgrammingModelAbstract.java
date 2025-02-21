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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.collections._Multimaps;
import org.apache.causeway.commons.internal.collections._Multimaps.SetMultimap;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.causeway.core.metamodel.facets.FacetFactory;
import org.apache.causeway.core.metamodel.postprocessors.MetaModelPostProcessor;
import org.apache.causeway.core.metamodel.specloader.validator.MetaModelValidator;

import lombok.Getter;

public abstract class ProgrammingModelAbstract
implements
    ProgrammingModel,
    HasMetaModelContext {

    @Getter(onMethod_ = {@Override})
    private final MetaModelContext metaModelContext;

    private List<FacetFactory> unmodifiableFactories;
    private List<MetaModelValidator> unmodifiableValidators;
    private List<MetaModelPostProcessor> unmodifiablePostProcessors;

    protected ProgrammingModelAbstract(final MetaModelContext metaModelContext) {
        this.metaModelContext = metaModelContext;
    }

    /**
     * Finalizes the factory collection, can not be modified afterwards.
     * @param filter - the final programming model will only contain factories accepted by this filter
     */
    public void init(final ProgrammingModelInitFilter filter) {
        if(isInitialized()) return;

        // for all registered facet-factories that also implement MetaModelRefiner
        for (var facetFactory : snapshotFactories(filter, metaModelContext)) {
            if(facetFactory instanceof MetaModelRefiner metaModelRefiner) {
                metaModelRefiner.refineProgrammingModel(this);
            }
        }

        this.unmodifiableFactories =
                Collections.unmodifiableList(snapshotFactories(filter, metaModelContext));
        this.unmodifiableValidators =
                Collections.unmodifiableList(snapshotValidators(filter, metaModelContext));
        this.unmodifiablePostProcessors =
                Collections.unmodifiableList(snapshotPostProcessors(filter, metaModelContext));
    }

    // -- SETUP

    @Override
    public <T extends FacetFactory> void addFactory(
            final FacetProcessingOrder order,
            final T instance,
            final Marker ... markers) {

        assertNotInitialized();
        metaModelContext.getServiceInjector().injectServicesInto(instance);
        var factoryEntry = new ProgrammingModelEntry<>(instance, markers);
        factoryEntriesByOrder.putElement(order, factoryEntry);
    }

    @Override
    public <T extends MetaModelValidator> void addValidator(
            final ValidationOrder order,
            final T instance,
            final Marker... markers) {

        assertNotInitialized();
        metaModelContext.getServiceInjector().injectServicesInto(instance);
        var validatorEntry = new ProgrammingModelEntry<>(instance, markers);
        validatorEntriesByOrder.putElement(order, validatorEntry);
    }

    @Override
    public <T extends MetaModelPostProcessor> void addPostProcessor(
            final PostProcessingOrder order,
            final T instance,
            final Marker... markers) {

        assertNotInitialized();
        metaModelContext.getServiceInjector().injectServicesInto(instance);
        var postProcessorEntry = new ProgrammingModelEntry<>(instance, markers);
        postProcessorEntriesByOrder.putElement(order, postProcessorEntry);
    }

    // -- ACCESS REGISTERED INSTANCES

    @Override
    public Stream<FacetFactory> streamFactories() {
        assertInitialized();
        return unmodifiableFactories.stream();
    }

    @Override
    public Stream<MetaModelValidator> streamValidators() {
        assertInitialized();
        return unmodifiableValidators.stream();
    }

    @Override
    public Stream<MetaModelPostProcessor> streamPostProcessors() {
        assertInitialized();
        return unmodifiablePostProcessors.stream();
    }

    // -- VALUE TYPE

    record ProgrammingModelEntry<T>(
            T instance,
            Marker[] markers) {
        @Override
        public final boolean equals(final Object obj) {
            return obj instanceof ProgrammingModelEntry other
                    ? Objects.equals(this.instance(), other.instance())
                    : false;
        }
        @Override
        public final int hashCode() {
            return Objects.hash(instance);
        }
    }

    // -- SNAPSHOT HELPER

    private final SetMultimap<FacetProcessingOrder, ProgrammingModelEntry<? extends FacetFactory>>
        factoryEntriesByOrder = _Multimaps.newSetMultimap(LinkedHashSet::new);

    private List<FacetFactory> snapshotFactories(
            final ProgrammingModelInitFilter filter,
            final MetaModelContext metaModelContext) {

        var factories = _Lists.<FacetFactory>newArrayList();
        for(var order : FacetProcessingOrder.values()) {
            var factoryEntrySet = factoryEntriesByOrder.get(order);
            if(factoryEntrySet==null)  continue;
            for(var factoryEntry : factoryEntrySet) {
                if(filter.acceptFactoryType(factoryEntry.instance().getClass(), factoryEntry.markers())) {
                    factories.add(factoryEntry.instance());
                }
            }
        }
        return factories;
    }

    private final SetMultimap<ValidationOrder, ProgrammingModelEntry<? extends MetaModelValidator>>
        validatorEntriesByOrder = _Multimaps.newSetMultimap(LinkedHashSet::new);

    private List<MetaModelValidator> snapshotValidators(
            final ProgrammingModelInitFilter filter,
            final MetaModelContext metaModelContext) {

        var validators = _Lists.<MetaModelValidator>newArrayList();
        for(var order : ValidationOrder.values()) {
            var validatorEntrySet = validatorEntriesByOrder.get(order);
            if(validatorEntrySet==null) continue;

            for(var validatorEntry : validatorEntrySet) {
                if(filter.acceptValidator(validatorEntry.instance().getClass(), validatorEntry.markers())) {
                    validators.add(validatorEntry.instance());
                }
            }
        }
        return validators;
    }

    private final SetMultimap<PostProcessingOrder, ProgrammingModelEntry<? extends MetaModelPostProcessor>>
        postProcessorEntriesByOrder = _Multimaps.newSetMultimap(LinkedHashSet::new);

    private List<MetaModelPostProcessor> snapshotPostProcessors(
            final ProgrammingModelInitFilter filter,
            final MetaModelContext metaModelContext) {

        var postProcessors = _Lists.<MetaModelPostProcessor>newArrayList();
        for(var order : PostProcessingOrder.values()) {
            var postProcessorEntrySet = postProcessorEntriesByOrder.get(order);
            if(postProcessorEntrySet==null) continue;

            for(var postProcessorEntry : postProcessorEntrySet) {
                if(filter.acceptPostProcessor(
                        postProcessorEntry.instance().getClass(),
                        postProcessorEntry.markers())) {
                    postProcessors.add(postProcessorEntry.instance());
                }
            }
        }
        return postProcessors;
    }

    // -- INIT HELPER

    private boolean isInitialized() {
        return unmodifiableFactories!=null;
    }

    protected void assertNotInitialized() {
        if(isInitialized()) {
            throw _Exceptions.unrecoverable(
                    "The programming-model was already initialized, it cannot be altered.");
        }
    }

    private void assertInitialized() {
        if(!isInitialized()) {
            throw _Exceptions.unrecoverable(
                    "The programming-model was not initialized yet.");
        }
    }

}
