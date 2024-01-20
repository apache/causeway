package org.apache.causeway.viewer.graphql.model.parts;

import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;

import graphql.schema.GraphQLFieldDefinition;

public class GqlvAction extends GqlvMember<ObjectAction> {

    public GqlvAction(
            final ObjectAction objectAction,
            final GraphQLFieldDefinition fieldDefinition) {
        super(objectAction, fieldDefinition);
    }

    public ObjectAction getObjectAction() {
        return getObjectMember();
    }
}
