package org.apache.causeway.viewer.graphql.model.domain;

import graphql.schema.GraphQLFieldDefinition;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

public interface GqlvMetaHolder extends GqlvHolder {
    ObjectSpecification getObjectSpecification();

}
