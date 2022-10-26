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
package org.apache.causeway.core.metamodel.facets.object.value;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.value.semantics.DefaultsProvider;
import org.apache.causeway.applib.value.semantics.OrderRelation;
import org.apache.causeway.applib.value.semantics.Parser;
import org.apache.causeway.applib.value.semantics.Renderer;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider.Context;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.reflection._Annotations;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.objectvalue.valuesemantics.ValueSemanticsSelectingFacet;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.causeway.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.core.metamodel.spec.feature.ObjectFeature;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.core.metamodel.specloader.specimpl.ObjectActionMixedIn;

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
    private final Class<T> valueClass;

    @Getter(onMethod_ = {@Override})
    private final Can<ValueSemanticsProvider<T>> allValueSemantics;
    private final ValueSerializer<T> valueSerializer;

    protected ValueFacetAbstract(
            final Class<T> valueClass,
            final Can<ValueSemanticsProvider<T>> allValueSemantics,
            final FacetHolder holder,
            final Facet.Precedence precedence) {

        super(type(), holder, precedence);
        this.valueClass = valueClass;
        this.allValueSemantics = allValueSemantics;
        this.valueSerializer = selectDefaultSemantics()
                .map(ValueSerializerDefault::forSemantics)
                .orElse(null); // JUnit support
    }

    protected boolean hasSemanticsProvider() {
        return !this.allValueSemantics.isEmpty();
    }

    @Override
    public <X> Stream<X> streamValueSemantics(final Class<X> requiredType) {
        return allValueSemantics.stream()
        .filter(requiredType::isInstance)
        .map(requiredType::cast);
    }

    @Override
    public boolean semanticEquals(@NonNull final Facet other) {
        return (other instanceof ValueFacetAbstract)
                ? this.getAllValueSemantics().equals(((ValueFacetAbstract<?>)other).getAllValueSemantics())
                : false;
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("default-semantics", selectDefaultSemantics()
                .map(ValueSemanticsProvider::toString)
                .orElse("none"));
    }

    @Override
    public final LogicalType getLogicalType() {
        return getFacetHolder().getFeatureIdentifier().getLogicalType();
    }

    @Override
    public ValueSemanticsProvider.Context createValueSemanticsContext(final @Nullable ObjectFeature feature) {
        val iaProvider = super.getInteractionService();
        if(iaProvider==null) {
            return null; // JUnit context
        }
        return ValueSemanticsProvider.Context.of(
                feature!=null
                    ? feature.getFeatureIdentifier()
                    : null,
                iaProvider.currentInteractionContext().orElse(null));
    }

    // -- TO/FROM STRING SERIALIZATION

    @Override
    public final T destring(final Format format, final String encodedData) {
        return valueSerializer.destring(format, encodedData);
    }

    @Override
    public final String enstring(final Format format, final T value) {
        return valueSerializer.enstring(format, value);
    }

    // -- ORDER RELATION

    @Override
    public Optional<OrderRelation<T, ?>> selectDefaultOrderRelation() {
        return getAllValueSemantics()
                .stream()
                .filter(isMatchingAnyOf(Can.empty()))
                .map(ValueSemanticsProvider::getOrderRelation)
                .filter(_NullSafe::isPresent)
                .findFirst()
                .map(rel->(OrderRelation<T, ?>)rel);
    }

    // -- DEFAULT SEMANTICS

    @Override
    public Optional<ValueSemanticsProvider<T>> selectDefaultSemantics() {
        return getAllValueSemantics()
                .stream()
                .filter(isMatchingAnyOf(Can.empty()))
                .filter(_NullSafe::isPresent)
                .findFirst();
    }

    // -- DEFAULTS PROVIDER

    @Override
    public Optional<DefaultsProvider<T>> selectDefaultDefaultsProvider() {
        return getAllValueSemantics()
                .stream()
                .filter(isMatchingAnyOf(Can.empty()))
                .map(ValueSemanticsProvider::getDefaultsProvider)
                .filter(_NullSafe::isPresent)
                .findFirst();
    }

    @Override
    public Optional<DefaultsProvider<T>> selectDefaultsProviderForParameter(final ObjectActionParameter param) {
        return streamValueSemanticsHonoringQualifiers(param)
                .map(ValueSemanticsProvider::getDefaultsProvider)
                .filter(_NullSafe::isPresent)
                .findFirst();
    }

    @Override
    public Optional<DefaultsProvider<T>> selectDefaultsProviderForProperty(final OneToOneAssociation prop) {
        return streamValueSemanticsHonoringQualifiers(prop)
                .map(ValueSemanticsProvider::getDefaultsProvider)
                .filter(_NullSafe::isPresent)
                .findFirst();
    }

    // -- PARSER

    @Override
    public Optional<Parser<T>> selectDefaultParser() {
        return getAllValueSemantics()
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
        return fallbackParser(getLogicalType(), featureIdentifier);
    }

    // -- RENDERER

    @Override
    public Optional<Renderer<T>> selectDefaultRenderer() {
        return getAllValueSemantics()
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
    public Optional<Renderer<T>> selectRendererForCollection(final OneToManyAssociation coll) {
        return streamValueSemanticsHonoringQualifiers(coll)
                .map(ValueSemanticsProvider::getRenderer)
                .filter(_NullSafe::isPresent)
                .findFirst();
    }

    @Override
    public Renderer<T> fallbackRenderer(final Identifier featureIdentifier) {
        return fallbackRenderer(getLogicalType(), featureIdentifier);
    }

    // -- COMPOSITE VALUE SUPPORT

    @Override
    public Optional<ObjectAction> selectCompositeValueMixinForParameter(
            final ParameterNegotiationModel parameterNegotiationModel,
            final int paramIndex) {
        if(!isCompositeValueType()) {
            return Optional.empty();
        }
        //feed the action's invocation result back into the parameter negotiation model of the parent edit dialog
        return resolveCompositeValueMixinForFeature(parameterNegotiationModel.getParamMetamodel(paramIndex))
                .map(m->CompositeValueUpdaterForParameter
                        .createProxy(parameterNegotiationModel, paramIndex, (ObjectActionMixedIn) m));
    }

    @Override
    public Optional<ObjectAction> selectCompositeValueMixinForProperty(final ManagedProperty managedProperty) {
        if(!isCompositeValueType()) {
            return Optional.empty();
        }
        //feed the action's invocation result back into the scalarModel's proposed value, then submit
        return resolveCompositeValueMixinForFeature(managedProperty.getProperty())
                .map(m->CompositeValueUpdaterForProperty.createProxy(managedProperty, (ObjectActionMixedIn) m));
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
        return getAllValueSemantics()
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
            .synthesize(valueSemantics.getClass(), Qualifier.class) //TODO memoize somewhere
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

    /**
     * Composite-value mixins are collected with the value-type's {@link ObjectSpecification}.
     * By convention, mixed in action names must correspond to the qualifier name,
     * which they are associated to. In the default case (no qualifier), the name 'default' is used.
     *
     * @param feature - optionally provides (custom) qualifier
     */
    protected Optional<ObjectAction> resolveCompositeValueMixinForFeature(final ObjectFeature feature) {
        return qualifiersAccepted(feature).add("default")
        .stream()
        .map(qualifier->feature.getElementType().getAction(qualifier, MixedIn.ONLY))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
    }

    @RequiredArgsConstructor
    private final static class PseudoRendererWithMessage<T>
    implements Renderer<T> {

        private final String message;

        @Override
        public String titlePresentation(final Context context, final T value) {
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
