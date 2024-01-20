package org.apache.causeway.viewer.graphql.model.domain;

import graphql.schema.GraphQLFieldDefinition;

import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;

public class GqlvCollection extends GqlvAssociation<OneToManyAssociation> {

    public GqlvCollection(
            final OneToManyAssociation oneToManyAssociation,
            final GraphQLFieldDefinition fieldDefinition) {
        super(oneToManyAssociation, fieldDefinition);
    }

    public OneToManyAssociation getOneToManyAssociation() {
        return getObjectAssociation();
    }
}
