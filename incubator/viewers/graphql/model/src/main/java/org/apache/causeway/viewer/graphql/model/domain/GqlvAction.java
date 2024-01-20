package org.apache.causeway.viewer.graphql.model.domain;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.interactions.managed.ActionInteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;

import java.util.Map;

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

    public void addDataFetcher() {

        codeRegistryBuilder.dataFetcher(
                FieldCoordinates.coordinates(getHolder().getGqlObjectType(), getFieldDefinition()),
                this::invoke
        );
    }

    private Object invoke(
            final DataFetchingEnvironment dataFetchingEnvironment) {
        final ObjectAction objectAction = getObjectAction();

        Object domainObjectInstance = dataFetchingEnvironment.getSource();

        Class<?> domainObjectInstanceClass = domainObjectInstance.getClass();
        ObjectSpecification specification = specificationLoader
                .loadSpecification(domainObjectInstanceClass);

        ManagedObject owner = ManagedObject.adaptSingular(specification, domainObjectInstance);

        ActionInteractionHead actionInteractionHead = objectAction.interactionHead(owner);

        Map<String, Object> arguments = dataFetchingEnvironment.getArguments();
        Can<ObjectActionParameter> parameters = objectAction.getParameters();
        Can<ManagedObject> canOfParams = parameters
                .map(oap -> {
                    Object argumentValue = arguments.get(oap.getId());
                    return ManagedObject.adaptParameter(oap, argumentValue);
                });

        ManagedObject managedObject = objectAction
                .execute(actionInteractionHead, canOfParams, InteractionInitiatedBy.USER);

        return managedObject.getPojo();
    }

}
