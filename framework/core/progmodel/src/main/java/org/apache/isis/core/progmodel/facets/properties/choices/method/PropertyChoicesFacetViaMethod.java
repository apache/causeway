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

package org.apache.isis.core.progmodel.facets.properties.choices.method;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.apache.isis.core.commons.lang.ArrayUtil;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.map.AdapterMap;
import org.apache.isis.core.metamodel.adapter.util.AdapterInvokeUtils;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLookup;
import org.apache.isis.core.progmodel.facets.CollectionUtils;
import org.apache.isis.core.progmodel.facets.properties.choices.PropertyChoicesFacetAbstract;

public class PropertyChoicesFacetViaMethod extends PropertyChoicesFacetAbstract implements ImperativeFacet {

    private final Method method;
    private final Class<?> choicesClass;

    private final AdapterMap adapterMap;

    public PropertyChoicesFacetViaMethod(final Method method, final Class<?> choicesClass, final FacetHolder holder, final SpecificationLookup specificationLookup, final AdapterMap adapterManager) {
        super(holder, specificationLookup);
        this.method = method;
        this.choicesClass = choicesClass;
        this.adapterMap = adapterManager;
    }

    /**
     * Returns a singleton list of the {@link Method} provided in the
     * constructor.
     */
    @Override
    public List<Method> getMethods() {
        return Collections.singletonList(method);
    }

    @Override
    public boolean impliesResolve() {
        return true;
    }

    @Override
    public boolean impliesObjectChanged() {
        return false;
    }

    @Override
    public Object[] getChoices(final ObjectAdapter owningAdapter, final SpecificationLookup specificationLookup) {
        final Object options = AdapterInvokeUtils.invoke(method, owningAdapter);
        if (options == null) {
            return null;
        }
        if (options.getClass().isArray()) {
            return ArrayUtil.getObjectAsObjectArray(options);
        }
        final ObjectSpecification specification = specificationLookup.loadSpecification(choicesClass);
        return CollectionUtils.getCollectionAsObjectArray(options, specification, getAdapterMap());
    }

    @Override
    protected String toStringValues() {
        return "method=" + method + ",class=" + choicesClass;
    }

    // ////////////////////////////////////////////
    // Dependencies
    // ////////////////////////////////////////////

    protected AdapterMap getAdapterMap() {
        return adapterMap;
    }

}
