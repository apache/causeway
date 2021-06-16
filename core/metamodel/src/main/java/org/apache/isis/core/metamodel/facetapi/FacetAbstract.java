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

package org.apache.isis.core.metamodel.facetapi;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.core.metamodel.context.HasMetaModelContext;
import org.apache.isis.core.metamodel.context.MetaModelContext;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

public abstract class FacetAbstract
implements Facet, HasMetaModelContext {

    private final @NonNull Class<? extends Facet> facetType;
    private Set<Facet> contributedFacets; // lazy init

    @Getter(onMethod_ = {@Override}) private final @NonNull Facet.Precedence precedence;

    @Getter(onMethod_ = {@Override})
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
    public final Class<? extends Facet> facetType() {
        return facetType;
    }

    @Override
    public MetaModelContext getMetaModelContext() {
        return facetHolder.getMetaModelContext();
    }

    protected String toStringValues() {
        return "";
    }

    @Override
    public String toString() {
        String details = interactionAdvisors(";");
        if (!details.isEmpty()) {
            details = "interactionAdvisors=" + details + ",";
        }

        final String className = getClass().getName();
        final String stringValues = toStringValues();
        if (getClass() != facetType()) {
            final String facetType = facetType().getName();
            details += "type=" + facetType.substring(facetType.lastIndexOf('.') + 1);
        }
        if (!"".equals(stringValues)) {
            details += ",";
        }
        return className.substring(className.lastIndexOf('.') + 1) + "[" + details + stringValues + "]";
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        visitor.accept("facet", this.getClass().getName());
        visitor.accept("precedence", getPrecedence().name());

        val interactionAdvisors = interactionAdvisors(", ");

        // suppress 'advisors' if none
        if(!interactionAdvisors.isEmpty()) {
            visitor.accept("interactionAdvisors", interactionAdvisors);
        }
    }

    /**
     * Marker interface used within {@link #toString()}.
     */
    public static interface HidingOrShowing {
    }

    /**
     * Marker interface used within {@link #toString()}.
     */
    public static interface DisablingOrEnabling {
    }

    /**
     * Marker interface used within {@link #toString()}.
     */
    public static interface Validating {
    }

    private String interactionAdvisors(final String delimiter) {
        return Stream.of(Validating.class, HidingOrShowing.class, DisablingOrEnabling.class)
        .filter(marker->marker.isAssignableFrom(getClass()))
        .map(Class::getSimpleName)
        .collect(Collectors.joining(delimiter));
    }

    // -- CONTRIBUTED FACET SUPPORT

    @Override
    public void addContributedFacet(final Facet contributedFacet) {
        if(contributedFacets==null) {
            contributedFacets = _Sets.newHashSet();
        }
        contributedFacets.add(contributedFacet);
    }

    @Override
    public void forEachContributedFacet(final Consumer<Facet> onContributedFacet) {
        if(contributedFacets!=null) {
            contributedFacets.forEach(onContributedFacet);
        }
    }


}
