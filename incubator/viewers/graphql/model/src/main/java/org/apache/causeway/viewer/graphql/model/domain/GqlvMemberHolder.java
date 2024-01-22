package org.apache.causeway.viewer.graphql.model.domain;

import graphql.schema.GraphQLFieldDefinition;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

public interface GqlvMemberHolder extends GqlvHolder {

    ObjectSpecification getObjectSpecification();

}
