package org.apache.causeway.viewer.graphql.viewer.source;

import graphql.schema.DataFetcher;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLCodeRegistry;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;

import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.interactions.managed.ActionInteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

@RequiredArgsConstructor
public class GqlvServiceBehaviour {

    private final GqlvServiceStructure structure;
    final Object service;
    private final SpecificationLoader specificationLoader;
    private final GraphQLCodeRegistry.Builder codeRegistryBuilder;


    public void addDataFetcher(
            Map.Entry<ObjectAction, GraphQLFieldDefinition> entry) {

        final ObjectAction objectAction = entry.getKey();
        GraphQLFieldDefinition fieldDefinition = entry.getValue();

        final GraphQLObjectType graphQLObjectType = structure.getGqlObjectType();

        codeRegistryBuilder.dataFetcher(
                FieldCoordinates.coordinates(graphQLObjectType, fieldDefinition),
                (DataFetcher<Object>) dataFetchingEnvironment -> {

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
                });
    }
}
