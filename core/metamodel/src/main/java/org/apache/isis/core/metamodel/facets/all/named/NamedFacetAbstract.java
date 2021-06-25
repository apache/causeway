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

package org.apache.isis.core.metamodel.facets.all.named;

import java.util.function.BiConsumer;

import org.apache.isis.commons.internal.base._Either;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.all.i8n.imperative.HasImperativeText;
import org.apache.isis.core.metamodel.facets.all.i8n.staatic.HasStaticText;
import org.apache.isis.core.metamodel.facets.all.i8n.staatic.I8nStaticFacetAbstract;
import org.apache.isis.core.metamodel.facets.all.i8n.staatic.NounForms;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

public abstract class NamedFacetAbstract
extends I8nStaticFacetAbstract
implements NamedFacet {

    private static final Class<? extends Facet> type() {
        return NamedFacet.class;
    }

    @Getter(onMethod_ = {@Override})
    private final _Either<HasStaticText, HasImperativeText> specialization = _Either.left(this);

    private final boolean escaped;

    protected NamedFacetAbstract(
            final NounForms namedNounForms,
            final boolean escaped,
            final FacetHolder holder) {
        this(namedNounForms, escaped, holder, Precedence.DEFAULT);
    }

    protected NamedFacetAbstract(
            final NounForms namedNounForms,
            final boolean escaped,
            final FacetHolder holder,
            final Facet.Precedence precedence) {
        super(type(), namedNounForms, holder, precedence);
        this.escaped = escaped;
    }

    @Override
    public boolean escaped() {
        return escaped;
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("escaped", escaped);
    }

    @Override
    public boolean semanticEquals(final @NonNull Facet other) {
        if(! (other instanceof NamedFacetAbstract)) {
            return false;
        }

        val otherNamedFacet = (NamedFacetAbstract)other;

        return this.escaped() == otherNamedFacet.escaped()
                && super.semanticEquals(otherNamedFacet);
    }

}
