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

package org.apache.isis.core.metamodel.facets.object.autocomplete;

import java.util.Collections;
import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacetUtils;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;

public abstract class AutoCompleteFacetAbstract extends FacetAbstract implements AutoCompleteFacet {

    public static Class<? extends Facet> type() {
        return AutoCompleteFacet.class;
    }

    private final Class<?> repositoryClass;
    private final String actionName;

    private final SpecificationLoader specificationLoader;
    private final AdapterManager adapterManager;
    private final ServicesInjector servicesInjector;

    /**
     * cached once searched for
     */
    private ObjectAction repositoryAction;
    private boolean cachedRepositoryAction = false;
    
    private boolean cachedRepositoryObject = false;
    private Object repository;

    public AutoCompleteFacetAbstract(
            final FacetHolder holder, final Class<?> repositoryClass, final String actionName, final SpecificationLoader specificationLoader, final AdapterManager adapterManager, final ServicesInjector servicesInjector) {
        super(type(), holder, Derivation.NOT_DERIVED);
        this.repositoryClass = repositoryClass;
        this.actionName = actionName;
        this.specificationLoader = specificationLoader;
        this.adapterManager = adapterManager;
        this.servicesInjector = servicesInjector;
    }

    @Override
    public List<ObjectAdapter> execute(final String search) {

        cacheRepositoryAndRepositoryActionIfRequired();
        if(repositoryAction == null || repository == null) {
            return Collections.emptyList();
        }
        
        final ObjectAdapter repositoryAdapter = adapterManager.getAdapterFor(repository);
        final ObjectAdapter searchAdapter = adapterManager.adapterFor(search);
        final ObjectAdapter resultAdapter = repositoryAction.execute(repositoryAdapter, new ObjectAdapter[] { searchAdapter} );
        // check a collection was returned
        if(CollectionFacetUtils.getCollectionFacetFromSpec(resultAdapter) == null) {
            return Collections.emptyList();
        }
        return CollectionFacetUtils.convertToAdapterList(resultAdapter);
    }

    private void cacheRepositoryAndRepositoryActionIfRequired() {
        if(!cachedRepositoryAction) {
            cacheRepositoryAction();
        }
        if(!cachedRepositoryObject) {
            cacheRepositoryObject();
        }
    }

    private void cacheRepositoryAction() {
        try {
            final ObjectSpecification repositorySpec = specificationLoader.loadSpecification(repositoryClass);
            final List<ObjectAction> objectActions = repositorySpec.getObjectActions(ActionType.USER, Contributed.EXCLUDED, ObjectAction.Filters.withId(actionName));

            repositoryAction = objectActions.size() == 1? objectActions.get(0): null;
        } finally {
            cachedRepositoryAction = true;
        }
    }

    private void cacheRepositoryObject() {
        repository = servicesInjector.lookupService(repositoryClass);
        cachedRepositoryObject = true;
    }

}
