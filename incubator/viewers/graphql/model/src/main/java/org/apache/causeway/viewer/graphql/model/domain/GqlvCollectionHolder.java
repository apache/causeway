package org.apache.causeway.viewer.graphql.model.domain;

import graphql.language.FieldDefinition;
import graphql.schema.GraphQLFieldDefinition;

public interface GqlvCollectionHolder extends GqlvAssociationHolder {

    void addCollectionField(GraphQLFieldDefinition fieldDefinition);
}
