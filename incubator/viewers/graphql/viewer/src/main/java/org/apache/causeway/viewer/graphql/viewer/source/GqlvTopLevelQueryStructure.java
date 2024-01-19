package org.apache.causeway.viewer.graphql.viewer.source;

import graphql.schema.GraphQLObjectType;

import lombok.Getter;

import static graphql.schema.GraphQLObjectType.newObject;

public class GqlvTopLevelQueryStructure {

    @Getter final GraphQLObjectType.Builder queryBuilder;

    public GqlvTopLevelQueryStructure() {
        queryBuilder = newObject().name("Query");
    }

}
