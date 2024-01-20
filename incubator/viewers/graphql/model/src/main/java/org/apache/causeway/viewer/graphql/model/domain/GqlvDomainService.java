package org.apache.causeway.viewer.graphql.model.domain;

import graphql.schema.DataFetcher;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;

import graphql.schema.GraphQLOutputType;

import lombok.Getter;

import java.util.Map;
import java.util.stream.Collectors;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.interactions.managed.ActionInteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.viewer.graphql.model.util._LTN;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;
import org.apache.causeway.viewer.graphql.model.util._BiMap;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

public class GqlvDomainService {

    private final ObjectSpecification serviceSpec;
    @Getter private final Object pojo;
    private final GraphQLCodeRegistry.Builder codeRegistryBuilder;
    private final SpecificationLoader specificationLoader;


    private String getLogicalTypeName() {
        return serviceSpec.getLogicalTypeName();
    }

    public String getLogicalTypeNameSanitized() {
        return _LTN.sanitized(serviceSpec);
    }

    private final GraphQLObjectType.Builder gqlObjectTypeBuilder;

    private GraphQLObjectType gqlObjectType;

    public GqlvDomainService(
            final ObjectSpecification serviceSpec,
            final Object pojo,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder,
            final SpecificationLoader specificationLoader
    ) {
        this.serviceSpec = serviceSpec;
        this.pojo = pojo;
        this.codeRegistryBuilder = codeRegistryBuilder;
        this.specificationLoader = specificationLoader;

        this.gqlObjectTypeBuilder = newObject().name(_LTN.sanitized(serviceSpec));
    }


    private final _BiMap<ObjectAction, GraphQLFieldDefinition> safeActionToField = new _BiMap<>();
    private final _BiMap<ObjectAction, GraphQLFieldDefinition> mutatorActionToField = new _BiMap<>();

    public Map<ObjectAction, GraphQLFieldDefinition> getSafeActions() {
        return safeActionToField.getForwardMapAsImmutable();
    }

    Map<ObjectAction, GraphQLFieldDefinition> getMutatorActions() {
        return mutatorActionToField.getForwardMapAsImmutable();
    }


    /**
     * @see #getGqlObjectType()
     */
    public GraphQLObjectType buildObjectGqlType() {
        if (gqlObjectType != null) {
            throw new IllegalArgumentException(String.format("GqlObjectType has already been built for %s", getLogicalTypeName()));
        }
        return gqlObjectType = gqlObjectTypeBuilder.build();
    }

    /**
     * @see #buildObjectGqlType()
     */
    public GraphQLObjectType getGqlObjectType() {
        if (gqlObjectType == null) {
            throw new IllegalStateException(String.format(
                    "GraphQLObjectType has not yet been built for %s", getLogicalTypeName()));
        }
        return gqlObjectType;
    }

    public GraphQLFieldDefinition createTopLevelQueryField() {
        return newFieldDefinition()
                .name(_LTN.sanitized(serviceSpec))
                .type(gqlObjectTypeBuilder)
                .build();
    }


    public void addAction(final ObjectAction objectAction) {

        String fieldName = objectAction.getId();

        GraphQLFieldDefinition.Builder fieldBuilder = newFieldDefinition()
                .name(fieldName)
                .type((GraphQLOutputType) TypeMapper.typeForObjectAction(objectAction));
        if (objectAction.getParameters().isNotEmpty()) {
            fieldBuilder.arguments(objectAction.getParameters().stream()
                    .map(objectActionParameter -> GraphQLArgument.newArgument()
                            .name(objectActionParameter.getId())
                            .type(TypeMapper.inputTypeFor(objectActionParameter))
                            .build())
                    .collect(Collectors.toList()));
        }
        GraphQLFieldDefinition fieldDefinition = fieldBuilder.build();
        gqlObjectTypeBuilder.field(fieldDefinition);

        // TODO: either safe or mutator
        safeActionToField.put(objectAction, fieldDefinition);
    }


    public void addDataFetcher(
            Map.Entry<ObjectAction, GraphQLFieldDefinition> entry) {

        final ObjectAction objectAction = entry.getKey();
        GraphQLFieldDefinition fieldDefinition = entry.getValue();

        codeRegistryBuilder.dataFetcher(
                FieldCoordinates.coordinates(getGqlObjectType(), fieldDefinition),
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