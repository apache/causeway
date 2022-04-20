package org.apache.isis.viewer.graphql.viewer.source;

import graphql.Scalars;
import graphql.schema.*;
import org.apache.isis.core.config.presets.IsisPresets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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