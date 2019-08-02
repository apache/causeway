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

package org.apache.isis.metamodel.facets.members.hidden;

import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.wrapper.events.VisibilityEvent;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.WhereValueFacetAbstract;
import org.apache.isis.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.metamodel.interactions.VisibilityContext;
import org.apache.isis.metamodel.spec.ManagedObject;

import static org.apache.isis.commons.internal.base._Casts.uncheckedCast;

/**
 * This implements {@link org.apache.isis.metamodel.facetapi.MultiTypedFacet} so that each concrete implementation
 * is added to the eventual {@link org.apache.isis.metamodel.facetapi.FacetHolder} twice: once under
 * <tt>HiddeFacet.class</tt> and once under its own concrete type class (eg <tt>HiddenFacetForActionAnnotation</tt>).
 * This satisfies a couple of (independent) requirements:
 * <ul>
 *     <li>that we don't have the concept of a single (blessed?) HiddenFacet; rather there are simply facets some of
 *     which implement {@link org.apache.isis.metamodel.interactions.HidingInteractionAdvisor}</li>
 *     <li>that there is nevertheless always at least one facet that is registered under <tt>HiddenFacet.class</tt>;
 *     this is used by the {@link org.apache.isis.metamodel.layoutmetadata.json.LayoutMetadataReaderFromJson} exporter</li>
 * </ul>
 * <p>
 *     Note that the {@link org.apache.isis.metamodel.facetapi.FacetUtil#getFacets(java.util.Map, Predicate)}
 *     (which among other things is used to return all facets matching a particular facet type) ensures that the list
 *     of facets returned contains no duplicates.
 * </p>
 */
public abstract class HiddenFacetAbstract extends WhereValueFacetAbstract implements HiddenFacet {

    public HiddenFacetAbstract(
            final Class<? extends Facet> facetType,
            Where where,
            final FacetHolder holder) {
        super(facetType, holder, where);
    }

    /**
     * For testing only.
     */
    public HiddenFacetAbstract(Where where, final FacetHolder holder) {
        super(HiddenFacetAbstract.class, holder, where);
    }

    @Override
    public String hides(final VisibilityContext<? extends VisibilityEvent> ic) {
        return hiddenReason(ic.getTarget(), ic.getWhere());
    }

    /**
     * The reason why the (feature of the) target object is currently hidden, or
     * <tt>null</tt> if visible.
     */
    protected abstract String hiddenReason(ManagedObject target, Where whereContext);

    @Override
    public Stream<Class<? extends Facet>> facetTypes() {
        return Stream.of(facetType(), HiddenFacet.class);
    }

    @Override
    public <T extends Facet> T getFacet(final Class<T> facet) {
        return uncheckedCast(this);
    }

    @Override
    public boolean containsFacetTypeOf(final Class<? extends Facet> requiredFacetType) {
        return facetTypes()
                .anyMatch(facetType->facetType == requiredFacetType);
    }


}
