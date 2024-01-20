package org.apache.causeway.viewer.graphql.model.domain;

import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;

import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

public class GqlvProperty extends GqlvAssociation<OneToOneAssociation, GqlvPropertyHolder> {

    public GqlvProperty(
            final GqlvPropertyHolder domainObject,
            final OneToOneAssociation oneToOneAssociation,
            final GraphQLFieldDefinition fieldDefinition,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder
    ) {
        super(domainObject, oneToOneAssociation, fieldDefinition, codeRegistryBuilder);
    }

    public OneToOneAssociation getOneToOneAssociation() {
        return getObjectAssociation();
    }
}
