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
package org.apache.causeway.viewer.graphql.applib.types;

import graphql.ExperimentalApi;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;

import org.springframework.lang.Nullable;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyActionParameter;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneFeature;

/**
 * SPI to map framework's own datatypes to GraphQL's types.
 *
 * <p>
 *     The framework provides a default implementation (as a fallback) that supports most of the common data types.
 * </p>
 *
 * <p>
 *     <b>NOTE</b>: this API is considered experimental/beta and may change in the future.
 * </p>
 *
 * @since 2.0 {@index}
 */
@ExperimentalApi
public interface TypeMapper {

    GraphQLScalarType scalarTypeFor(final Class<?> c);

    GraphQLOutputType outputTypeFor(final OneToOneFeature oneToOneFeature);

    @Nullable
    GraphQLOutputType outputTypeFor(final ObjectSpecification objectSpecification);

    @Nullable
    GraphQLList listTypeForElementTypeOf(OneToManyAssociation oneToManyAssociation);

    @Nullable
    GraphQLList listTypeFor(ObjectSpecification elementType);

    GraphQLInputType inputTypeFor(
            final OneToOneFeature oneToOneFeature,
            final InputContext inputContext);

    GraphQLList inputTypeFor(final OneToManyActionParameter oneToManyActionParameter, final InputContext inputContextUnused);

    Object adaptPojo(
            final Object argumentValue,
            final ObjectSpecification elementType);

    enum InputContext {
        HIDE,
        DISABLE,
        VALIDATE,
        CHOICES,
        AUTOCOMPLETE,
        DEFAULT,
        INVOKE,
        SET,
        ;
        public boolean isOptionalAlwaysAllowed() {
            return !(this == INVOKE || this == SET);
        }
    }

}
