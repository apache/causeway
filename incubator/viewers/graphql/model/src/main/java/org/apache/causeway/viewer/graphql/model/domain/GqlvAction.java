package org.apache.causeway.viewer.graphql.model.domain;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.interactions.managed.ActionInteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;

import lombok.val;

import java.util.Map;
import java.util.stream.Collectors;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLNonNull.nonNull;

public class GqlvAction extends GqlvMember<ObjectAction, GqlvActionHolder> {

    private final GraphQLObjectType.Builder objectTypeBuilder;

    public GqlvAction(
            final GqlvActionHolder holder,
            final ObjectAction objectAction,
            final GraphQLObjectType.Builder objectTypeBuilder,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder
            ) {
        super(holder, objectAction, fieldDefinition(objectAction, objectTypeBuilder), codeRegistryBuilder);
        this.objectTypeBuilder = objectTypeBuilder;
    }

    private static GraphQLFieldDefinition fieldDefinition(
            final ObjectAction objectAction,
            final GraphQLObjectType.Builder objectTypeBuilder) {
        val fieldName = objectAction.getId();
        GraphQLFieldDefinition.Builder fieldBuilder = newFieldDefinition()
                .name(fieldName)
                .type((GraphQLOutputType) TypeMapper.typeForObjectAction(objectAction));
        addGqlArguments(objectAction, fieldBuilder);
        GraphQLFieldDefinition fieldDefinition = fieldBuilder.build();

        objectTypeBuilder.field(fieldDefinition);
        return fieldDefinition;
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

    static void addGqlArguments(
            final ObjectAction objectAction,
            final GraphQLFieldDefinition.Builder builder) {

        Can<ObjectActionParameter> parameters = objectAction.getParameters();

        if (parameters.isNotEmpty()) {
            builder.arguments(parameters.stream()
                    .map(GqlvAction::gqlArgumentFor)
                    .collect(Collectors.toList()));
        }
    }

    private static GraphQLArgument gqlArgumentFor(final ObjectActionParameter objectActionParameter) {
        return GraphQLArgument.newArgument()
                .name(objectActionParameter.getId())
                .type(objectActionParameter.isOptional()
                        ? TypeMapper.inputTypeFor(objectActionParameter)
                        : nonNull(TypeMapper.inputTypeFor(objectActionParameter)))
                .build();
    }


}
