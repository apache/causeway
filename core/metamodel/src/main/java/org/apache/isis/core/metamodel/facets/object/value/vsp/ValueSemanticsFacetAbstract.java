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
package org.apache.isis.core.metamodel.facets.object.value.vsp;

import org.apache.isis.applib.value.semantics.DefaultsProvider;
import org.apache.isis.applib.value.semantics.EncoderDecoder;
import org.apache.isis.applib.value.semantics.Parser;
import org.apache.isis.applib.value.semantics.ValueSemanticsProvider;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;

import lombok.Getter;
import lombok.NonNull;

public abstract class ValueSemanticsFacetAbstract<T>
extends FacetAbstract
implements ValueSemanticsProvider<T> {

    @Getter
    final @NonNull ValueSemanticsProvider<T> valueSemantics;

    protected ValueSemanticsFacetAbstract(
            final Class<? extends Facet> facetType,
            final ValueSemanticsProvider<T> valueSemantics,
            final FacetHolder facetHolder) {
        super(facetType, facetHolder);
        this.valueSemantics = valueSemantics;
    }

    protected ValueSemanticsFacetAbstract(
            final Class<? extends Facet> facetType,
            final ValueSemanticsProvider<T> valueSemantics,
            final FacetHolder facetHolder,
            final Precedence precedence) {
        super(facetType, facetHolder, precedence);
        this.valueSemantics = valueSemantics;
    }

    @Override
    public final Parser<T> getParser() {
        return valueSemantics.getParser();
    }

    @Override
    public final EncoderDecoder<T> getEncoderDecoder() {
        return valueSemantics.getEncoderDecoder();
    }

    @Override
    public final DefaultsProvider<T> getDefaultsProvider() {
        return valueSemantics.getDefaultsProvider();
    }

}
