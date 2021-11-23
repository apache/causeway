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
import java.util.stream.Stream;

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
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeature;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

/**
 * Indicates that this class has value semantics.
 *
 * <p>
 * In the standard Apache Isis Programming Model, corresponds to the
 * <tt>@Value</tt> annotation. However, note that value semantics is just a
 * convenient term for a number of mostly optional semantics all of which are
 * defined elsewhere.
 */
public interface ValueFacet<T> extends Facet {

    LogicalType getValueType();
    Can<ValueSemanticsProvider<T>> getValueSemantics();
    Context createValueSemanticsContext(@Nullable ObjectFeature feature);
    <X> Stream<X> streamValueSemantics(Class<X> requiredType);

    // -- ORDER RELATION

    /** no qualifiers allowed on the default semantics provider*/
    Optional<OrderRelation<T, ?>> selectDefaultOrderRelation();

    // -- ENCODER DECODER

    /** no qualifiers allowed on the default semantics provider*/
    Optional<EncoderDecoder<T>> selectDefaultEncoderDecoder();

    // -- PARSER

    /** no qualifiers allowed on the default semantics provider*/
    Optional<Parser<T>> selectDefaultParser();
    Optional<Parser<T>> selectParserForParameter(final ObjectActionParameter param);
    Optional<Parser<T>> selectParserForProperty(final OneToOneAssociation prop);

    default Optional<Parser<T>> selectParserForFeature(final ObjectFeature feature) {
        switch(feature.getFeatureType()) {
        case ACTION_PARAMETER_SCALAR:
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

    Renderer<T> fallbackRenderer(Identifier featureIdentifier);

    default Renderer<T> selectRendererForParameterElseFallback(final ObjectActionParameter param) {
        return selectRendererForParameter(param)
                .orElseGet(()->fallbackRenderer(param.getFeatureIdentifier()));
    }

    default Renderer<T> selectRendererForPropertyElseFallback(final OneToOneAssociation prop) {
        return selectRendererForProperty(prop)
                .orElseGet(()->fallbackRenderer(prop.getFeatureIdentifier()));
    }


}
