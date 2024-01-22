package org.apache.causeway.viewer.graphql.model.domain;

import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

/**
 * A holder of <code>_gql_mutations</code> field.
 */
public interface GqlvMutationsHolder {
    ObjectSpecification getObjectSpecification();

    void addMutationsField(GraphQLFieldDefinition mutationsField);

    GraphQLObjectType getGqlObjectType();

    FieldCoordinates coordinatesFor(GraphQLFieldDefinition fieldDefinition);
}
