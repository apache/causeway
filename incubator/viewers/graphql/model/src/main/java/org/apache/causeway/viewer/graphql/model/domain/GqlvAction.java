package org.apache.causeway.viewer.graphql.model.domain;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.causeway.core.metamodel.interactions.managed.ActionInteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.viewer.graphql.model.types.ScalarMapper;
import org.apache.causeway.viewer.graphql.model.util.TypeNames;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import graphql.Scalars;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLNonNull.nonNull;

@Log4j2
public class GqlvAction extends GqlvMember<ObjectAction, GqlvActionHolder> {

    public GqlvAction(
            final GqlvActionHolder holder,
            final ObjectAction objectAction,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder
            ) {
        super(holder, objectAction, fieldDefinition(objectAction, holder), codeRegistryBuilder);
    }

    private static GraphQLFieldDefinition fieldDefinition(
            final ObjectAction objectAction,
            final GqlvActionHolder holder) {

        GraphQLFieldDefinition fieldDefinition = null;
        GraphQLOutputType type = typeFor(objectAction);

        if (type != null) {
            val fieldBuilder = newFieldDefinition()
                    .name(objectAction.getId())
                    .type(type);
            addGqlArguments(objectAction, fieldBuilder);
            fieldDefinition = fieldBuilder.build();

            holder.addField(fieldDefinition);
        }
        return fieldDefinition;
    }

    @Nullable
    private static GraphQLOutputType typeFor(final ObjectAction objectAction){
        ObjectSpecification objectSpecification = objectAction.getReturnType();
        switch (objectSpecification.getBeanSort()){

            case COLLECTION:

                TypeOfFacet facet = objectAction.getFacet(TypeOfFacet.class);
                if (facet == null) {
                    log.warn("Unable to locate TypeOfFacet for {}", objectAction.getFeatureIdentifier().getFullIdentityString());
                    return null;
                }
                ObjectSpecification objectSpecificationForElementWhenCollection = facet.elementSpec();
                GraphQLType wrappedType = outputTypeFor(objectSpecificationForElementWhenCollection);
                if (wrappedType == null) {
                    log.warn("Unable to create wrapped type of for {} for action {}",
                            objectSpecificationForElementWhenCollection.getFullIdentifier(),
                            objectAction.getFeatureIdentifier().getFullIdentityString());
                    return null;
                }
                return GraphQLList.list(wrappedType);

            case VALUE:
            case ENTITY:
            case VIEW_MODEL:
            default:
                // TODO: this cast is suspicious
                return (GraphQLOutputType) outputTypeFor(objectSpecification);

        }
    }

    @Nullable
    private static GraphQLType outputTypeFor(final ObjectSpecification objectSpecification){

        switch (objectSpecification.getBeanSort()){
            case ABSTRACT:
            case ENTITY:
            case VIEW_MODEL:
                return GraphQLTypeReference.typeRef(TypeNames.objectTypeNameFor(objectSpecification));

            case VALUE:
                return ScalarMapper.typeFor(objectSpecification.getCorrespondingClass());

            case COLLECTION:
                // should be noop
                return null;

            default:
                // for now
                return Scalars.GraphQLString;
        }
    }

    public ObjectAction getObjectAction() {
        return getObjectMember();
    }

    public void addDataFetcher() {
        GraphQLFieldDefinition fieldDefinition = getFieldDefinition();
        codeRegistryBuilder.dataFetcher(
                getHolder().coordinatesFor(fieldDefinition),
                this::invoke
        );
    }

    private Object invoke(
            final DataFetchingEnvironment dataFetchingEnvironment) {
        final ObjectAction objectAction = getObjectAction();

        // TODO: not tested
        Object source = dataFetchingEnvironment.getSource();
        Object domainObjectInstance;
        if (source instanceof GqlvMutations.Fetcher) {
            GqlvMutations.Fetcher fetcher = (GqlvMutations.Fetcher) source;
            domainObjectInstance = fetcher.getTargetPojo();
        } else {
            domainObjectInstance = source;
        }

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
                        ? GqlvActionParameter.inputTypeFor(objectActionParameter)
                        : nonNull(GqlvActionParameter.inputTypeFor(objectActionParameter)))
                .build();
    }
}
