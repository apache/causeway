package org.apache.causeway.viewer.graphql.model.domain;

import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;

import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;

public class GqlvAction extends GqlvMember<ObjectAction, GqlvActionHolder> {

    public GqlvAction(
            final GqlvActionHolder holder,
            final ObjectAction objectAction,
            final GraphQLFieldDefinition fieldDefinition,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder
    ) {
        super(holder, objectAction, fieldDefinition, codeRegistryBuilder);
    }

    public ObjectAction getObjectAction() {
        return getObjectMember();
    }
}
