package org.apache.causeway.viewer.graphql.model.toplevel;

import java.util.ArrayList;
import java.util.List;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLObjectType;

import org.apache.causeway.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.GqlvAbstractCustom;
import org.apache.causeway.viewer.graphql.model.domain.Parent;
import org.apache.causeway.viewer.graphql.model.domain.rich.GqlvMutationForAction;
import org.apache.causeway.viewer.graphql.model.domain.rich.GqlvMutationForProperty;

import lombok.val;

public class GqlvTopLevelMutation
                extends GqlvAbstractCustom
                implements Parent {

    private final List<GqlvMutationForAction> actions = new ArrayList<>();
    private final List<GqlvMutationForProperty> properties = new ArrayList<>();

    public GqlvTopLevelMutation(final Context context) {
        super("Mutation", context);

        if (isBuilt()) {
            // type already exists, nothing else to do.
            return;
        }
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

    /**
     * Never used.
     *
     * @param environment
     * @return
     */
    @Override
    protected Object fetchData(DataFetchingEnvironment environment) {
        return null;
    }

    public void addAction(ObjectSpecification objectSpec, final ObjectAction objectAction) {
        val gqlvMutationForAction = new GqlvMutationForAction(objectSpec, objectAction, context);
        addChildFieldFor(gqlvMutationForAction);
        actions.add(gqlvMutationForAction);
    }

    public void addProperty(ObjectSpecification objectSpec, final OneToOneAssociation property) {
        val gqlvMutationForProperty = new GqlvMutationForProperty(objectSpec, property, context);
        addChildFieldFor(gqlvMutationForProperty);
        properties.add(gqlvMutationForProperty);
    }


    @Override
    public GraphQLObjectType getGqlObjectType() {
        return super.getGqlObjectType();
    }

    public void addDataFetchers() {
        addDataFetchersForChildren();
    }

    @Override
    protected void addDataFetchersForChildren() {
        actions.forEach(gqlvMutationForAction -> gqlvMutationForAction.addDataFetcher(this));
        properties.forEach(gqlvMutationForProperty -> gqlvMutationForProperty.addDataFetcher(this));
    }


}
