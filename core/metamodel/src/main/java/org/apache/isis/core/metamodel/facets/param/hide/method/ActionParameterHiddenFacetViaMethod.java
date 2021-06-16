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

package org.apache.isis.core.metamodel.facets.param.hide.method;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.facets.param.hide.ActionParameterHiddenFacetAbstract;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

public class ActionParameterHiddenFacetViaMethod
extends ActionParameterHiddenFacetAbstract
implements ImperativeFacet {

    @Getter(onMethod_ = {@Override}) private final @NonNull Can<Method> methods;
    private final @NonNull Optional<Constructor<?>> ppmFactory;

    public ActionParameterHiddenFacetViaMethod(
            final Method method,
            final Optional<Constructor<?>> ppmFactory,
            final FacetHolder holder) {

        super(holder);
        this.methods = Can.ofSingleton(method);
        this.ppmFactory = ppmFactory;
    }

    @Override
    public Intent getIntent(final Method method) {
        return Intent.CHECK_IF_VALID;
    }

    @Override
    public boolean isHidden(
            final ManagedObject owningAdapter,
            final Can<ManagedObject> argumentAdapters) {

        val method = methods.getFirstOrFail();
        final Object returnValue = ppmFactory.isPresent()
                ? ManagedObjects.InvokeUtil.invokeWithPPM(ppmFactory.get(), method, owningAdapter, argumentAdapters)
                : ManagedObjects.InvokeUtil.invokeAutofit(method, owningAdapter, argumentAdapters);

        if(returnValue instanceof Boolean) {
            return (Boolean) returnValue;
        }
        // following precedent for validate, we let this through.
        return false;
    }

    @Override
    protected String toStringValues() {
        val method = methods.getFirstOrFail();
        return "method=" + method;
    }

    @Override
    public boolean semanticEquals(final @NonNull Facet otherFacet) {

        if(! (otherFacet instanceof ActionParameterHiddenFacetViaMethod)) {
            return false;
        }

        val other = (ActionParameterHiddenFacetViaMethod)otherFacet;
        return this.ppmFactory.equals(other.ppmFactory)
                && this.getMethods().equals(other.getMethods());
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        ImperativeFacet.visitAttributes(this, visitor);
    }


}
