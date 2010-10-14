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


package org.apache.isis.metamodel.facets.collections;

import org.apache.isis.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.metamodel.facets.FacetHolder;
import org.apache.isis.metamodel.facets.MethodRemover;
import org.apache.isis.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.metamodel.facets.actcoll.typeof.TypeOfFacetDefaultToObject;
import org.apache.isis.metamodel.facets.actcoll.typeof.TypeOfFacetInferredFromArray;
import org.apache.isis.metamodel.facets.actcoll.typeof.TypeOfFacetInferredFromGenerics;
import org.apache.isis.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.metamodel.runtimecontext.RuntimeContextAware;
import org.apache.isis.metamodel.spec.feature.ObjectFeatureType;
import org.apache.isis.metamodel.specloader.collectiontyperegistry.CollectionTypeRegistry;
import org.apache.isis.metamodel.specloader.collectiontyperegistry.CollectionTypeRegistryAware;


public class CollectionFacetFactory extends FacetFactoryAbstract implements CollectionTypeRegistryAware, RuntimeContextAware {

    private CollectionTypeRegistry collectionTypeRegistry;
	private RuntimeContext runtimeContext;

    public CollectionFacetFactory() {
        super(ObjectFeatureType.OBJECTS_ONLY);
    }


    @Override
    public boolean process(final Class<?> cls, final MethodRemover methodRemover, final FacetHolder holder) {
        if (collectionTypeRegistry.isCollectionType(cls)) {
            final TypeOfFacet typeOfFacet = holder.getFacet(TypeOfFacet.class);
            if (typeOfFacet == null) {
                Class<?> collectionElementType = collectionElementType(cls);
                holder.addFacet(collectionElementType != Object.class ? new TypeOfFacetInferredFromGenerics(
                        collectionElementType, holder, getSpecificationLoader()) : new TypeOfFacetDefaultToObject(holder, getSpecificationLoader()));
            } else {
                // nothing
            }
            holder.addFacet(new JavaCollectionFacet(holder, getRuntimeContext()));
            return true;
        }
        if (collectionTypeRegistry.isArrayType(cls)) {
            holder.addFacet(new JavaArrayFacet(holder, getRuntimeContext()));
            holder.addFacet(new TypeOfFacetInferredFromArray(cls.getComponentType(), holder, getSpecificationLoader()));
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
    public void setCollectionTypeRegistry(final CollectionTypeRegistry collectionTypeRegistry) {
        this.collectionTypeRegistry = collectionTypeRegistry;
    }


    /**
     * As per {@link #setRuntimeContext(RuntimeContext)}
     */
    public RuntimeContext getRuntimeContext() {
		return runtimeContext;
	}

    /**
     * Injected since {@link RuntimeContextAware}.
     */
	public void setRuntimeContext(final RuntimeContext runtimeContext) {
		this.runtimeContext = runtimeContext;
	}


}
