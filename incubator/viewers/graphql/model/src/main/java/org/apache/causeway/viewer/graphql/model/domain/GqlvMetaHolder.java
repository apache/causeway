package org.apache.causeway.viewer.graphql.model.domain;

import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLFieldDefinition;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

public interface GqlvMetaHolder {
    ObjectSpecification getObjectSpecification();

    FieldCoordinates coordinatesFor(GraphQLFieldDefinition metaField);

    void addMetaField(GraphQLFieldDefinition metaField);
}
