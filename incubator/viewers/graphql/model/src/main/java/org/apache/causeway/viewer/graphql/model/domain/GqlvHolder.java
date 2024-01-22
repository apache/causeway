package org.apache.causeway.viewer.graphql.model.domain;

import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLFieldDefinition;

public interface GqlvHolder {

    FieldCoordinates coordinatesFor(GraphQLFieldDefinition fieldDefinition);
}
