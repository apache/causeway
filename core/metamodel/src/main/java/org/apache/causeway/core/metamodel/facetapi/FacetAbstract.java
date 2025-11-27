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
package org.apache.causeway.core.metamodel.facetapi;

import org.jspecify.annotations.NonNull;

import org.apache.causeway.core.metamodel.context.HasMetaModelContext;

import lombok.Getter;
import lombok.experimental.Accessors;

public abstract class FacetAbstract
implements Facet, HasMetaModelContext {

	@Getter(onMethod_ = {@Override}) @Accessors(fluent = true)
    private final @NonNull Class<? extends Facet> facetType;

    @Getter(onMethod_ = {@Override}) @Accessors(fluent = true)
    private final Facet.@NonNull Precedence precedence;

    @Getter(onMethod_ = {@Override}) @Accessors(fluent = true)
    private final @NonNull FacetHolder facetHolder;

    public FacetAbstract(
            final Class<? extends Facet> facetType,
            final FacetHolder facetHolder,
            final Facet.Precedence precedence) {

        this.facetType = facetType;
        this.facetHolder = facetHolder;
        this.precedence = precedence;
    }

    protected FacetAbstract(
            final Class<? extends Facet> facetType,
            final FacetHolder facetHolder) {
        this(facetType, facetHolder, Facet.Precedence.DEFAULT);
    }

    @Override
    public String toString() {
        return FacetUtil.toString(this);
    }

}
