package org.apache.causeway.viewer.graphql.model.domain;

import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLFieldDefinition;

public interface GqlvHolder {

    /**
     * Called while building out the structure
     */
    void addField(GraphQLFieldDefinition fieldDefinition);

    /**
     * Called while registering the fetchers.
     */
    FieldCoordinates coordinatesFor(GraphQLFieldDefinition fieldDefinition);
}
