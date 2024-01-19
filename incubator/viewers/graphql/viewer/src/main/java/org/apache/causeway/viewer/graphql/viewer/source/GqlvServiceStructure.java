package org.apache.causeway.viewer.graphql.viewer.source;

import graphql.schema.GraphQLObjectType;

import lombok.Getter;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

public class GqlvServiceStructure {

    @Getter private final ObjectSpecification serviceSpec;
    @Getter private final GqlvTopLevelQueryStructure topLevelQueryStructure;

    private final GraphQLObjectType.Builder queryBuilder;

    private GraphQLObjectType.Builder gqlObjectTypeBuilder;

    // TODO - don't expose
    public GraphQLObjectType.Builder getGraphQlTypeBuilder() {
        return gqlObjectTypeBuilder;
    }

    public GqlvServiceStructure(ObjectSpecification serviceSpec, GqlvTopLevelQueryStructure topLevelQueryStructure) {
        this.serviceSpec = serviceSpec;
        this.topLevelQueryStructure = topLevelQueryStructure;

        queryBuilder = topLevelQueryStructure.getQueryBuilder();

        gqlObjectTypeBuilder = newObject().name(_LTN.sanitized(serviceSpec));
    }

    public void addTypeToTopLevelQuery() {
        queryBuilder.field(newFieldDefinition()
                .name(_LTN.sanitized(serviceSpec))
                .type(getGraphQlTypeBuilder())
                .build());
    }

}
