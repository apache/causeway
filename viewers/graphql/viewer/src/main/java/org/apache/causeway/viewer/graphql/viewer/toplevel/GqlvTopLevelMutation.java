package org.apache.causeway.viewer.graphql.viewer.toplevel;

import java.util.ArrayList;
import java.util.List;

import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;

import static graphql.schema.GraphQLObjectType.newObject;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.GqlvMutationForAction;
import org.apache.causeway.viewer.graphql.model.domain.GqlvMutationForProperty;

import lombok.Getter;

public class GqlvTopLevelMutation
                implements GqlvMutationForAction.Holder, GqlvMutationForProperty.Holder {

    private final Context context;

    @Getter final GraphQLObjectType.Builder gqlObjectTypeBuilder;


    /**
     * Built using {@link #buildMutationType()}
     */
    private GraphQLObjectType gqlObjectType;

    private final List<GqlvMutationForAction> actions = new ArrayList<>();
    private final List<GqlvMutationForProperty> properties = new ArrayList<>();

    public GqlvTopLevelMutation(final Context context) {
        this.context = context;
        gqlObjectTypeBuilder = newObject().name("Mutation");

    }



    public GraphQLObjectType buildMutationType() {
        if (gqlObjectType != null) {
            throw new IllegalStateException("Mutation type has already been built");
        }
        return gqlObjectType = gqlObjectTypeBuilder.build();
    }

    /**
     *
     * @see #buildMutationType()
     */
    public GraphQLObjectType getGqlObjectType() {
        if (gqlObjectType == null) {
            throw new IllegalStateException("Mutation type has not yet been built");
        }
        return gqlObjectType;
    }


    public void addAction(ObjectSpecification objectSpec, final ObjectAction objectAction) {
        actions.add(new GqlvMutationForAction(this, objectSpec, objectAction, context));
    }

    public void addProperty(ObjectSpecification objectSpec, final OneToOneAssociation property) {
        properties.add(new GqlvMutationForProperty(this, objectSpec, property, context));
    }

    @Override
    public GraphQLFieldDefinition addField(GraphQLFieldDefinition field) {
        gqlObjectTypeBuilder.field(field);
        return field;
    }

    @Override
    public FieldCoordinates coordinatesFor(GraphQLFieldDefinition fieldDefinition) {
        return FieldCoordinates.coordinates(gqlObjectType, fieldDefinition);
    }

    public void addDataFetchers() {
        actions.forEach(GqlvMutationForAction::addDataFetcher);
        properties.forEach(GqlvMutationForProperty::addDataFetcher);
    }


}

