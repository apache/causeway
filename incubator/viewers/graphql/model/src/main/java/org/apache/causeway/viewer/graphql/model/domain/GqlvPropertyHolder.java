package org.apache.causeway.viewer.graphql.model.domain;

import graphql.schema.GraphQLFieldDefinition;

public interface GqlvPropertyHolder extends GqlvAssociationHolder {

    void addPropertyField(GraphQLFieldDefinition fieldDefinition);

}
