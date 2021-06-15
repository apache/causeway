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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Stream;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Multimaps;
import org.apache.isis.commons.internal.collections._Multimaps.SetMultimap;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.context.HasMetaModelContext;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.context.MetaModelContextAware;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.postprocessors.ObjectSpecificationPostProcessor;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidator;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;
import lombok.val;

public abstract class ProgrammingModelAbstract
implements
        ProgrammingModel,
        HasMetaModelContext {

    @Getter(onMethod_ = {@Override})
    private final MetaModelContext metaModelContext;

    private List<FacetFactory> unmodifiableFactories;
    private List<MetaModelValidator> unmodifiableValidators;
    private List<ObjectSpecificationPostProcessor> unmodifiablePostProcessors;

    protected ProgrammingModelAbstract(final MetaModelContext metaModelContext) {
        this.metaModelContext = metaModelContext;
    }

    /**
     * Finalizes the factory collection, can not be modified afterwards.
     * @param filter - the final programming model will only contain factories accepted by this filter
     */
    public void init(final ProgrammingModelInitFilter filter) {

        assertNotInitialized();

        // for all registered facet-factories that also implement MetaModelRefiner
        for (val facetFactory : snapshotFactories(filter, metaModelContext)) {

            if(facetFactory instanceof MetaModelRefiner) {
                val metaModelValidatorRefiner = (MetaModelRefiner) facetFactory;
                metaModelValidatorRefiner.refineProgrammingModel(this);
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
        val factoryEntry = ProgrammingModelEntry.of(instance, markers);
        factoryEntriesByOrder.putElement(order, factoryEntry);
    }

    @Override
    public <T extends MetaModelValidator> void addValidator(
            final ValidationOrder order,
            final T instance,
            final Marker... markers) {

        assertNotInitialized();
        metaModelContext.getServiceInjector().injectServicesInto(instance);
        val validatorEntry = ProgrammingModelEntry.of(instance, markers);
        validatorEntriesByOrder.putElement(order, validatorEntry);
    }

    @Override
    public <T extends ObjectSpecificationPostProcessor> void addPostProcessor(
            final PostProcessingOrder order,
            final T instance,
            final Marker... markers) {

        assertNotInitialized();
        metaModelContext.getServiceInjector().injectServicesInto(instance);
        val postProcessorEntry = ProgrammingModelEntry.of(instance, markers);
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
    public Stream<ObjectSpecificationPostProcessor> streamPostProcessors() {
        assertInitialized();
        return unmodifiablePostProcessors.stream();
    }

    // -- VALUE TYPE

    @Value(staticConstructor = "of") @EqualsAndHashCode(of = "instance")
    static final class ProgrammingModelEntry<T> {
        T instance;
        Marker[] markers;
    }

    // -- SNAPSHOT HELPER

    private final SetMultimap<FacetProcessingOrder, ProgrammingModelEntry<? extends FacetFactory>>
        factoryEntriesByOrder = _Multimaps.newSetMultimap(LinkedHashSet::new);

    private List<FacetFactory> snapshotFactories(
            final ProgrammingModelInitFilter filter,
            final MetaModelContext metaModelContext) {

        val factories = _Lists.<FacetFactory>newArrayList();
        for(val order : FacetProcessingOrder.values()) {
            val factoryEntrySet = factoryEntriesByOrder.get(order);
            if(factoryEntrySet==null) {
                continue;
            }
            for(val factoryEntry : factoryEntrySet) {
                if(filter.acceptFactoryType(factoryEntry.getInstance().getClass(), factoryEntry.getMarkers())) {
                    factories.add(honorMetaModelContextAwareness(
                            factoryEntry.getInstance(), metaModelContext));
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

        val validators = _Lists.<MetaModelValidator>newArrayList();
        for(val order : ValidationOrder.values()) {
            val validatorEntrySet = validatorEntriesByOrder.get(order);
            if(validatorEntrySet==null) {
                continue;
            }
            for(val validatorEntry : validatorEntrySet) {
                if(filter.acceptValidator(validatorEntry.getInstance().getClass(), validatorEntry.getMarkers())) {

                    validators.add(honorMetaModelContextAwareness(
                            validatorEntry.getInstance(), metaModelContext));

                }
            }
        }
        return validators;
    }

    private final SetMultimap<PostProcessingOrder, ProgrammingModelEntry<? extends ObjectSpecificationPostProcessor>>
        postProcessorEntriesByOrder = _Multimaps.newSetMultimap(LinkedHashSet::new);

    private List<ObjectSpecificationPostProcessor> snapshotPostProcessors(
            final ProgrammingModelInitFilter filter,
            final MetaModelContext metaModelContext) {

        val postProcessors = _Lists.<ObjectSpecificationPostProcessor>newArrayList();
        for(val order : PostProcessingOrder.values()) {
            val postProcessorEntrySet = postProcessorEntriesByOrder.get(order);
            if(postProcessorEntrySet==null) {
                continue;
            }
            for(val postProcessorEntry : postProcessorEntrySet) {
                if(filter.acceptPostProcessor(postProcessorEntry.getInstance().getClass(), postProcessorEntry.getMarkers())) {

                    postProcessors.add(honorMetaModelContextAwareness(
                            postProcessorEntry.getInstance(), metaModelContext));
                }
            }
        }
        return postProcessors;
    }

    // -- INIT HELPER

    private boolean isInitialized() {
        return unmodifiableFactories!=null;
    }

    private void assertNotInitialized() {
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

    // -- METAMODEL CONTEXT AWARE

    private static <T> T honorMetaModelContextAwareness(final T pojo, final MetaModelContext metaModelContext) {
        if(pojo instanceof MetaModelContextAware) {
            val contextAware = (MetaModelContextAware) pojo;
            contextAware.setMetaModelContext(metaModelContext);
        }
        return pojo;
    }




}
