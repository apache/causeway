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


package org.apache.isis.core.progmodel.facets.collections;

import org.apache.isis.core.metamodel.adapter.map.AdapterMap;
import org.apache.isis.core.metamodel.adapter.map.AdapterMapAware;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MethodRemover;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacetDefaultToObject;
import org.apache.isis.core.metamodel.spec.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.specloader.collectiontyperegistry.CollectionTypeRegistry;
import org.apache.isis.core.metamodel.specloader.collectiontyperegistry.CollectionTypeRegistryAware;
import org.apache.isis.core.progmodel.facets.actcoll.typeof.TypeOfFacetInferredFromArray;
import org.apache.isis.core.progmodel.facets.actcoll.typeof.TypeOfFacetInferredFromGenerics;


public class CollectionFacetFactory extends FacetFactoryAbstract implements CollectionTypeRegistryAware, AdapterMapAware {

    private CollectionTypeRegistry collectionTypeRegistry;
	private AdapterMap adapterMap;

    public CollectionFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }


    @Override
    public boolean process(final Class<?> cls, final MethodRemover methodRemover, final FacetHolder holder) {
        if (collectionTypeRegistry.isCollectionType(cls)) {
            final TypeOfFacet typeOfFacet = holder.getFacet(TypeOfFacet.class);
            if (typeOfFacet == null) {
                Class<?> collectionElementType = collectionElementType(cls);
                holder.addFacet(collectionElementType != Object.class ? new TypeOfFacetInferredFromGenerics(
                        collectionElementType, holder, getSpecificationLookup()) : new TypeOfFacetDefaultToObject(holder, getSpecificationLookup()));
            } else {
                // nothing
            }
            holder.addFacet(new JavaCollectionFacet(holder, getAdapterMap()));
            return true;
        }
        if (collectionTypeRegistry.isArrayType(cls)) {
            holder.addFacet(new JavaArrayFacet(holder, getAdapterMap()));
            holder.addFacet(new TypeOfFacetInferredFromArray(cls.getComponentType(), holder, getSpecificationLookup()));
            return true;
        }

        return false;
    }

    private Class<?> collectionElementType(final Class<?> cls) {
        return Object.class;
    }


    ////////////////////////////////////////////////////////////////
    // Dependencies (injected)
    ////////////////////////////////////////////////////////////////

    /**
     * Injected since {@link CollectionTypeRegistryAware}.
     */
    @Override
    public void setCollectionTypeRegistry(final CollectionTypeRegistry collectionTypeRegistry) {
        this.collectionTypeRegistry = collectionTypeRegistry;
    }

    public AdapterMap getAdapterMap() {
        return adapterMap;
    }
    
    @Override
    public void setAdapterMap(AdapterMap adapterManager) {
        this.adapterMap = adapterManager;
    }

}
