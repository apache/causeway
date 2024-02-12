package org.apache.causeway.viewer.graphql.model.toplevel;

import java.util.ArrayList;
import java.util.List;

import graphql.schema.GraphQLObjectType;

import static graphql.schema.GraphQLObjectType.newObject;

import org.apache.causeway.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.GqlvAbstractCustom;
import org.apache.causeway.viewer.graphql.model.domain.GqlvMutationForAction;
import org.apache.causeway.viewer.graphql.model.domain.GqlvMutationForProperty;
import org.apache.causeway.viewer.graphql.model.domain.Parent;

import lombok.val;

public class GqlvTopLevelMutation
                extends GqlvAbstractCustom
                implements Parent {

    private final List<GqlvMutationForAction> actions = new ArrayList<>();
    private final List<GqlvMutationForProperty> properties = new ArrayList<>();

    public GqlvTopLevelMutation(final Context context) {
        super("Mutation", context);

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

        buildObjectType();
    }

    public void addAction(ObjectSpecification objectSpec, final ObjectAction objectAction) {
        val gqlvMutationForAction = new GqlvMutationForAction(objectSpec, objectAction, context);
        addChildField(gqlvMutationForAction.getField());
        actions.add(gqlvMutationForAction);
    }

    public void addProperty(ObjectSpecification objectSpec, final OneToOneAssociation property) {
        val gqlvMutationForProperty = new GqlvMutationForProperty(objectSpec, property, context);
        addChildField(gqlvMutationForProperty.getField());
        properties.add(gqlvMutationForProperty);
    }


    @Override
    public GraphQLObjectType getGqlObjectType() {
        return super.getGqlObjectType();
    }

    public void addDataFetchers() {
        actions.forEach(gqlvMutationForAction -> gqlvMutationForAction.addDataFetcher(this));
        properties.forEach(gqlvMutationForProperty -> gqlvMutationForProperty.addDataFetcher(this));
    }


}