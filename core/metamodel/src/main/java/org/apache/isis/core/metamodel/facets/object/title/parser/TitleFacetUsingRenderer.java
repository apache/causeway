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
package org.apache.isis.core.metamodel.facets.object.title.parser;

import java.util.function.BiConsumer;

import org.apache.isis.applib.adapters.Renderer;
import org.apache.isis.applib.adapters.ValueSemanticsProvider;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetHolderAbstract;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.NonNull;
import lombok.val;

public final class TitleFacetUsingRenderer
extends FacetAbstract
implements TitleFacet {

    private final @NonNull Renderer<?> renderer;

    public static TitleFacetUsingRenderer create(final Renderer<?> renderer, final FacetHolder holder) {
        return new TitleFacetUsingRenderer(renderer, holder);
    }

    private TitleFacetUsingRenderer(final Renderer<?> renderer, final FacetHolder holder) {
        super(TitleFacet.class, holder, Precedence.LOW);
        this.renderer = renderer;
    }

    @Override
    public boolean semanticEquals(final @NonNull Facet other) {
        return other instanceof TitleFacetUsingRenderer
                ? this.renderer.getClass() == ((TitleFacetUsingRenderer)other).renderer.getClass()
                : false;
    }

    @Override
    public String title(final ManagedObject adapter) {
        if (adapter == null) {
            return null;
        }
        final Object object = adapter.getPojo();
        if (object == null) {
            return null;
        }
        return renderer.presentationValue(valueSemanticsContext(), _Casts.uncheckedCast(object));
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("renderer", renderer.toString());
    }

    private ValueSemanticsProvider.Context valueSemanticsContext() {
        val iaProvider = super.getInteractionProvider();
        if(iaProvider==null) {
            return null; // JUnit context
        }
        return ValueSemanticsProvider.Context.of(
                ((FacetHolderAbstract)getFacetHolder()).getFeatureIdentifier(),
                iaProvider.currentInteractionContext().orElse(null));
    }

}
