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

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.applib.value.semantics.EncoderDecoder;
import org.apache.isis.applib.value.semantics.OrderRelation;
import org.apache.isis.applib.value.semantics.Parser;
import org.apache.isis.applib.value.semantics.Renderer;
import org.apache.isis.applib.value.semantics.ValueSemanticsProvider;
import org.apache.isis.applib.value.semantics.ValueSemanticsProvider.Context;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.reflection._Annotations;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.objectvalue.valuesemantics.ValueSemanticsSelectingFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeature;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

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
    public <X> Stream<X> streamValueSemantics(final Class<X> requiredType) {
        return valueSemantics.stream()
        .filter(requiredType::isInstance)
        .map(requiredType::cast);
    }

    @Override
    public boolean semanticEquals(@NonNull final Facet other) {
        return (other instanceof ValueFacetAbstract)
                ? this.getValueSemantics().equals(((ValueFacetAbstract<?>)other).getValueSemantics())
                : false;
    }

    @Override
    public final LogicalType getValueType() {
        return getFacetHolder().getFeatureIdentifier().getLogicalType();
    }

    @Override
    public ValueSemanticsProvider.Context createValueSemanticsContext(final @Nullable ObjectFeature feature) {
        val iaProvider = super.getInteractionProvider();
        if(iaProvider==null) {
            return null; // JUnit context
        }
        return ValueSemanticsProvider.Context.of(
                feature!=null
                    ? feature.getFeatureIdentifier()
                    : null,
                iaProvider.currentInteractionContext().orElse(null));
    }

    // -- ORDER RELATION

    @Override
    public Optional<OrderRelation<T, ?>> selectDefaultOrderRelation() {
        return getValueSemantics()
                .stream()
                .filter(isMatchingAnyOf(Can.empty()))
                .map(ValueSemanticsProvider::getOrderRelation)
                .filter(_NullSafe::isPresent)
                .findFirst()
                .map(rel->(OrderRelation<T, ?>)rel);
    }

    // -- ENCODER DECODER

    @Override
    public Optional<EncoderDecoder<T>> selectDefaultEncoderDecoder() {
        return getValueSemantics()
                .stream()
                .filter(isMatchingAnyOf(Can.empty()))
                .map(ValueSemanticsProvider::getEncoderDecoder)
                .filter(_NullSafe::isPresent)
                .findFirst();
    }

    // -- PARSER

    @Override
    public Optional<Parser<T>> selectDefaultParser() {
        return getValueSemantics()
                .stream()
                .filter(isMatchingAnyOf(Can.empty()))
                .map(ValueSemanticsProvider::getParser)
                .filter(_NullSafe::isPresent)
                .findFirst();
    }

    @Override
    public Optional<Parser<T>> selectParserForParameter(final ObjectActionParameter param) {
        return streamValueSemanticsHonoringQualifiers(param)
                .map(ValueSemanticsProvider::getParser)
                .filter(_NullSafe::isPresent)
                .findFirst();
    }

    @Override
    public Optional<Parser<T>> selectParserForProperty(final OneToOneAssociation prop) {
        return streamValueSemanticsHonoringQualifiers(prop)
                .map(ValueSemanticsProvider::getParser)
                .filter(_NullSafe::isPresent)
                .findFirst();
    }

    @Override
    public Parser<T> fallbackParser(final Identifier featureIdentifier) {
        return fallbackParser(getValueType(), featureIdentifier);
    }

    // -- RENDERER

    @Override
    public Optional<Renderer<T>> selectDefaultRenderer() {
        return getValueSemantics()
                .stream()
                .filter(isMatchingAnyOf(Can.empty()))
                .map(ValueSemanticsProvider::getRenderer)
                .filter(_NullSafe::isPresent)
                .findFirst();
    }

    @Override
    public Optional<Renderer<T>> selectRendererForParameter(final ObjectActionParameter param) {
        return streamValueSemanticsHonoringQualifiers(param)
                .map(ValueSemanticsProvider::getRenderer)
                .filter(_NullSafe::isPresent)
                .findFirst();
    }

    @Override
    public Optional<Renderer<T>> selectRendererForProperty(final OneToOneAssociation prop) {
        return streamValueSemanticsHonoringQualifiers(prop)
                .map(ValueSemanticsProvider::getRenderer)
                .filter(_NullSafe::isPresent)
                .findFirst();
    }

    @Override
    public Renderer<T> fallbackRenderer(final Identifier featureIdentifier) {
        return fallbackRenderer(getValueType(), featureIdentifier);
    }

    // -- UTILITY

    public static <X> Parser<X> fallbackParser(
            final LogicalType valueType,
            final Identifier featureIdentifier) {
        return new PseudoParserWithMessage<X>(String
                .format("Could not find a parser for type %s "
                        + "in the context of %s",
                        valueType,
                        featureIdentifier));
    }

    public static <X> Renderer<X> fallbackRenderer(
            final LogicalType valueType,
            final Identifier featureIdentifier) {
        return new PseudoRendererWithMessage<X>(String
                .format("Could not find a renderer for type %s "
                        + "in the context of %s",
                        valueType,
                        featureIdentifier));
    }

    // -- HELPER

    private Stream<ValueSemanticsProvider<T>> streamValueSemanticsHonoringQualifiers(
            final FacetHolder feature) {
        return getValueSemantics()
                .stream()
                .filter(isMatchingAnyOf(qualifiersAccepted(feature)));
    }

    private Can<String> qualifiersAccepted(final FacetHolder feature) {
        return feature.lookupFacet(ValueSemanticsSelectingFacet.class)
        .map(ValueSemanticsSelectingFacet::value)
        .map(_Strings::emptyToNull)
        .stream()
        .collect(Can.toCan());
    }

    private Predicate<ValueSemanticsProvider<T>> isMatchingAnyOf(final Can<String> qualifiersAccepted) {
        return valueSemantics->{

            // qualifiers accepted vs. qualifiers present on bean type
            // 1. empty     vs. empty      ->  accept
            // 2. empty     vs. not-empty  ->  reject
            // 3. not-empty vs. empty      ->  reject
            // 4. not-empty vs. not-empty  ->  accept when any match

            val qualifiersOnBean =
            _Annotations
            .synthesizeInherited(valueSemantics.getClass(), Qualifier.class) //TODO memoize somewhere
            .map(Qualifier::value)
            .stream()
            .map(_Strings::emptyToNull)
            .collect(Can.toCan());

            if(qualifiersAccepted.isEmpty()
                    && qualifiersOnBean.isEmpty()) {
                return true;
            }

            if(qualifiersAccepted.isNotEmpty()
                    && qualifiersOnBean.isNotEmpty()) {
                return qualifiersAccepted.stream().anyMatch(qualifiersOnBean::contains);
            }

            return false;
        };
    }

    @RequiredArgsConstructor
    private final static class PseudoRendererWithMessage<T>
    implements Renderer<T> {

        private final String message;

        @Override
        public String simpleTextPresentation(final Context context, final T value) {
            return message;
        }

    }

    @RequiredArgsConstructor
    private final static class PseudoParserWithMessage<T>
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
