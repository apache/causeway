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

import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.ObjectSupport.IconSize;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.HasImperativeAspect;
import org.apache.causeway.core.metamodel.facets.ImperativeAspect;
import org.apache.causeway.core.metamodel.facets.object.icon.IconFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.jspecify.annotations.Nullable;

import lombok.Getter;

public final class IconFacetViaIconMethod
extends FacetAbstract
implements IconFacet, HasImperativeAspect {

    public static Optional<IconFacet> create(
            final @Nullable ResolvedMethod methodIfAny,
            final FacetHolder holder) {
        return Optional.ofNullable(methodIfAny)
            .map(method->
                new IconFacetViaIconMethod(
                        ImperativeAspect.singleRegularMethod(method, Intent.UI_HINT),
                        holder));
    }

    @Getter(onMethod_ = {@Override})
    private final ImperativeAspect imperativeAspect;

    private IconFacetViaIconMethod(final ImperativeAspect imperativeAspect, final FacetHolder facetHolder) {
    	super(IconFacet.class, facetHolder);
    	this.imperativeAspect = imperativeAspect;
    }

    @Override
    public Optional<ObjectSupport.IconResource> icon(final ManagedObject domainObject, final IconSize iconSize) {
        return Optional.ofNullable(imperativeAspect.eval(domainObject, (ObjectSupport.IconResource)null, iconSize));
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
    	IconFacet.super.visitAttributes(visitor);
        imperativeAspect.visitAttributes(visitor);
    }

}
