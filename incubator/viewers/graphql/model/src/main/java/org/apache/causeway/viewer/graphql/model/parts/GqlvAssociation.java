package org.apache.causeway.viewer.graphql.model.parts;

import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;

import graphql.schema.GraphQLFieldDefinition;

public abstract class GqlvAssociation<T extends ObjectAssociation> extends GqlvMember<T> {

    public GqlvAssociation(
            final T objectAssociation,
            final GraphQLFieldDefinition fieldDefinition) {
        super(objectAssociation, fieldDefinition);
    }

    /**
     * @see #getObjectMember()
     */
    public T getObjectAssociation() {
        return getObjectMember();
    }
}
