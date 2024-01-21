package org.apache.causeway.viewer.graphql.model.domain;

import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLObjectType;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.viewer.graphql.model.util.TypeNames;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

public class GqlvMutations implements GqlvActionHolder {

    private final GqlvMutationsHolder holder;
    private final GraphQLCodeRegistry.Builder codeRegistryBuilder;
    private final BookmarkService bookmarkService;
    private final ObjectManager objectManager;

    /**
     * Used to build {@link #mutationsTypeIfAny}.
     */
    @Getter final GraphQLObjectType.Builder gqlObjectTypeBuilder;

    /**
     * Built lazily using {@link #buildMutationsTypeIfAny()}
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<GraphQLObjectType> mutationsTypeIfAny;

    public GqlvMutations(
            final GqlvMutationsHolder holder,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder,
            final BookmarkService bookmarkService,
            final ObjectManager objectManager
    ) {
        this.holder = holder;
        this.codeRegistryBuilder = codeRegistryBuilder;
        this.bookmarkService = bookmarkService;
        this.objectManager = objectManager;

        gqlObjectTypeBuilder = newObject().name(TypeNames.mutationsTypeNameFor(this.holder.getObjectSpecification()));
    }

    public void addAction(final ObjectAction objectAction) {
        actions.add(new GqlvAction(this, objectAction, gqlObjectTypeBuilder, codeRegistryBuilder));
    }

    private final List<GqlvAction> actions = new ArrayList<>();
    public List<GqlvAction> getActions() {return Collections.unmodifiableList(actions);}

    boolean hasActions() {
        return !actions.isEmpty();
    }


    /**
     * @see #buildMutationsTypeIfAny()
     */
    public Optional<GraphQLObjectType> getMutationsTypeIfAny() {
        //noinspection OptionalAssignedToNull
        if (mutationsTypeIfAny == null) {
            throw new IllegalArgumentException(String.format("Gql MutatorsType has not yet been built for %s", holder.getObjectSpecification().getLogicalTypeName()));
        }
        return mutationsTypeIfAny;
    }

    /**
     * @see #getMutationsTypeIfAny()
     */
    public Optional<GraphQLObjectType> buildMutationsTypeIfAny() {
        //noinspection OptionalAssignedToNull
        if (mutationsTypeIfAny != null) {
            throw new IllegalArgumentException("Gql MutatorsType has already been built for " + holder.getObjectSpecification().getLogicalTypeName());
        }
        return mutationsTypeIfAny = hasActions()
                ? Optional.of(gqlObjectTypeBuilder.build())
                : Optional.empty();
    }

    @Override
    public GraphQLObjectType getGqlObjectType() {
        return mutationsTypeIfAny.orElse(null);
    }

    public void addDataFetchersForActions() {
        // TODO
    }
}
