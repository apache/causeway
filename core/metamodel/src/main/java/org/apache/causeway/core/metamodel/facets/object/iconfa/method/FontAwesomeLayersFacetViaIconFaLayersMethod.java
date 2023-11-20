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
package org.apache.causeway.core.metamodel.facets.object.iconfa.method;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.fa.FontAwesomeLayers;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.HasImperativeAspect;
import org.apache.causeway.core.metamodel.facets.ImperativeAspect;
import org.apache.causeway.core.metamodel.facets.members.cssclassfa.FontAwesomeLayersProvider;
import org.apache.causeway.core.metamodel.facets.object.iconfa.FontAwesomeLayersFacet;
import org.apache.causeway.core.metamodel.facets.object.iconfa.FontAwesomeLayersFacetAbstract;
import org.apache.causeway.core.metamodel.object.ManagedObject;

import lombok.Getter;
import lombok.NonNull;

public class FontAwesomeLayersFacetViaIconFaLayersMethod
extends FontAwesomeLayersFacetAbstract
implements HasImperativeAspect {

    @Getter(onMethod_ = {@Override}) private final @NonNull ImperativeAspect imperativeAspect;

    public static Optional<FontAwesomeLayersFacet> create(
            final @Nullable ResolvedMethod methodIfAny,
            final FacetHolder holder) {

        return Optional.ofNullable(methodIfAny)
        .map(method->
            new FontAwesomeLayersFacetViaIconFaLayersMethod(
                    ImperativeAspect.singleRegularMethod(method, Intent.UI_HINT),
                    holder));
    }

    private FontAwesomeLayersFacetViaIconFaLayersMethod(
            final ImperativeAspect imperativeAspect,
            final FacetHolder holder) {
        super(holder);
        this.imperativeAspect = imperativeAspect;
    }

    @Override
    public FontAwesomeLayers layers(final ManagedObject domainObject) {
        return imperativeAspect.eval(domainObject, (FontAwesomeLayers)null);
    }

    @Override
    public FontAwesomeLayersProvider getCssClassFaFactory(final Supplier<ManagedObject> domainObjectProvider) {
        //TODO[CAUSEWAY-3646] implement
        return () -> layers(domainObjectProvider.get());
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        imperativeAspect.visitAttributes(visitor);
    }

}
