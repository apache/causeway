package org.apache.causeway.viewer.graphql.model.toplevel;

import java.util.ArrayList;
import java.util.List;

import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;

import static graphql.schema.GraphQLObjectType.newObject;

import org.apache.causeway.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.GqlvMutationForAction;
import org.apache.causeway.viewer.graphql.model.domain.GqlvMutationForProperty;

import lombok.Getter;
import lombok.val;

public class GqlvTopLevelMutation
                implements GqlvMutationForAction.Holder, GqlvMutationForProperty.Holder {

    private final Context context;

    private final GraphQLObjectType.Builder gqlObjectTypeBuilder;

    @Getter
    private final GraphQLObjectType objectType;

    private final List<GqlvMutationForAction> actions = new ArrayList<>();
    private final List<GqlvMutationForProperty> properties = new ArrayList<>();

    public GqlvTopLevelMutation(final Context context) {
        this.context = context;
        gqlObjectTypeBuilder = newObject().name("Mutation");

        val objectSpecifications = context.objectSpecifications();

        objectSpecifications.forEach(objectSpec -> {
            objectSpec.streamActions(context.getActionScope(), MixedIn.INCLUDED)
                    .filter(x -> ! x.getSemantics().isSafeInNature())
                    .forEach(objectAction -> addAction(objectSpec, objectAction));
            objectSpec.streamProperties(MixedIn.INCLUDED)
                    .filter(property -> ! property.isAlwaysHidden())
                    .filter(property -> property.containsFacet(PropertySetterFacet.class))
                    .forEach(property -> addProperty(objectSpec, property));

        });

        objectType = gqlObjectTypeBuilder.build();
    }


    public void addAction(ObjectSpecification objectSpec, final ObjectAction objectAction) {
        val gqlvMutationForAction = new GqlvMutationForAction(this, objectSpec, objectAction, context);
        addField(gqlvMutationForAction.getField());
        actions.add(gqlvMutationForAction);
    }

    public void addProperty(ObjectSpecification objectSpec, final OneToOneAssociation property) {
        val gqlvMutationForProperty = new GqlvMutationForProperty(this, objectSpec, property, context);
        addField(gqlvMutationForProperty.getField());
        properties.add(gqlvMutationForProperty);
    }

    private GraphQLFieldDefinition addField(GraphQLFieldDefinition field) {
        if (field != null) {
            gqlObjectTypeBuilder.field(field);
        }
        return field;
    }

    @Override
    public FieldCoordinates coordinatesFor(GraphQLFieldDefinition fieldDefinition) {
        return FieldCoordinates.coordinates(objectType, fieldDefinition);
    }

    public void addDataFetchers() {
        actions.forEach(GqlvMutationForAction::addDataFetcher);
        properties.forEach(GqlvMutationForProperty::addDataFetcher);
    }


}

