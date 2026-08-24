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
package org.apache.causeway.viewer.graphql.model.registry;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import graphql.schema.GraphQLTypeReference;
import graphql.schema.GraphQLUnionType;

import static graphql.schema.GraphQLUnionType.newUnionType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class GraphQLTypeRegistryUnionTest {

    @Test
    void repeatedUnionRegistrationMergesPossibleTypesByName() {
        var registry = new GraphQLTypeRegistry(null);
        var first = union("ValueHolder", "FirstType");
        var second = union("ValueHolder", "SecondType", "FirstType");

        assertSame(first, registry.addUnionTypeIfNotAlreadyPresent(first));
        var merged = registry.addUnionTypeIfNotAlreadyPresent(second);

        assertEquals(Set.of("FirstType", "SecondType"), names(merged));
        assertSame(merged, registry.lookup("ValueHolder", GraphQLUnionType.class).orElseThrow());
        assertEquals(1, registry.getGraphQLTypes().size());
    }

    private static GraphQLUnionType union(final String name, final String... possibleTypes) {
        var builder = newUnionType().name(name);
        for (var possibleType : possibleTypes) {
            builder.possibleType(GraphQLTypeReference.typeRef(possibleType));
        }
        return builder.build();
    }

    private static Set<String> names(final GraphQLUnionType unionType) {
        return unionType.getTypes().stream()
                .map(type -> type.getName())
                .collect(Collectors.toSet());
    }
}
