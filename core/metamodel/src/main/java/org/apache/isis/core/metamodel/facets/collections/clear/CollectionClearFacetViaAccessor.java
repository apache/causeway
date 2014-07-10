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

package org.apache.isis.core.metamodel.facets.collections.clear;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectDirtier;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionClearFacetAbstract;

public class CollectionClearFacetViaAccessor extends CollectionClearFacetAbstract implements ImperativeFacet {

    private final Method method;
    private final AdapterManager adapterManager;
    private final ObjectDirtier objectDirtier;

    public CollectionClearFacetViaAccessor(final Method method, final FacetHolder holder, final AdapterManager adapterManager, final ObjectDirtier objectDirtier) {
        super(holder);
        this.method = method;
        this.adapterManager = adapterManager;
        this.objectDirtier = objectDirtier;
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
    public Intent getIntent(final Method method) {
        return Intent.ACCESSOR;
    }

    @Override
    public boolean impliesResolve() {
        return true;
    }

    /**
     * Bytecode cannot automatically call
     * {@link DomainObjectContainer#objectChanged(Object)} because cannot
     * distinguish whether interacting with accessor to read it or to modify its
     * contents.
     */
    @Override
    public boolean impliesObjectChanged() {
        return false;
    }

    @Override
    public void clear(final ObjectAdapter owningAdapter) {
        final Collection<?> collection = (Collection<?>) ObjectAdapter.InvokeUtils.invoke(method, owningAdapter);
        collection.clear();
        final ObjectAdapter adapter = getAdapterManager().getAdapterFor(owningAdapter);
        getObjectDirtier().objectChanged(adapter);
    }

    @Override
    protected String toStringValues() {
        return "method=" + method;
    }

    // /////////////////////////////////////////////////////////
    // Dependencies (from constructor)
    // /////////////////////////////////////////////////////////

    protected AdapterManager getAdapterManager() {
        return adapterManager;
    }

    protected ObjectDirtier getObjectDirtier() {
        return objectDirtier;
    }

}
