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
package org.apache.isis.core.metamodel.facets.object.value;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.adapters.ValueSemanticsProvider;
import org.apache.isis.applib.adapters.ValueSemanticsProvider.Context;
import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public abstract class ValueFacetAbstract<T>
extends FacetAbstract
implements ValueFacet<T> {

    private static final Class<? extends Facet> type() {
        return ValueFacet.class;
    }

    @Getter(onMethod_ = {@Override})
    private final Can<ValueSemanticsProvider<T>> valueSemantics;

    protected ValueFacetAbstract(
            final Can<ValueSemanticsProvider<T>> valueSemantics,
            final FacetHolder holder,
            final Facet.Precedence precedence) {

        super(type(), holder, precedence);
        this.valueSemantics = valueSemantics;
    }

    protected boolean hasSemanticsProvider() {
        return !this.valueSemantics.isEmpty();
    }

    @Override
    public final LogicalType getValueType() {
        return getFacetHolder().getFeatureIdentifier().getLogicalType();
    }

    @Override
    public Optional<Parser<T>> selectParserForParameter(final ObjectActionParameter param) {
        return streamParsers(s->true) // TODO filter by qualifier if any
                .findFirst();
    }

    @Override
    public Optional<Parser<T>> selectParserForProperty(final OneToOneAssociation prop) {
        return streamParsers(s->true) // TODO filter by qualifier if any
                .findFirst();
    }

    @Override
    public Parser<T> fallbackParser(final Identifier featureIdentifier) {
        return new ReadonlyMissingParserMessageParser<T>(String
                .format("Could not find a parser for type %s "
                        + "in the context of %s",
                        getValueType(),
                        featureIdentifier));
    }

    // -- HELPER

    private Stream<Parser<T>> streamParsers(
            final Predicate<ValueSemanticsProvider<T>> semanticsFilter) {
        return getValueSemantics()
                .stream()
                .filter(semanticsFilter)
                .map(ValueSemanticsProvider::getParser)
                .filter(_NullSafe::isPresent);
    }

    @RequiredArgsConstructor
    private final static class ReadonlyMissingParserMessageParser<T>
    implements Parser<T> {

        private final String message;

        @Override
        public String parseableTextRepresentation(final Context context, final T value) {
            return message;
        }

        @Override
        public T parseTextRepresentation(final Context context, final String text) {
            throw _Exceptions.unsupportedOperation(message);
        }

        @Override
        public int typicalLength() {
            return 60;
        }

    }

}
