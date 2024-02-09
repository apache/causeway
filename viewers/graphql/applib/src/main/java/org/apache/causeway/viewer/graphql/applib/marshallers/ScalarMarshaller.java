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

import org.springframework.lang.NonNull;

/**
 * Provides an SPI to allow different scalar datatypes to be marshalled to and from GraphQL scalar types.
 *
 * <p>
 *     The implementations are called following a chain-of-responsibility pattern, first one matching is used.
 *     Use {@link javax.annotation.Priority} (with {@link org.apache.causeway.applib.annotation.PriorityPrecedence} values)
 *     to override the framework-provided defaults, earliest wins.
 * </p>
 *
 * @param <K>
 *
 * @since 2.0 {@index}
 */
public interface ScalarMarshaller<K> {

    /**
     * Whether this marshaller is able to marshall/unmarshall the provided Java class.
     *
     * @param javaClass
     * @return
     */
    boolean handles(Class<?> javaClass);

    /**
     * The corresponding GraphQL scalar type for the Java-class.
     * @return
     */
    GraphQLScalarType getGqlScalarType();

    /**
     * Unmarshal the provided graphQL value into its Java equivalent.
     *
     * @param graphValue - to be unmarshalled.  This will never be null.
     * @param targetType - the required type.  Usually isn't required, though the fallback Object marshaller uses it to correctly marshal enums.
     * @return
     */
    K unmarshal(
            final Object graphValue,
            @NonNull final Class<?> targetType);
}
