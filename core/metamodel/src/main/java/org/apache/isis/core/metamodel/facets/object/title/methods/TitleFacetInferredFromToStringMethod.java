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
package org.apache.isis.core.metamodel.facets.object.title.methods;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.springframework.lang.Nullable;

import org.apache.isis.core.metamodel.commons.ClassExtensions;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.HasImperativeAspect;
import org.apache.isis.core.metamodel.facets.ImperativeAspect;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacetAbstract;
import org.apache.isis.core.metamodel.facets.object.title.TitleRenderRequest;

import lombok.Getter;
import lombok.NonNull;

public class TitleFacetInferredFromToStringMethod
extends TitleFacetAbstract
implements HasImperativeAspect {

    @Getter(onMethod_ = {@Override}) private final @NonNull ImperativeAspect imperativeAspect;

    public static Optional<TitleFacet> create(
            final @Nullable Method methodIfAny,
            final FacetHolder holder) {

        return Optional.ofNullable(methodIfAny)
        .filter(method->!ClassExtensions.isJavaClass(method.getDeclaringClass()))
        .map(method->
            new TitleFacetInferredFromToStringMethod(
                    ImperativeAspect.singleMethod(method, Intent.UI_HINT),
                    holder));
    }

    private TitleFacetInferredFromToStringMethod(
            final ImperativeAspect imperativeAspect,
            final FacetHolder holder) {
        super(holder, Precedence.INFERRED);
        this.imperativeAspect = imperativeAspect;
    }

    @Override
    public String title(final TitleRenderRequest titleRenderRequest) {
        return imperativeAspect.eval(titleRenderRequest.getObject(), "(not present)");
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        imperativeAspect.visitAttributes(visitor);
    }

}
