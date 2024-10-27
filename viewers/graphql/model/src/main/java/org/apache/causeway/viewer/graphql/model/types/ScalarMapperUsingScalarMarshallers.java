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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import graphql.schema.GraphQLScalarType;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import org.apache.causeway.viewer.graphql.applib.marshallers.ScalarMarshaller;

import lombok.RequiredArgsConstructor;

/**
 * Internal (default) implementation of {@link ScalarMapper} that implements the chain-of-responsibility
 * pattern over an injected list of all known {@link ScalarMarshaller} components.
 */
@Component
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ScalarMapperUsingScalarMarshallers implements ScalarMapper {

    @Configuration
    public static class AutoConfiguration {

        @Bean
        @ConditionalOnMissingBean(ScalarMapperUsingScalarMarshallers.class)
        public ScalarMapper defaultScalarMapper(final List<ScalarMarshaller<?>> scalarMarshallers) {
            return new ScalarMapperUsingScalarMarshallers(scalarMarshallers);
        }
    }

    private final List<ScalarMarshaller<?>> scalarMarshallers;

    /**
     * Lazily populated cache
     */
    final Map<Class<?>, ScalarMarshaller<?>> scalarMarshallerByClass = new HashMap<>();

    @Override
    public GraphQLScalarType scalarTypeFor(final Class<?> clazz){
        return scalarMarshallerFor(clazz).getGqlScalarType();
    }

    @Override
    public Object unmarshal(
            final Object argumentValue,
            final Class<?> targetType) {

        ScalarMarshaller<?> scalarMarshaller = scalarMarshallerFor(targetType);
        return scalarMarshaller.unmarshal(argumentValue, targetType);
    }

    private ScalarMarshaller<?> scalarMarshallerFor(Class<?> c) {
        return scalarMarshallerByClass.computeIfAbsent(c, cls -> {
            for (ScalarMarshaller<?> scalarMarshaller : scalarMarshallers) {
                if (scalarMarshaller.handles(cls)) {
                    return scalarMarshaller;
                }
            }
            // should never happen because we have ScalarMarshallerObject to act as a fallback.
            throw new IllegalArgumentException("Could not locate a ScalarMarshaller to handle class " + c);
        });
    }

}
