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
package org.apache.causeway.viewer.graphql.applib.marshallers;

import graphql.schema.GraphQLScalarType;

import org.apache.causeway.core.config.CausewayConfiguration;

import lombok.Getter;

/**
 * Convenience adapter for {@link ScalarMarshaller} SPI.
 *
 * @param <K>
 */
public abstract class ScalarMarshallerAbstract<K> implements ScalarMarshaller<K> {

    final Class<? extends K> javaClass;

    @Getter
    private final GraphQLScalarType gqlScalarType;

    protected final CausewayConfiguration causewayConfiguration;

    private final boolean supportsInput;

    /**
     * Compatibility constructor for output-only application marshallers.
     */
    protected ScalarMarshallerAbstract(
            final Class<? extends K> javaClass,
            final GraphQLScalarType gqlScalarType,
            final CausewayConfiguration causewayConfiguration) {
        this(javaClass, gqlScalarType, causewayConfiguration, false);
    }

    /**
     * Constructor for marshallers that explicitly declare their input capability.
     */
    protected ScalarMarshallerAbstract(
            final Class<? extends K> javaClass,
            final GraphQLScalarType gqlScalarType,
            final CausewayConfiguration causewayConfiguration,
            final boolean supportsInput) {
        this.javaClass = javaClass;
        this.gqlScalarType = gqlScalarType;
        this.causewayConfiguration = causewayConfiguration;
        this.supportsInput = supportsInput;
    }

    @Override
    public boolean handles(final Class<?> javaClass) {
        return this.javaClass.isAssignableFrom(javaClass);
    }

    @Override
    public boolean supportsInput() {
        return supportsInput;
    }

}
