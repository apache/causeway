package org.apache.causeway.viewer.graphql.viewer.source;

import graphql.Scalars;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;

import lombok.Getter;

import java.util.Optional;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

public class GqlvTopLevelQueryStructure {

    @Getter final GraphQLObjectType.Builder queryBuilder;

    @Getter private GraphQLFieldDefinition numServicesField;

    /**
     * Built using {@link #buildQueryType()}
     */
    private GraphQLObjectType queryType;


    public GqlvTopLevelQueryStructure() {
        queryBuilder = newObject().name("Query");

        numServicesField = newFieldDefinition()
                .name("numServices")
                .type(Scalars.GraphQLInt)
                .build();
        queryBuilder.field(numServicesField);
    }



    public GraphQLObjectType buildQueryType() {
        if (queryType != null) {
            throw new IllegalStateException("QueryType has already been built");
        }
        return queryType = queryBuilder.build();
    }

    /**
     *
     * @see #buildQueryType()
     */
    public GraphQLObjectType getQueryType() {
        if (queryType == null) {
            throw new IllegalStateException("QueryType has not yet been built");
        }
        return queryType;
    }

}
