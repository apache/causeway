package org.apache.causeway.viewer.graphql.model.domain;

import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.Getter;

import graphql.schema.GraphQLFieldDefinition;

public abstract class GqlvMember<T extends ObjectMember> {

    @Getter final T objectMember;
    @Getter final GraphQLFieldDefinition fieldDefinition;

    public GqlvMember(
            final T objectMember,
            final GraphQLFieldDefinition fieldDefinition) {
        this.objectMember = objectMember;
        this.fieldDefinition = fieldDefinition;
    }

    public String getId() {
        return objectMember.getFeatureIdentifier().getFullIdentityString();
    }

}
