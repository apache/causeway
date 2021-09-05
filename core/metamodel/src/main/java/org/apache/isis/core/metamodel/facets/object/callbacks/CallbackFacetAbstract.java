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

package org.apache.isis.core.metamodel.facets.object.callbacks;

import java.lang.reflect.Method;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.reflection._Reflect;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;

import lombok.Getter;

/**
 * Adapter superclass for {@link Facet}s for {@link CallbackFacet}.
 */
public abstract class CallbackFacetAbstract
extends FacetAbstract
implements CallbackFacet {

    @Getter(onMethod_ = {@Override})
    private final Can<Method> methods;

    protected CallbackFacetAbstract(
            final Class<? extends Facet> facetType,
            final Can<Method> methods,
            final FacetHolder holder) {
        super(facetType, holder);
        this.methods = methods
                .map(method->_Reflect.lookupRegularMethodForSynthetic(method).orElse(null));
    }

    @Override
    public final Intent getIntent(final Method method) {
        return Intent.LIFECYCLE;
    }

    @Override
    protected final String toStringValues() {
        // free of side-effects, though might not reflect the final state if called too early
        return "methods=" + methods;
    }

    @Override
    public final void invoke(final ManagedObject adapter) {
        // as a side effect memoizes the list of methods and locks it so cannot add any more
        ManagedObjects.InvokeUtil.invokeAll(getMethods(), adapter);
    }


}
