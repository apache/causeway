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
package org.apache.causeway.viewer.graphql.viewer.test.source;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import graphql.Scalars;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;

class ScratchPad {

    static class GQLObjectType {

        GQLObjectType (GraphQLObjectType objectType){
            this.objectType = objectType;
        }

        private List<GraphQLFieldDefinition> fieldDefinitions = new ArrayList<>();

        private GraphQLObjectType objectType;

    }

    @Test
    public void xxx(){

        // given
        GraphQLObjectType query = getQuery();

        Set<GraphQLType> objectTypes = new HashSet<>();

        objectTypes.add(GraphQLObjectType.newObject().name("e1").field(GraphQLFieldDefinition.newFieldDefinition().name("e1").type(GraphQLTypeReference.typeRef("e2")).build()).build());
        objectTypes.add(GraphQLObjectType.newObject().name("e2").field(GraphQLFieldDefinition.newFieldDefinition().name("e2").type(GraphQLTypeReference.typeRef("e1")).build()).build());


        GraphQLSchema schema = GraphQLSchema.newSchema()
                .query(query)
                .additionalTypes(objectTypes)
//                .codeRegistry(codeRegistry)
                .build();
        Assertions.assertEquals(1, schema.getType("e1").getChildren().size());

    }


    private GraphQLObjectType getQuery() {
        GraphQLObjectType.Builder queryBuilder = new GraphQLObjectType.Builder().name("query");
        queryBuilder.field(GraphQLFieldDefinition.newFieldDefinition().name("f1").type(Scalars.GraphQLString).build());
        GraphQLObjectType query = queryBuilder.build();
        return query;
    }

}