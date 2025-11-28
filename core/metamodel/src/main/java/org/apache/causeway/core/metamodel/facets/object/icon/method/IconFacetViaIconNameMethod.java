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
package org.apache.causeway.core.metamodel.facets.object.icon.method;

import java.util.Optional;
import java.util.function.BiConsumer;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.ObjectSupport.IconSize;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.HasImperativeAspect;
import org.apache.causeway.core.metamodel.facets.ImperativeAspect;
import org.apache.causeway.core.metamodel.facets.object.icon.IconFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;

public record IconFacetViaIconNameMethod(
    ImperativeAspect imperativeAspect,
    FacetHolder facetHolder)
implements IconFacet, HasImperativeAspect {

    public static Optional<IconFacet> create(
            final @Nullable ResolvedMethod methodIfAny,
            final FacetHolder holder) {
        return Optional.ofNullable(methodIfAny)
            .map(method->
                new IconFacetViaIconNameMethod(
                        ImperativeAspect.singleRegularMethod(method, Intent.UI_HINT),
                        holder));
    }

    @Override public Class<? extends Facet> facetType() { return IconFacet.class; }
    @Override public Precedence precedence() { return Precedence.LOW; }

    @Override
    public Optional<ObjectSupport.IconResource> icon(ManagedObject domainObject, IconSize iconSize) {
        return Optional.of(new ObjectSupport.ClassPathIconResource(
            _Strings.nullToEmpty(imperativeAspect.eval(domainObject, (String)null))));
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
    	IconFacet.super.visitAttributes(visitor);
        imperativeAspect.visitAttributes(visitor);
    }

}
