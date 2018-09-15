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

package org.apache.isis.core.metamodel.facets.actions.defaults.method;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacet;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForDomainEventAbstract;
import org.apache.isis.core.metamodel.facets.actions.defaults.ActionDefaultsFacetAbstract;

public class ActionDefaultsFacetViaMethod extends ActionDefaultsFacetAbstract implements ImperativeFacet {

    private final Method defaultMethod;

    @SuppressWarnings("unused")
    private final Method actionMethod;

    public ActionDefaultsFacetViaMethod(final Method defaultMethod, final FacetHolder holder) {
        super(holder, Derivation.NOT_DERIVED);
        this.defaultMethod = defaultMethod;
        this.actionMethod = determineActionMethod(holder);
    }

    private static Method determineActionMethod(final FacetHolder holder) {
        Method method2;
        final Facet actionInvocationFacet = holder.getFacet(ActionInvocationFacet.class);
        if (actionInvocationFacet instanceof ActionInvocationFacetForDomainEventAbstract) {
            final ActionInvocationFacetForDomainEventAbstract facetViaMethod = (ActionInvocationFacetForDomainEventAbstract) actionInvocationFacet;
            method2 = facetViaMethod.getMethods().get(0);
        } else {
            method2 = null;
        }
        return method2;
    }

    /**
     * Returns a singleton list of the {@link Method} provided in the
     * constructor.
     */
    @Override
    public List<Method> getMethods() {
        return Collections.singletonList(defaultMethod);
    }

    @Override
    public Intent getIntent(final Method method) {
        return Intent.DEFAULTS;
    }

    @Override
    public Object[] getDefaults(final ObjectAdapter owningAdapter) {
        return (Object[]) ObjectAdapter.InvokeUtils.invoke(defaultMethod, owningAdapter);
    }

    @Override
    protected String toStringValues() {
        return "method=" + defaultMethod;
    }

}
