package org.apache.causeway.viewer.graphql.viewer.toplevel;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLObjectType;

import static graphql.schema.GraphQLObjectType.newObject;

import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyActionParameter;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneActionParameter;
import org.apache.causeway.viewer.graphql.applib.types.TypeMapper;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.GqlvAction;
import org.apache.causeway.viewer.graphql.model.domain.GqlvActionInvokeMutating;
import org.apache.causeway.viewer.graphql.model.domain.GqlvHolder;

import lombok.Getter;
import lombok.val;

public class GqlvTopLevelMutation implements GqlvHolder {

    private final Context context;

    @Getter final GraphQLObjectType.Builder gqlObjectTypeBuilder;


    /**
     * Built using {@link #buildMutationType()}
     */
    private GraphQLObjectType gqlObjectType;

    private final Map<String, GqlvActionInvokeMutating> actions = new LinkedHashMap<String, GqlvActionInvokeMutating>();

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

//    public void addFieldFor(
//            final GqlvDomainService domainService,
//            final GraphQLCodeRegistry.Builder codeRegistryBuilder) {
//
//        GraphQLFieldDefinition topLevelQueryField = domainService.createTopLevelQueryField();
//        gqlObjectBuilder.field(topLevelQueryField);
//
//        codeRegistryBuilder.dataFetcher(
//                // TODO: it would be nice to make these typesafe...
//                FieldCoordinates.coordinates("Mutation", topLevelQueryField.getName()),
//                (DataFetcher<Object>) environment -> domainService.getServicePojo());
//
//    }


    public void addAction(ObjectSpecification objectSpec, final ObjectAction objectAction) {
        // TODO: kinda ugly the responsibilities here
        val holder = new GqlvActionInvokeMutatingHolder(this, objectSpec, objectAction, context);
        actions.put(objectAction.getId(), new GqlvActionInvokeMutating(holder, context));
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

    public void addFetchers() {

    }
}

class GqlvActionInvokeMutatingHolder implements GqlvActionInvokeMutating.Holder {

    private final GqlvTopLevelMutation gqlvTopLevelMutation;
    private final ObjectSpecification objectSpec;
    private final ObjectAction objectAction;
    private final Context context;

    public GqlvActionInvokeMutatingHolder(
            final GqlvTopLevelMutation gqlvTopLevelMutation,
            final ObjectSpecification objectSpec,
            final ObjectAction objectAction,
            final Context context) {
        this.objectSpec = objectSpec;
        this.objectAction = objectAction;
        this.gqlvTopLevelMutation = gqlvTopLevelMutation;
        this.context = context;
    }

    @Override public ObjectAction getObjectAction() {return objectAction;}
    @Override public ObjectAction getObjectMember() {return objectAction;}
    @Override public ObjectSpecification getObjectSpecification() {return objectSpec;}

    // TODO: adapted from GqlvAction
    @Override
    public void addGqlArguments(
            final GraphQLFieldDefinition.Builder fieldBuilder,
            final TypeMapper.InputContext inputContext) {

        // add target (if not a service)
        if (!objectSpec.getBeanSort().isManagedBeanContributing()) {
            GraphQLInputType graphQLInputType = context.typeMapper.inputTypeFor(objectSpec);
            fieldBuilder.argument(GraphQLArgument.newArgument()
                    .name("target")
                    .type(graphQLInputType)
                    .build());
        }

        val parameters = objectAction.getParameters();
        val arguments = parameters.stream()
                .map(objectActionParameter -> gqlArgumentFor(objectActionParameter, inputContext))
                .collect(Collectors.toList());
        if (!arguments.isEmpty()) {
            fieldBuilder.arguments(arguments);
        }
    }

    // TODO: copied from GqlvAction
    GraphQLArgument gqlArgumentFor(
            final ObjectActionParameter objectActionParameter,
            final TypeMapper.InputContext inputContext) {
        return objectActionParameter.isPlural()
                ? gqlArgumentFor((OneToManyActionParameter) objectActionParameter, inputContext)
                : gqlArgumentFor((OneToOneActionParameter) objectActionParameter, inputContext);
    }

    // TODO: copied from GqlvAction
    GraphQLArgument gqlArgumentFor(
            final OneToOneActionParameter oneToOneActionParameter,
            final TypeMapper.InputContext inputContext) {
        return GraphQLArgument.newArgument()
                .name(oneToOneActionParameter.getId())
                .type(context.typeMapper.inputTypeFor(oneToOneActionParameter, inputContext))
                .build();
    }

    // TODO: copied from GqlvAction
    GraphQLArgument gqlArgumentFor(
            final OneToManyActionParameter oneToManyActionParameter,
            final TypeMapper.InputContext inputContext) {
        return GraphQLArgument.newArgument()
                .name(oneToManyActionParameter.getId())
                .type(context.typeMapper.inputTypeFor(oneToManyActionParameter, inputContext))
                .build();
    }

    @Override
    public Can<ManagedObject> argumentManagedObjectsFor(
            final DataFetchingEnvironment dataFetchingEnvironment,
            final ObjectAction objectAction,
            final BookmarkService bookmarkService) {
        return GqlvAction.argumentManagedObjectsFor(dataFetchingEnvironment, objectAction, context);
    }

    @Override
    public GraphQLFieldDefinition addField(GraphQLFieldDefinition fieldDefinition) {
        return gqlvTopLevelMutation.addField(fieldDefinition);
    }

    @Override
    public FieldCoordinates coordinatesFor(GraphQLFieldDefinition fieldDefinition) {
        return gqlvTopLevelMutation.coordinatesFor(fieldDefinition);
    }

}
