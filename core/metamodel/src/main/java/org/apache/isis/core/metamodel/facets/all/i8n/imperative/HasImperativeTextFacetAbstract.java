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
package org.apache.isis.core.metamodel.facets.all.i8n.imperative;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.BiConsumer;

import org.apache.isis.applib.services.i18n.TranslationContext;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.functional.Result;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;

import lombok.NonNull;
import lombok.val;

public class HasImperativeTextFacetAbstract
extends FacetAbstract
implements
    ImperativeFacet,
    HasImperativeText {

    protected final TranslationContext translationContext;
    protected final @NonNull Method method;

    protected HasImperativeTextFacetAbstract(
            final Class<? extends Facet> facetType,
            final TranslationContext translationContext,
            final Method method,
            final FacetHolder holder) {
        // imperative takes precedence over any other (except for events)
        super(facetType, holder, Precedence.IMPERATIVE);
        this.method = method;
        this.translationContext = translationContext;
    }

    @Override
    public final Result<String> text(final ManagedObject object) {
        return ManagedObjects.imperativeText(object, method, translationContext);
    }

    @Override
    public final Can<Method> getMethods() {
        return Can.ofSingleton(method);
    }

    @Override
    public final Intent getIntent(final Method method) {
        return Intent.UI_HINT;
    }

    @Override
    public final void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("context", translationContext);
        ImperativeFacet.visitAttributes(this, visitor);
    }

    @Override
    public final boolean semanticEquals(final @NonNull Facet other) {

        // equality by facet-type, java-method and translation-context

        if(!this.facetType().equals(other.facetType())) {
            return false;
        }

        val otherFacet = (HasImperativeTextFacetAbstract)other;

        return Objects.equals(this.method, otherFacet.method)
                && Objects.equals(this.translationContext, otherFacet.translationContext);

    }

}
