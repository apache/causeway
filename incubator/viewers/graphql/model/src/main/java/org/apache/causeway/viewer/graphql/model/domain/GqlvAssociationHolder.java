package org.apache.causeway.viewer.graphql.model.domain;

import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLFieldDefinition;

public interface GqlvAssociationHolder extends GqlvMemberHolder {
    FieldCoordinates coordinatesFor(GraphQLFieldDefinition fieldDefinition);
}
