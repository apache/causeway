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
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR  CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.viewer.graphql.viewer.test.domain.calc;

import java.util.Locale;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.viewer.graphql.applib.marshallers.ScalarMarshallerAbstract;

import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;

@Component
@Priority(PriorityPrecedence.EARLY)
public class ScalarMarshallerComplexNumber extends ScalarMarshallerAbstract<ComplexNumber> {

    private static final GraphQLScalarType SCALAR = GraphQLScalarType.newScalar()
            .name("ComplexNumber")
            .description("Application-defined reversible complex number in algebraic form, such as 3+4i or 3-4i.")
            .coercing(new Coercing<ComplexNumber, String>() {
                @Override
                public String serialize(
                        final Object dataFetcherResult,
                        final GraphQLContext graphQLContext,
                        final Locale locale) {
                    if (dataFetcherResult instanceof ComplexNumber complexNumber) {
                        return complexNumber.canonical();
                    }
                    throw new CoercingSerializeException("Expected ComplexNumber output");
                }

                @Override
                public ComplexNumber parseValue(
                        final Object input,
                        final GraphQLContext graphQLContext,
                        final Locale locale) {
                    if (!(input instanceof String text)) {
                        throw new CoercingParseValueException("Expected ComplexNumber input as a string");
                    }
                    try {
                        return ComplexNumber.parse(text);
                    } catch (RuntimeException ex) {
                        throw new CoercingParseValueException("Invalid ComplexNumber value");
                    }
                }

                @Override
                public ComplexNumber parseLiteral(
                        final Value<?> input,
                        final CoercedVariables variables,
                        final GraphQLContext graphQLContext,
                        final Locale locale) {
                    if (!(input instanceof StringValue stringValue)) {
                        throw new CoercingParseLiteralException("Expected ComplexNumber input as a string literal");
                    }
                    try {
                        return ComplexNumber.parse(stringValue.getValue());
                    } catch (RuntimeException ex) {
                        throw new CoercingParseLiteralException("Invalid ComplexNumber value");
                    }
                }
            })
            .build();

    @Inject
    public ScalarMarshallerComplexNumber(final CausewayConfiguration causewayConfiguration) {
        super(ComplexNumber.class, SCALAR, causewayConfiguration, true);
    }

    @Override
    public ComplexNumber unmarshal(final Object graphValue, final Class<?> targetType) {
        return graphValue instanceof ComplexNumber complexNumber
                ? complexNumber
                : ComplexNumber.parse((String) graphValue);
    }
}
