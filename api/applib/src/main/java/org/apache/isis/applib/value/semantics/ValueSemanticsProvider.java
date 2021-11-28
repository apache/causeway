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

package org.apache.isis.applib.value.semantics;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Value;
import org.apache.isis.applib.services.iactnlayer.InteractionContext;
import org.apache.isis.schema.chg.v2.ChangesDto;
import org.apache.isis.schema.cmd.v2.CommandDto;
import org.apache.isis.schema.common.v2.ValueType;
import org.apache.isis.schema.ixn.v2.InteractionDto;

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
 * <tt>FacetHolder</tt>, and <tt>IsisConfiguration</tt> and a
 * <tt>ValueSemanticsProviderContext</tt>. This constructor is then used by the
 * framework to instantiate the object reflectively.
 *
 * @see Parser
 * @see EncoderDecoder
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
     * The {@link EncoderDecoder}, if any.
     */
    EncoderDecoder<T> getEncoderDecoder();

    /**
     * The {@link DefaultsProvider}, if any.
     *
     * <p>
     * If not <tt>null</tt>, implies that the value has (or may have) a default.
     */
    DefaultsProvider<T> getDefaultsProvider();

}
