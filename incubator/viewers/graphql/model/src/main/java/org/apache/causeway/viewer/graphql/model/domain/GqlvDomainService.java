package org.apache.causeway.viewer.graphql.model.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
import org.apache.causeway.core.metamodel.spec.ActionScope;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.viewer.graphql.model.registry.GraphQLTypeRegistry;
import org.apache.causeway.viewer.graphql.model.util.TypeNames;

import graphql.schema.FieldCoordinates;

import lombok.Getter;

import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;

import lombok.val;

import static graphql.schema.FieldCoordinates.coordinates;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

/**
 * Exposes a domain service (view model or entity) via the GQL viewer.
 */
public class GqlvDomainService implements GqlvActionHolder, GqlvMutationsHolder {

    @Getter private final ObjectSpecification objectSpecification;
    @Getter private final Object servicePojo;
    private final GraphQLCodeRegistry.Builder codeRegistryBuilder;

    @Getter private final GqlvMutations mutations;
    private final BookmarkService bookmarkService;
    private final ObjectManager objectManager;

    @Getter private final GraphQLObjectType.Builder gqlObjectTypeBuilder;

    String getLogicalTypeName() {
        return objectSpecification.getLogicalTypeName();
    }
    public String getLogicalTypeNameSanitized() {
        return TypeNames.objectTypeNameFor(objectSpecification);
    }


    private final List<GqlvAction> safeActions = new ArrayList<>();
    public List<GqlvAction> getSafeActions() {return Collections.unmodifiableList(safeActions);}

    /**
     * Built using {@link #buildGqlObjectType()}
     */
    private GraphQLObjectType gqlObjectType;

    public GqlvDomainService(
            final ObjectSpecification objectSpecification,
            final Object servicePojo,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder,
            final BookmarkService bookmarkService,
            final ObjectManager objectManager
    ) {
        this.objectSpecification = objectSpecification;
        this.servicePojo = servicePojo;
        this.codeRegistryBuilder = codeRegistryBuilder;

        this.gqlObjectTypeBuilder = newObject().name(TypeNames.objectTypeNameFor(objectSpecification));

        this.mutations = new GqlvMutations(this, codeRegistryBuilder, bookmarkService, objectManager);

        this.bookmarkService = bookmarkService;
        this.objectManager = objectManager;
    }

    /**
     * @return <code>true</code> if any (at least one) actions were added
     */
    public boolean addActions() {

        val anyActions = new AtomicBoolean(false);
        objectSpecification.streamActions(ActionScope.PRODUCTION, MixedIn.INCLUDED)
                .forEach(objectAction -> {
                    anyActions.set(true);
                    addAction(objectAction);
                });

        buildMutationsTypeAndFieldIfRequired();

        return anyActions.get();
    }

    void addAction(final ObjectAction objectAction) {
        if (objectAction.getSemantics().isSafeInNature()) {
            safeActions.add(new GqlvAction(this, objectAction, gqlObjectTypeBuilder, codeRegistryBuilder));
        } else {
            // TODO: still trying to add the action to the mutations
             mutations.addAction(objectAction);

            // for now, we go direct...
//            safeActions.add(new GqlvAction(this, objectAction, gqlObjectTypeBuilder, codeRegistryBuilder));
        }
    }


    /**
     * Should be called only after fields etc have been added.
     *
     * @see #getGqlObjectType()
     */
    public GraphQLObjectType buildGqlObjectType() {
        if (gqlObjectType != null) {
            throw new IllegalArgumentException(String.format("GqlObjectType has already been built for %s", getLogicalTypeName()));
        }
        return gqlObjectType = gqlObjectTypeBuilder.build();
    }

    /**
     * @see #buildGqlObjectType()
     */
    public GraphQLObjectType getGqlObjectType() {
        if (gqlObjectType == null) {
            throw new IllegalStateException(String.format(
                    "GraphQLObjectType has not yet been built for %s", getLogicalTypeName()));
        }
        return gqlObjectType;
    }



    /**
     * @see #buildMutationsTypeAndFieldIfRequired()
     */
    public Optional<GraphQLObjectType> getMutatorsTypeIfAny() {
        return mutations.getMutationsTypeIfAny();
    }

    /**
     * @see #getMutatorsTypeIfAny()
     */
    public Optional<GraphQLObjectType> buildMutationsTypeAndFieldIfRequired() {
        return mutations.buildMutationsTypeAndFieldIfRequired();
    }

    @Override
    public void addMutationsField(GraphQLFieldDefinition mutationsField) {
        gqlObjectTypeBuilder.field(mutationsField);
    }

    @Override
    public FieldCoordinates coordinatesFor(GraphQLFieldDefinition fieldDefinition) {
        return coordinates(getGqlObjectType(), fieldDefinition);
    }

    public GraphQLFieldDefinition createTopLevelQueryField() {
        return newFieldDefinition()
                .name(TypeNames.objectTypeNameFor(objectSpecification))
                .type(gqlObjectTypeBuilder)
                .build();
    }



    public void registerTypesInto(GraphQLTypeRegistry graphQLTypeRegistry) {
        buildGqlObjectType();

        getMutatorsTypeIfAny().ifPresent(graphQLTypeRegistry::addTypeIfNotAlreadyPresent);
    }

    public void addDataFetchersForSafeActions() {
        getSafeActions().forEach(GqlvAction::addDataFetcher);
    }

    public void addDataFetchersForMutations() {
        getMutations().addDataFetchers();
    }

    @Override
    public String toString() {
        return objectSpecification.getLogicalTypeName();
    }

}
