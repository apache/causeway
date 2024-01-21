package org.apache.causeway.viewer.graphql.model.domain;

import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLObjectType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.viewer.graphql.model.util.TypeNames;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

public class GqlvMutators implements GqlvActionHolder {

    private final GqlvMutatorsHolder holder;
    private final GraphQLCodeRegistry.Builder codeRegistryBuilder;

    final GraphQLObjectType.Builder objectTypeBuilder;

    /**
     * Built lazily using {@link #buildMutatorsTypeIfAny()}
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<GraphQLObjectType> mutatorsTypeIfAny;

    public GqlvMutators(
            final GqlvMutatorsHolder holder,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder
    ) {
        this.holder = holder;
        this.codeRegistryBuilder = codeRegistryBuilder;

        objectTypeBuilder = newObject().name(TypeNames.mutatorTypeNameFor(this.holder.getObjectSpecification()));

    }

    public void addAction(final ObjectAction objectAction) {

//        val fieldName = objectAction.getId();
//        GraphQLFieldDefinition.Builder fieldBuilder = newFieldDefinition()
//                .name(fieldName)
//                .type((GraphQLOutputType) TypeMapper.typeForObjectAction(objectAction));
//        addGqlArguments(objectAction, fieldBuilder);
//        GraphQLFieldDefinition fieldDefinition = fieldBuilder.build();
//
//        objectTypeBuilder.field(fieldDefinition);
        actions.add(new GqlvAction(holder, objectAction, objectTypeBuilder, codeRegistryBuilder));
    }

    private final List<GqlvAction> actions = new ArrayList<>();
    public List<GqlvAction> getActions() {return Collections.unmodifiableList(actions);}

    boolean hasActions() {
        return !actions.isEmpty();
    }


    /**
     * @see #buildMutatorsTypeIfAny()
     */
    public Optional<GraphQLObjectType> getMutatorsTypeIfAny() {
        //noinspection OptionalAssignedToNull
        if (mutatorsTypeIfAny == null) {
            throw new IllegalArgumentException(String.format("Gql MutatorsType has not yet been built for %s", holder.getObjectSpecification().getLogicalTypeName()));
        }
        return mutatorsTypeIfAny;
    }

    /**
     * @see #getMutatorsTypeIfAny()
     */
    public Optional<GraphQLObjectType> buildMutatorsTypeIfAny() {
        //noinspection OptionalAssignedToNull
        if (mutatorsTypeIfAny != null) {
            throw new IllegalArgumentException("Gql MutatorsType has already been built for " + holder.getObjectSpecification().getLogicalTypeName());
        }
        return mutatorsTypeIfAny = hasActions()
                ? Optional.of(objectTypeBuilder.build())
                : Optional.empty();
    }

    @Override
    public GraphQLObjectType getGqlObjectType() {
        return mutatorsTypeIfAny.orElse(null);
    }
}
