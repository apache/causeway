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

package org.apache.causeway.applib.value.semantics;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.Value;
import org.apache.causeway.applib.services.bookmark.IdStringifier;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.schema.chg.v2.ChangesDto;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.common.v2.TypedTupleDto;
import org.apache.causeway.schema.common.v2.ValueType;
import org.apache.causeway.schema.common.v2.ValueWithTypeDto;
import org.apache.causeway.schema.ixn.v2.InteractionDto;

/**
 * Provides a mechanism for providing a set of value semantics.
 * <p>
 * This interface is used by {@link Value} to allow these semantics to be
 * provided through a single point. Alternatively, {@link Value} supports this
 * information being provided via the configuration files.
 * <p>
 * Whatever the class that implements this interface, it must also expose either
 * a <tt>public</tt> no-arg constructor, or (for implementations that also are
 * <tt>Facet</tt>s) a <tt>public</tt> constructor that accepts a
 * <tt>FacetHolder</tt>, and <tt>CausewayConfiguration</tt> and a
 * <tt>ValueSemanticsProviderContext</tt>. This constructor is then used by the
 * framework to instantiate the object reflectively.
 *
 * @see Parser
 * @see DefaultsProvider
 * @since 1.x {@index}
 */
public interface ValueSemanticsProvider<T> {

    @lombok.Value(staticConstructor = "of")
    class Context {
        private final @Nullable Identifier featureIdentifier;
        private final @Nullable InteractionContext interactionContext;
    }

    Class<T> getCorrespondingClass();

    /**
     * Values might appear within {@link CommandDto}, {@link InteractionDto} and
     * {@link ChangesDto}, where a mapping onto one of {@link ValueType}(s) as provided by the
     * XML schema is required.
     */
    ValueType getSchemaValueType();

    /**
     * Converts a value object into either a {@link ValueWithTypeDto}
     * or {@link TypedTupleDto}.
     */
    ValueDecomposition decompose(T value);

    /**
     * Converts either a {@link ValueWithTypeDto} or
     * a {@link TypedTupleDto}
     * to an instance of the object.
     *
     * @see #decompose(Object)
     */
    T compose(ValueDecomposition decomposition);

    // --


    /**
     * The {@link OrderRelation}, if any.
     */
    OrderRelation<T, ?> getOrderRelation();

    /**
     * The {@link Converter}, if any.
     */
    Converter<T, ?> getConverter();

    /**
     * The {@link Renderer}, if any.
     */
    Renderer<T> getRenderer();

    /**
     * The {@link Parser}, if any.
     */
    Parser<T> getParser();

    /**
     * The {@link DefaultsProvider}, if any.
     * <p>
     * If not <tt>null</tt>, implies that the value has (or may have) a default.
     */
    DefaultsProvider<T> getDefaultsProvider();

    /**
     * The {@link IdStringifier}, if any.
     */
    IdStringifier<T> getIdStringifier();

    // -- UTILITY

    default <X> ValueSemanticsProvider<X> castTo(final Class<X> cls) {
        return _Casts.uncheckedCast(this);
    }

    // -- CATEGORIZATION

    default boolean isEnumType() {
        return getSchemaValueType()==ValueType.ENUM;
    }

    default boolean isNumberType() {
        return getSchemaValueType()==ValueType.BIG_DECIMAL
                || getSchemaValueType()==ValueType.BIG_INTEGER
                || getSchemaValueType()==ValueType.LONG
                || getSchemaValueType()==ValueType.INT
                || getSchemaValueType()==ValueType.SHORT
                || getSchemaValueType()==ValueType.BYTE
                || getSchemaValueType()==ValueType.DOUBLE
                || getSchemaValueType()==ValueType.FLOAT;
    }

    default boolean isTemporalType() {
        return getSchemaValueType().name().contains("DATE")
                || getSchemaValueType().name().contains("TIME");
    }

    default boolean isCompositeType() {
        return getSchemaValueType()==ValueType.COMPOSITE;
    }

}
