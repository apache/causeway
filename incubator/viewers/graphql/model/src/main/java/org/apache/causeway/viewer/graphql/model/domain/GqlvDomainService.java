package org.apache.causeway.viewer.graphql.model.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.core.metamodel.spec.ActionScope;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.viewer.graphql.model.registry.GraphQLTypeRegistry;
import org.apache.causeway.viewer.graphql.model.util.TypeNames;

import lombok.Getter;
import lombok.val;

import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;

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
    private final BookmarkService bookmarkService;

    private final GraphQLObjectType.Builder gqlObjectTypeBuilder;

    @Getter private final GqlvMutations mutations;

    String getLogicalTypeName() {
        return objectSpecification.getLogicalTypeName();
    }

    private final List<GqlvAction> safeActions = new ArrayList<>();

    private GraphQLObjectType gqlObjectType;

    public GqlvDomainService(
            final ObjectSpecification objectSpecification,
            final Object servicePojo,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder,
            final BookmarkService bookmarkService
    ) {
        this.objectSpecification = objectSpecification;
        this.servicePojo = servicePojo;
        this.codeRegistryBuilder = codeRegistryBuilder;
        this.bookmarkService = bookmarkService;

        this.gqlObjectTypeBuilder = newObject().name(TypeNames.objectTypeNameFor(objectSpecification));

        this.mutations = new GqlvMutations(this, codeRegistryBuilder, bookmarkService);
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

        mutations.buildObjectTypeAndFieldIfRequired();

        return anyActions.get();
    }

    void addAction(final ObjectAction objectAction) {
        if (objectAction.getSemantics().isSafeInNature()) {
            // safeActionSimples.add(new GqlvActionSimple(this, objectAction, codeRegistryBuilder));
            safeActions.add(new GqlvAction(this, objectAction, codeRegistryBuilder, bookmarkService));
        } else {
             mutations.addAction(objectAction);
        }
    }


    @Override
    public void addField(GraphQLFieldDefinition fieldDefinition) {
        gqlObjectTypeBuilder.field(fieldDefinition);
    }


    public void registerTypesInto(GraphQLTypeRegistry graphQLTypeRegistry) {
        gqlObjectType = gqlObjectTypeBuilder.build();
        // TODO: unlike GqlvDomainObject, not sure where gqlObjectType is already registered

        mutations.registerTypesInto(graphQLTypeRegistry);
    }

    public void addDataFetchers() {
        // safeActionSimples.forEach(GqlvActionSimple::addDataFetcher);
        safeActions.forEach(GqlvAction::addDataFetcher);
        getMutations().addDataFetchers();
    }

    @Override
    public FieldCoordinates coordinatesFor(GraphQLFieldDefinition fieldDefinition) {
        if (gqlObjectType == null) {
            throw new IllegalStateException(String.format(
                    "GraphQLObjectType has not yet been built for %s", getLogicalTypeName()));
        }
        return coordinates(gqlObjectType, fieldDefinition);
    }

    public GraphQLFieldDefinition createTopLevelQueryField() {
        return newFieldDefinition()
                .name(TypeNames.objectTypeNameFor(objectSpecification))
                .type(gqlObjectTypeBuilder)
                .build();
    }

    @Override
    public String toString() {
        return objectSpecification.getLogicalTypeName();
    }

}
