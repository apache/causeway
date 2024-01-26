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
package org.apache.causeway.viewer.graphql.model.types;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import graphql.Scalars;
import graphql.schema.*;

import lombok.experimental.UtilityClass;
import lombok.val;

import javax.annotation.Priority;
import javax.ws.rs.NotSupportedException;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyActionParameter;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneFeature;
import org.apache.causeway.viewer.graphql.model.domain.TypeNames;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import static graphql.schema.GraphQLNonNull.nonNull;
import static graphql.schema.GraphQLTypeReference.typeRef;

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
        boolean isOptionalAlwaysAllowed() {
            return !(this == INVOKE || this == SET);
        }
    }
}
