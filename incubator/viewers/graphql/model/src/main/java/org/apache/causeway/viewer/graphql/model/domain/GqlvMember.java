package org.apache.causeway.viewer.graphql.model.domain;

import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

import graphql.schema.GraphQLCodeRegistry;

import lombok.Getter;

import graphql.schema.GraphQLFieldDefinition;

public abstract class GqlvMember<T extends ObjectMember, H extends GqlvMemberHolder> {

    @Getter private final H holder;
    @Getter private final T objectMember;
    @Getter private final GraphQLFieldDefinition fieldDefinition;
    final GraphQLCodeRegistry.Builder codeRegistryBuilder;
    final SpecificationLoader specificationLoader;

    public GqlvMember(
            final H holder,
            final T objectMember,
            final GraphQLFieldDefinition fieldDefinition,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder
    ) {
        this.holder = holder;
        this.objectMember = objectMember;
        this.fieldDefinition = fieldDefinition;
        this.codeRegistryBuilder = codeRegistryBuilder;
        this.specificationLoader = objectMember.getSpecificationLoader();
    }

    public String getId() {
        return objectMember.getFeatureIdentifier().getFullIdentityString();
    }

}
