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
import java.util.Optional;
import java.util.function.BiConsumer;

import org.springframework.lang.Nullable;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.HasImperativeAspect;
import org.apache.isis.core.metamodel.facets.ImperativeAspect;
import org.apache.isis.core.metamodel.facets.object.hidden.HiddenObjectFacet;
import org.apache.isis.core.metamodel.facets.object.hidden.HiddenObjectFacetAbstract;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.Getter;
import lombok.NonNull;

public class HiddenObjectFacetViaMethod
extends HiddenObjectFacetAbstract
implements HasImperativeAspect {

    @Getter(onMethod_ = {@Override}) private final @NonNull ImperativeAspect imperativeAspect;

    public static Optional<HiddenObjectFacet> create(
            final @Nullable Method methodIfAny,
            final FacetHolder holder) {

        return Optional.ofNullable(methodIfAny)
        .map(method->ImperativeAspect.singleMethod(method, Intent.CHECK_IF_HIDDEN))
        .map(imperativeAspect->new HiddenObjectFacetViaMethod(imperativeAspect, holder));
    }

    private HiddenObjectFacetViaMethod(final ImperativeAspect imperativeAspect, final FacetHolder holder) {
        super(holder);
        this.imperativeAspect = imperativeAspect;
    }

    @Override
    public String hides(final VisibilityContext ic) {
        final ManagedObject toValidate = ic.getTarget();
        return toValidate != null ? hiddenReason(toValidate) : null;
    }

    @Override
    public String hiddenReason(final ManagedObject target) {
        final boolean isHidden = imperativeAspect.eval(target, false);
        return isHidden ? "Hidden" : null;
    }

    @Override
    public HiddenObjectFacetViaMethod clone(final FacetHolder holder) {
        return new HiddenObjectFacetViaMethod(imperativeAspect, holder);
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        imperativeAspect.visitAttributes(visitor);
    }
}
