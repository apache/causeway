package org.apache.causeway.viewer.graphql.model.domain;

import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;

public interface GqlvMemberHolder  {

    FieldCoordinates coordinatesFor(GraphQLFieldDefinition fieldDefinition);
}
