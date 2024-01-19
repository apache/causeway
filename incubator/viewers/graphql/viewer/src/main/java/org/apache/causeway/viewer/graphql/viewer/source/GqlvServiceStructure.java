package org.apache.causeway.viewer.graphql.viewer.source;

import graphql.schema.GraphQLObjectType;

import lombok.Getter;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

public class GqlvServiceStructure {

    @Getter private final ObjectSpecification serviceSpec;
    @Getter private final GqlvTopLevelQueryStructure topLevelQueryStructure;

    private String getLogicalTypeName() {
        return serviceSpec.getLogicalTypeName();
    }

    public String getLogicalTypeNameSanitized() {
        return _LTN.sanitized(serviceSpec);
    }

    private final GraphQLObjectType.Builder queryBuilder;

    private GraphQLObjectType.Builder gqlObjectTypeBuilder;

    private GraphQLObjectType gqlObjectType;

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

    /**
     * @see #getGqlObjectType()
     */
    public GraphQLObjectType buildObjectGqlType() {
        if (gqlObjectType != null) {
            throw new IllegalArgumentException(String.format("GqlObjectType has already been built for %s", getLogicalTypeName()));
        }
        return gqlObjectType = gqlObjectTypeBuilder.build();
    }

    /**
     * @see #buildObjectGqlType()
     */
    public GraphQLObjectType getGqlObjectType() {
        if (gqlObjectType == null) {
            throw new IllegalStateException(String.format(
                    "GraphQLObjectType has not yet been built for %s", getLogicalTypeName()));
        }
        return gqlObjectType;
    }
}
