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
package org.apache.causeway.viewer.graphql.viewer.test.e2e.special;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.viewer.graphql.model.types.BuiltInValueTypeClassifications;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;
import org.apache.causeway.viewer.graphql.viewer.test.e2e.Abstract_IntegTest;

import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLType;

import static org.assertj.core.api.Assertions.assertThat;

@Order(64)
public class BuiltInValueTypeClassifications_IntegTest extends Abstract_IntegTest {

    @Inject
    private List<ValueSemanticsProvider<?>> valueSemanticsProviders;

    @Inject
    private TypeMapper typeMapper;

    @Test
    void everyFrameworkValueSemanticsHasAnExplicitGraphQlClassification() {
        Set<String> frameworkValueTypes = valueSemanticsProviders.stream()
                .filter(provider -> provider.getClass().getName()
                        .startsWith("org.apache.causeway.core.metamodel.valuesemantics."))
                .map(ValueSemanticsProvider::getCorrespondingClass)
                .map(Class::getName)
                .collect(Collectors.toSet());

        assertThat(BuiltInValueTypeClassifications.classNames())
                .containsExactlyInAnyOrderElementsOf(frameworkValueTypes);
    }

    @Test
    void everyClassificationMatchesItsGeneratedGraphQlCapability() throws Exception {
        for (var className : BuiltInValueTypeClassifications.classNames()) {
            var javaType = Class.forName(className);
            var classification = BuiltInValueTypeClassifications.classificationFor(javaType).orElseThrow();
            var inputName = typeName(typeMapper.inputTypeFor(javaType));
            var outputName = typeName(typeMapper.outputTypeFor(javaType));

            switch (classification) {
                case REVERSIBLE, PROTECTED, STRUCTURED -> {
                    assertThat(inputName).as(className + " input").isNotEqualTo("UnsupportedValue");
                    assertThat(outputName).as(className + " output").isNotEqualTo("UnsupportedValue");
                }
                case OUTPUT_ONLY -> {
                    assertThat(inputName).as(className + " input").isEqualTo("UnsupportedValue");
                    assertThat(outputName).as(className + " output").isNotEqualTo("UnsupportedValue");
                }
                case UNSUPPORTED -> {
                    assertThat(inputName).as(className + " input").isEqualTo("UnsupportedValue");
                    assertThat(outputName).as(className + " output").isEqualTo("UnsupportedValue");
                }
            }
        }
    }

    private static String typeName(final GraphQLType type) {
        return ((GraphQLNamedType) type).getName();
    }
}
