package org.apache.causeway.viewer.graphql.viewer.source;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;

import graphql.schema.GraphQLOutputType;

import lombok.Getter;
import lombok.val;

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
import org.apache.causeway.viewer.graphql.viewer.util._BiMap;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

public class GqlvServiceStructure {

    @Getter private final ObjectSpecification serviceSpec;
    @Getter private final GqlvTopLevelQueryStructure topLevelQueryStructure;
    private final SpecificationLoader specificationLoader;

    private GraphQLFieldDefinition topLevelQueryField;


    private String getLogicalTypeName() {
        return serviceSpec.getLogicalTypeName();
    }

    public String getLogicalTypeNameSanitized() {
        return _LTN.sanitized(serviceSpec);
    }

    private final GraphQLObjectType.Builder gqlObjectTypeBuilder;
    private final GraphQLObjectType.Builder queryBuilder;

    private GraphQLObjectType gqlObjectType;

    public GqlvServiceStructure(
            final ObjectSpecification serviceSpec,
            final GqlvTopLevelQueryStructure topLevelQueryStructure,
            final SpecificationLoader specificationLoader
    ) {
        this.serviceSpec = serviceSpec;
        this.topLevelQueryStructure = topLevelQueryStructure;

        this.gqlObjectTypeBuilder = newObject().name(_LTN.sanitized(serviceSpec));

        this.queryBuilder = topLevelQueryStructure.getQueryBuilder();
        this.specificationLoader = specificationLoader;
    }


    private final _BiMap<ObjectAction, GraphQLFieldDefinition> safeActionToField = new _BiMap<>();
    private final _BiMap<ObjectAction, GraphQLFieldDefinition> mutatorActionToField = new _BiMap<>();


    Map<ObjectAction, GraphQLFieldDefinition> getSafeActions() {
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

    /**
     * @see #getTopLevelQueryField()
     */
    public GraphQLFieldDefinition addTopLevelQueryField() {
        if (topLevelQueryField != null) {
            throw new IllegalStateException(String.format(
                    "queryField has already been added to top-level Query, for %s", getLogicalTypeName()));
        }
        topLevelQueryField = newFieldDefinition()
                .name(_LTN.sanitized(serviceSpec))
                .type(gqlObjectTypeBuilder)
                .build();
        queryBuilder.field(topLevelQueryField);
        return topLevelQueryField;
    }

    /**
     * @see #addTopLevelQueryField()
     */
    public GraphQLFieldDefinition getTopLevelQueryField() {
        if (topLevelQueryField == null) {
            throw new IllegalStateException(String.format(
                    "queryField has not yet been added to top-level Query, for %s", getLogicalTypeName()));
        }
        return topLevelQueryField;
    }


    void addAction(final ObjectAction objectAction) {

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
        gqlObjectTypeBuilder.field(fieldBuilder.build());
    }


    void addBehaviour(
            final ObjectAction objectAction,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder) {

        final GraphQLObjectType graphQLObjectType = getGqlObjectType();

        String fieldName = objectAction.getId();
        codeRegistryBuilder.dataFetcher(
            FieldCoordinates.coordinates(graphQLObjectType, fieldName),
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
