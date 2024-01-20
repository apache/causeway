package org.apache.causeway.viewer.graphql.model.domain;

import graphql.schema.GraphQLObjectType;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

public interface GqlvMutatorsHolder extends GqlvActionHolder {
    ObjectSpecification getObjectSpecification();
}
