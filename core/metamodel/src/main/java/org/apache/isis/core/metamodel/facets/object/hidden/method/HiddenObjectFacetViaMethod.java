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

package org.apache.isis.core.metamodel.facets.object.hidden.method;

import java.lang.reflect.Method;
import java.util.function.BiConsumer;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.facets.object.hidden.HiddenObjectFacetAbstract;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

public class HiddenObjectFacetViaMethod
extends HiddenObjectFacetAbstract
implements ImperativeFacet {

    @Getter(onMethod_ = {@Override}) private final @NonNull Can<Method> methods;

    public HiddenObjectFacetViaMethod(final Method method, final FacetHolder holder) {
        super(holder);
        this.methods = ImperativeFacet.singleMethod(method);;
    }

    @Override
    public Intent getIntent(final Method method) {
        return Intent.CHECK_IF_HIDDEN;
    }

    @Override
    public String hides(final VisibilityContext ic) {
        final ManagedObject toValidate = ic.getTarget();
        return toValidate != null ? hiddenReason(toValidate) : null;
    }

    @Override
    public String hiddenReason(final ManagedObject target) {
        if (target == null) {
            return null;
        }
        val method = methods.getFirstOrFail();
        final Boolean isHidden = (Boolean) ManagedObjects.InvokeUtil.invoke(method, target);
        return isHidden ? "Hidden" : null;
    }

    @Override
    public HiddenObjectFacetViaMethod clone(final FacetHolder holder) {
        val method = methods.getFirstOrFail();
        return new HiddenObjectFacetViaMethod(method, holder);
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        val method = methods.getFirstOrFail();
        super.visitAttributes(visitor);
        ImperativeFacet.visitAttributes(this, visitor);
        visitor.accept("method", method);
    }
}
