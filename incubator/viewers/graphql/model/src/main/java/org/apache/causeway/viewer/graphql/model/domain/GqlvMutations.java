package org.apache.causeway.viewer.graphql.model.domain;

import graphql.schema.DataFetcher;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.viewer.graphql.model.types._Constants;
import org.apache.causeway.viewer.graphql.model.util.TypeNames;

import static graphql.schema.FieldCoordinates.coordinates;
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
    final GraphQLObjectType.Builder gqlObjectTypeBuilder;

    /**
     * Built lazily using {@link #buildMutationsTypeAndFieldIfRequired()}
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<GraphQLObjectType> mutationsTypeIfAny;

    /**
     * Built lazily using {@link #buildMutationsTypeAndFieldIfRequired()}
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<GraphQLFieldDefinition> mutationsFieldIfAny;

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
     * @see #buildMutationsTypeAndFieldIfRequired()
     */
    public Optional<GraphQLObjectType> getMutationsTypeIfAny() {
        //noinspection OptionalAssignedToNull
        if (mutationsTypeIfAny == null) {
            throw new IllegalArgumentException(String.format("Gql mutators type and field has not yet been built for %s", holder.getObjectSpecification().getLogicalTypeName()));
        }
        return mutationsTypeIfAny;
    }

    /**
     * @see #getMutationsTypeIfAny()
     */
    public Optional<GraphQLObjectType> buildMutationsTypeAndFieldIfRequired() {
        //noinspection OptionalAssignedToNull
        if (mutationsTypeIfAny != null) {
            throw new IllegalArgumentException("Gql mutations type and field has already been built for " + holder.getObjectSpecification().getLogicalTypeName());
        }

        if (hasActions()) {

            // create the type
            GraphQLObjectType mutationsType = gqlObjectTypeBuilder.build();
            this.mutationsTypeIfAny = Optional.of(mutationsType);

            // create the field
            GraphQLFieldDefinition mutationsField = newFieldDefinition()
                    .name(_Constants.GQL_MUTATIONS_FIELDNAME)
                    .type(mutationsType)
                    .build();
            mutationsFieldIfAny = Optional.of(mutationsField);

            // register the field into the owning type
            holder.addMutationsField(mutationsField);

        } else {
            mutationsFieldIfAny = Optional.empty();
            mutationsTypeIfAny = Optional.empty();
        }
        return mutationsTypeIfAny;
    }

    public void addDataFetchers() {
        if (mutationsFieldIfAny.isPresent()) {
            codeRegistryBuilder.dataFetcher(
                    holder.coordinatesFor(mutationsFieldIfAny.get()),
                    (DataFetcher<Object>) environment ->
                        bookmarkService.bookmarkFor(environment.getSource())
                            .map(bookmark -> new Fetcher(bookmark, bookmarkService))
                            .orElseThrow());

            getActions().forEach(GqlvAction::addDataFetcher);
        }
    }

    @Override
    public FieldCoordinates coordinatesFor(GraphQLFieldDefinition fieldDefinition) {
        return FieldCoordinates.coordinates(mutationsTypeIfAny.orElse(null), fieldDefinition);
    }

    static class Fetcher {

        private final Bookmark bookmark;
        private final BookmarkService bookmarkService;

        public Fetcher(
                final Bookmark bookmark,
                final BookmarkService bookmarkService) {

            this.bookmark = bookmark;
            this.bookmarkService = bookmarkService;
        }

        public Object getTargetPojo() {
            return bookmarkService.lookup(bookmark).orElseThrow();
        }
    }
}
