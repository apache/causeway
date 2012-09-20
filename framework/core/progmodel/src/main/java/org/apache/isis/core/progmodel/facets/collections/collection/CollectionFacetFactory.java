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

package org.apache.isis.core.progmodel.facets.collections.collection;

import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManagerAware;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.facets.typeof.TypeOfFacetDefaultToObject;
import org.apache.isis.core.metamodel.facets.typeof.TypeOfFacetInferredFromArray;
import org.apache.isis.core.metamodel.facets.typeof.TypeOfFacetInferredFromGenerics;
import org.apache.isis.core.metamodel.specloader.collectiontyperegistry.CollectionTypeRegistry;
import org.apache.isis.core.metamodel.specloader.collectiontyperegistry.CollectionTypeRegistryAware;

public class CollectionFacetFactory extends FacetFactoryAbstract implements CollectionTypeRegistryAware, AdapterManagerAware {

    private CollectionTypeRegistry collectionTypeRegistry;
    private AdapterManager adapterManager;

    public CollectionFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessClassContext processClassContaxt) {

        if (collectionTypeRegistry.isCollectionType(processClassContaxt.getCls())) {
            processCollectionType(processClassContaxt);
        } else if (collectionTypeRegistry.isArrayType(processClassContaxt.getCls())) {
            processAsArrayType(processClassContaxt);
        }

    }

    private void processCollectionType(final ProcessClassContext processClassContaxt) {
        final FacetHolder facetHolder = processClassContaxt.getFacetHolder();
        final TypeOfFacet typeOfFacet = facetHolder.getFacet(TypeOfFacet.class);
        if (typeOfFacet == null) {
            final Class<?> collectionElementType = collectionElementType(processClassContaxt.getCls());
            facetHolder.addFacet(collectionElementType != Object.class ? new TypeOfFacetInferredFromGenerics(collectionElementType, facetHolder, getSpecificationLoader()) : new TypeOfFacetDefaultToObject(facetHolder, getSpecificationLoader()));
        } else {
            // nothing
        }
        facetHolder.addFacet(new JavaCollectionFacet(facetHolder, getAdapterManager()));
        return;
    }

    private void processAsArrayType(final ProcessClassContext processClassContaxt) {
        final FacetHolder facetHolder = processClassContaxt.getFacetHolder();
        facetHolder.addFacet(new JavaArrayFacet(facetHolder, getAdapterManager()));
        facetHolder.addFacet(new TypeOfFacetInferredFromArray(processClassContaxt.getCls().getComponentType(), facetHolder, getSpecificationLoader()));
    }

    private Class<?> collectionElementType(final Class<?> cls) {
        return Object.class;
    }

    // //////////////////////////////////////////////////////////////
    // Dependencies (injected)
    // //////////////////////////////////////////////////////////////

    /**
     * Injected since {@link CollectionTypeRegistryAware}.
     */
    @Override
    public void setCollectionTypeRegistry(final CollectionTypeRegistry collectionTypeRegistry) {
        this.collectionTypeRegistry = collectionTypeRegistry;
    }

    public AdapterManager getAdapterManager() {
        return adapterManager;
    }

    @Override
    public void setAdapterManager(final AdapterManager adapterManager) {
        this.adapterManager = adapterManager;
    }

}
