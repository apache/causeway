package org.apache.causeway.viewer.graphql.model.domain;

import graphql.schema.GraphQLFieldDefinition;

import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

public class GqlvProperty extends GqlvAssociation<OneToOneAssociation> {

    public GqlvProperty(
            final OneToOneAssociation oneToOneAssociation,
            final GraphQLFieldDefinition fieldDefinition) {
        super(oneToOneAssociation, fieldDefinition);
    }

    public OneToOneAssociation getOneToOneAssociation() {
        return getObjectAssociation();
    }
}
