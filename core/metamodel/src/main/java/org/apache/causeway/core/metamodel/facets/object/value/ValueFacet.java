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
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.causeway.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.core.metamodel.spec.feature.ObjectFeature;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
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
    Optional<DefaultsProvider<T>> selectDefaultsProviderForParameter(final ObjectActionParameter param);
    Optional<DefaultsProvider<T>> selectDefaultsProviderForProperty(final OneToOneAssociation prop);

    // -- PARSER

    /** no qualifiers allowed on the default semantics provider*/
    Optional<Parser<T>> selectDefaultParser();
    Optional<Parser<T>> selectParserForParameter(final ObjectActionParameter param);
    Optional<Parser<T>> selectParserForProperty(final OneToOneAssociation prop);

    default Optional<Parser<T>> selectParserForFeature(final @Nullable ObjectFeature feature) {
        if(feature==null) {
            return selectDefaultParser();
        }
        switch(feature.getFeatureType()) {
        case ACTION_PARAMETER_SINGULAR:
            return selectParserForParameter((ObjectActionParameter)feature);
        case PROPERTY:
            return selectParserForProperty((OneToOneAssociation)feature);
        default:
            return selectDefaultParser();
        }
    }

    Parser<T> fallbackParser(Identifier featureIdentifier);

    default Parser<T> selectParserForParameterElseFallback(final ObjectActionParameter param) {
        return selectParserForParameter(param)
                .orElseGet(()->fallbackParser(param.getFeatureIdentifier()));
    }

    default Parser<T> selectParserForPropertyElseFallback(final OneToOneAssociation prop) {
        return selectParserForProperty(prop)
                .orElseGet(()->fallbackParser(prop.getFeatureIdentifier()));
    }

    default Parser<T> selectParserForFeatureElseFallback(final ObjectFeature feature) {
        return selectParserForFeature(feature)
                .orElseGet(()->fallbackParser(feature.getFeatureIdentifier()));
    }

    // -- RENDERER

    /** no qualifiers allowed on the default semantics provider*/
    Optional<Renderer<T>> selectDefaultRenderer();
    Optional<Renderer<T>> selectRendererForParameter(final ObjectActionParameter param);
    Optional<Renderer<T>> selectRendererForProperty(final OneToOneAssociation prop);
    Optional<Renderer<T>> selectRendererForCollection(final OneToManyAssociation coll);

    Renderer<T> fallbackRenderer(Identifier featureIdentifier);

    default Optional<Renderer<T>> selectRendererForFeature(final @Nullable ObjectFeature feature) {
        if(feature==null) {
            return selectDefaultRenderer();
        }
        switch(feature.getFeatureType()) {
        case ACTION_PARAMETER_SINGULAR:
            return selectRendererForParameter((ObjectActionParameter)feature);
        case PROPERTY:
            return selectRendererForProperty((OneToOneAssociation)feature);
        case COLLECTION:
            return selectRendererForCollection((OneToManyAssociation)feature);
        default:
            return selectDefaultRenderer();
        }
    }

    default Renderer<T> selectRendererForParameterElseFallback(final ObjectActionParameter param) {
        return selectRendererForParameter(param)
                .orElseGet(()->fallbackRenderer(param.getFeatureIdentifier()));
    }

    default Renderer<T> selectRendererForPropertyElseFallback(final OneToOneAssociation prop) {
        return selectRendererForProperty(prop)
                .orElseGet(()->fallbackRenderer(prop.getFeatureIdentifier()));
    }

    default Renderer<T> selectRendererForCollectionElseFallback(final OneToManyAssociation coll) {
        return selectRendererForCollection(coll)
                .orElseGet(()->fallbackRenderer(coll.getFeatureIdentifier()));
    }

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
