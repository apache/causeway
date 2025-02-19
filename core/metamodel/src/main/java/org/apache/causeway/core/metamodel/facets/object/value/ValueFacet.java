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
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.value.semantics.DefaultsProvider;
import org.apache.causeway.applib.value.semantics.OrderRelation;
import org.apache.causeway.applib.value.semantics.Parser;
import org.apache.causeway.applib.value.semantics.Renderer;
import org.apache.causeway.applib.value.semantics.TemporalSupport;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider.Context;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.causeway.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectFeature;
import org.apache.causeway.schema.common.v2.ValueType;

/**
 * Indicates that this class has value semantics.
 *
 * <p>
 * In the standard Apache Causeway Programming Model, corresponds to the
 * <tt>@Value</tt> annotation. However, note that value semantics is just a
 * convenient term for a number of mostly optional semantics all of which are
 * defined elsewhere.
 */
public interface ValueFacet<T>
extends
    ValueSerializer<T>,
    Facet {

    Class<T> getValueClass();

    LogicalType getLogicalType();

    Can<ValueSemanticsProvider<T>> getAllValueSemantics();

    Context createValueSemanticsContext(@Nullable ObjectFeature feature);
    <X> Stream<X> streamValueSemantics(Class<X> requiredType);

    /** no qualifiers allowed on the default semantics provider*/
    Optional<ValueSemanticsProvider<T>> selectDefaultSemantics();

    // -- ORDER RELATION

    /** no qualifiers allowed on the default semantics provider*/
    Optional<OrderRelation<T, ?>> selectDefaultOrderRelation();

    // -- DEFAULTS PROVIDER

    /** no qualifiers allowed on the default semantics provider*/
    Optional<DefaultsProvider<T>> selectDefaultDefaultsProvider();
    Optional<DefaultsProvider<T>> selectDefaultsProviderForAttribute(final @Nullable ObjectFeature feature);

    // -- PARSER

    /** no qualifiers allowed on the default semantics provider*/
    Optional<Parser<T>> selectDefaultParser();
    Optional<Parser<T>> selectParserForAttribute(@NonNull ObjectFeature feature);

    default Optional<Parser<T>> selectParserForFeature(final @Nullable ObjectFeature feature) {
        return feature==null
            ? selectDefaultParser()
            : switch(feature.getFeatureType()) {
                case ACTION_PARAMETER_SINGULAR, PROPERTY->selectParserForAttribute(feature);
                default->selectDefaultParser();
            };
    }

    Parser<T> fallbackParser(Identifier featureIdentifier);

    default Parser<T> selectParserForAttributeOrElseFallback(final @NonNull ObjectFeature feature) {
        return selectParserForAttribute(feature)
                .orElseGet(()->fallbackParser(feature.getFeatureIdentifier()));
    }

    // -- RENDERER

    /** no qualifiers allowed on the default semantics provider*/
    Optional<Renderer<T>> selectDefaultRenderer();
    Optional<Renderer<T>> selectRendererForParamOrPropOrColl(@NonNull ObjectFeature param);

    Renderer<T> fallbackRenderer(Identifier featureIdentifier);

    default Optional<Renderer<T>> selectRendererForFeature(final @Nullable ObjectFeature feature) {
        return feature==null
            ? selectDefaultRenderer()
            : switch(feature.getFeatureType()) {
                case ACTION_PARAMETER_SINGULAR, PROPERTY, COLLECTION->
                    selectRendererForParamOrPropOrColl(feature);
                default->selectDefaultRenderer();
            };
    }

    default Renderer<T> selectRendererForParamOrPropOrCollOrElseFallback(final @NonNull ObjectFeature feature) {
        return selectRendererForParamOrPropOrColl(feature)
                .orElseGet(()->fallbackRenderer(feature.getFeatureIdentifier()));
    }

    // -- TEMPORAL SUPPORT

    Optional<TemporalSupport<T>> selectTemporalSupportForFeature(final @Nullable ObjectFeature feature);

    // -- COMPOSITE VALUE SUPPORT

    Optional<ObjectAction> selectCompositeValueMixinForParameter(
            final ParameterNegotiationModel parameterNegotiationModel,
            final int paramIndex);
    Optional<ObjectAction> selectCompositeValueMixinForProperty(final ManagedProperty managedProperty);

    default boolean isCompositeValueType() {
        return selectDefaultSemantics()
        .map(valueSemantics->valueSemantics.getSchemaValueType()==ValueType.COMPOSITE)
        .orElse(false);
    }

}
