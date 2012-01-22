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

package org.apache.isis.core.progmodel.facets.collections.clear;

import java.lang.reflect.Method;

import org.apache.isis.core.commons.lang.NameUtils;
import org.apache.isis.core.metamodel.adapter.ObjectDirtier;
import org.apache.isis.core.metamodel.adapter.ObjectDirtierAware;
import org.apache.isis.core.metamodel.adapter.map.AdapterMap;
import org.apache.isis.core.metamodel.adapter.map.AdapterMapAware;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionClearFacet;
import org.apache.isis.core.metamodel.methodutils.MethodScope;
import org.apache.isis.core.progmodel.facets.MethodFinderUtils;
import org.apache.isis.core.progmodel.facets.MethodPrefixBasedFacetFactoryAbstract;
import org.apache.isis.core.progmodel.facets.MethodPrefixConstants;

public class CollectionClearFacetFactory extends MethodPrefixBasedFacetFactoryAbstract implements AdapterMapAware, ObjectDirtierAware {

    private static final String[] PREFIXES = { MethodPrefixConstants.CLEAR_PREFIX };

    private AdapterMap adapterMap;
    private ObjectDirtier objectDirtier;

    public CollectionClearFacetFactory() {
        super(FeatureType.COLLECTIONS_ONLY, PREFIXES);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        attachCollectionClearFacets(processMethodContext);

    }

    private void attachCollectionClearFacets(final ProcessMethodContext processMethodContext) {

        final Method getMethod = processMethodContext.getMethod();
        final String capitalizedName = NameUtils.javaBaseName(getMethod.getName());

        final Class<?> cls = processMethodContext.getCls();
        final Method method = MethodFinderUtils.findMethod(cls, MethodScope.OBJECT, MethodPrefixConstants.CLEAR_PREFIX + capitalizedName, void.class, null);
        processMethodContext.removeMethod(method);

        final FacetHolder collection = processMethodContext.getFacetHolder();
        FacetUtil.addFacet(createCollectionClearFacet(method, getMethod, collection));
    }

    private CollectionClearFacet createCollectionClearFacet(final Method clearMethodIfAny, final Method accessorMethod, final FacetHolder collection) {
        if (clearMethodIfAny != null) {
            return new CollectionClearFacetViaMethod(clearMethodIfAny, collection);
        } else {
            return new CollectionClearFacetViaAccessor(accessorMethod, collection, getAdapterMap(), getObjectDirtier());
        }
    }

    // ///////////////////////////////////////////////////////
    // Dependencies (injected)
    // ///////////////////////////////////////////////////////

    protected AdapterMap getAdapterMap() {
        return adapterMap;
    }

    @Override
    public void setAdapterMap(final AdapterMap adapterMap) {
        this.adapterMap = adapterMap;
    }

    protected ObjectDirtier getObjectDirtier() {
        return objectDirtier;
    }

    @Override
    public void setObjectDirtier(final ObjectDirtier objectDirtier) {
        this.objectDirtier = objectDirtier;
    }

}
