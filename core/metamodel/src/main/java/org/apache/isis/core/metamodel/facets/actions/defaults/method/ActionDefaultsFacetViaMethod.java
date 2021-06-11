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
import java.util.function.BiConsumer;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacet;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForDomainEventAbstract;
import org.apache.isis.core.metamodel.facets.actions.defaults.ActionDefaultsFacetAbstract;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

public class ActionDefaultsFacetViaMethod
extends ActionDefaultsFacetAbstract
implements ImperativeFacet {

    @Getter(onMethod_ = {@Override}) private final @NonNull Can<Method> methods;

    @SuppressWarnings("unused")
    private final Method actionMethod;

    public ActionDefaultsFacetViaMethod(final Method method, final FacetHolder holder) {
        super(holder);
        this.methods = Can.ofSingleton(method);
        this.actionMethod = determineActionMethod(holder);
    }

    private static Method determineActionMethod(final FacetHolder holder) {
        Method method2;
        final Facet actionInvocationFacet = holder.getFacet(ActionInvocationFacet.class);
        if (actionInvocationFacet instanceof ActionInvocationFacetForDomainEventAbstract) {
            final ActionInvocationFacetForDomainEventAbstract facetViaMethod = (ActionInvocationFacetForDomainEventAbstract) actionInvocationFacet;
            method2 = facetViaMethod.getMethods().getFirstOrFail();
        } else {
            method2 = null;
        }
        return method2;
    }

    @Override
    public Intent getIntent(final Method method) {
        return Intent.DEFAULTS;
    }

    @Override
    public Object[] getDefaults(final ManagedObject owningAdapter) {
        val method = methods.getFirstOrFail();
        return (Object[]) ManagedObjects.InvokeUtil.invoke(method, owningAdapter);
    }

    @Override
    protected String toStringValues() {
        val method = methods.getFirstOrFail();
        return "method=" + method;
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        ImperativeFacet.visitAttributes(this, visitor);
    }

}
