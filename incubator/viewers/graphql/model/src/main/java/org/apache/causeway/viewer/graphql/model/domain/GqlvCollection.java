package org.apache.causeway.viewer.graphql.model.domain;

import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;

import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

public class GqlvCollection extends GqlvAssociation<OneToManyAssociation, GqlvCollectionHolder> {

    public GqlvCollection(
            final GqlvCollectionHolder domainObject,
            final OneToManyAssociation oneToManyAssociation,
            final GraphQLFieldDefinition fieldDefinition,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder
    ) {
        super(domainObject, oneToManyAssociation, fieldDefinition, codeRegistryBuilder);
    }

    public OneToManyAssociation getOneToManyAssociation() {
        return getObjectAssociation();
    }

}
