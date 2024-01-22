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
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.graphql.model.registry.GraphQLTypeRegistry;
import org.apache.causeway.viewer.graphql.model.util.TypeNames;

import graphql.schema.FieldCoordinates;

import lombok.Getter;

import graphql.Scalars;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLObjectType;

import lombok.val;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static graphql.schema.GraphQLInputObjectType.newInputObject;
import static graphql.schema.GraphQLNonNull.nonNull;
import static graphql.schema.GraphQLObjectType.newObject;

/**
 * Exposes a domain object (view model or entity) via the GQL viewer.
 */
public class GqlvDomainObject implements GqlvActionHolder, GqlvPropertyHolder, GqlvCollectionHolder, GqlvMutationsHolder, GqlvMetaHolder {

    @Getter private final ObjectSpecification objectSpecification;
    private final GraphQLCodeRegistry.Builder codeRegistryBuilder;

    @Getter private final GqlvMeta meta;
    @Getter private final GqlvMutations mutations;

    private final GraphQLObjectType.Builder gqlObjectTypeBuilder;

    String getLogicalTypeName() {
        return objectSpecification.getLogicalTypeName();
    }
    public String getLogicalTypeNameSanitized() {
        return TypeNames.objectTypeNameFor(objectSpecification);
    }

    private final List<GqlvProperty> properties = new ArrayList<>();
    public List<GqlvProperty> getProperties() {return Collections.unmodifiableList(properties);}

    private final List<GqlvCollection> collections = new ArrayList<>();
    public List<GqlvCollection> getCollections() {return Collections.unmodifiableList(collections);}

    private final List<GqlvAction> safeActions = new ArrayList<>();
    public List<GqlvAction> getSafeActions() {return Collections.unmodifiableList(safeActions);}

    /**
     * Built using {@link #buildGqlObjectType()}
     */
    private GraphQLObjectType gqlObjectType;

    @Getter private final GraphQLInputObjectType gqlInputObjectType;

    public GqlvDomainObject(
            final ObjectSpecification objectSpecification,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder,
            final BookmarkService bookmarkService,
            final ObjectManager objectManager) {
        this.objectSpecification = objectSpecification;
        this.codeRegistryBuilder = codeRegistryBuilder;

        this.gqlObjectTypeBuilder = newObject().name(TypeNames.objectTypeNameFor(objectSpecification));

        this.meta = new GqlvMeta(this, codeRegistryBuilder, bookmarkService, objectManager);
        this.mutations = new GqlvMutations(this, codeRegistryBuilder, bookmarkService, objectManager);


        // input object type
        GraphQLInputObjectType.Builder inputTypeBuilder = newInputObject().name(TypeNames.inputTypeNameFor(objectSpecification));
        inputTypeBuilder
                .field(newInputObjectField()
                        .name("id")
                        .type(nonNull(Scalars.GraphQLID))
                        .build());
        gqlInputObjectType = inputTypeBuilder.build();
    }


    public void addPropertiesAsFields() {
        objectSpecification.streamProperties(MixedIn.INCLUDED).forEach(this::addPropertyAsField);
    }


    private void addPropertyAsField(final OneToOneAssociation otoa) {
        GqlvProperty property = new GqlvProperty(this, otoa, codeRegistryBuilder);
        if (property.hasFieldDefinition()) {
            properties.add(property);
        }
    }


    public void addCollectionsAsLists() {
        objectSpecification.streamCollections(MixedIn.INCLUDED).forEach(this::addCollection);
    }

    private void addCollection(OneToManyAssociation otom) {

        GqlvCollection gqlvCollection = new GqlvCollection(this, otom, codeRegistryBuilder);
        if (gqlvCollection.hasFieldDefinition()) {
            collections.add(gqlvCollection);
        }

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
            safeActions.add(new GqlvAction(this, objectAction, codeRegistryBuilder));
        } else {
            mutations.addAction(objectAction);
        }
    }


    /**
     * Should be called only after fields etc have been added.
     */
    private GraphQLObjectType buildGqlObjectType() {
        if (gqlObjectType != null) {
            throw new IllegalArgumentException(String.format("GqlObjectType has already been built for %s", getLogicalTypeName()));
        }
        return gqlObjectType = gqlObjectTypeBuilder.name(getLogicalTypeNameSanitized()).build();
    }

    /**
     * @see #buildMutationsTypeAndFieldIfRequired()
     */
    public Optional<GraphQLObjectType> getMutationsTypeIfAny() {
        return mutations.getMutationsTypeIfAny();
    }

    /**
     * @see #getMutationsTypeIfAny()
     */
    public Optional<GraphQLObjectType> buildMutationsTypeAndFieldIfRequired() {
        return mutations.buildMutationsTypeAndFieldIfRequired();
    }

    @Override
    public void addField(GraphQLFieldDefinition fieldDefinition) {
        gqlObjectTypeBuilder.field(fieldDefinition);
    }


    @Override
    public FieldCoordinates coordinatesFor(final GraphQLFieldDefinition fieldDefinition) {
        if (gqlObjectType == null) {
            throw new IllegalStateException(String.format(
                    "GraphQLObjectType has not yet been built for %s", getLogicalTypeName()));
        }
        return FieldCoordinates.coordinates(gqlObjectType, fieldDefinition);
    }

    public void addDataFetchersForMeta() {
        meta.addDataFetchers();
    }

    public void addDataFetchersForProperties() {
        getProperties().forEach(GqlvAssociation::addDataFetcher);
    }

    public void addDataFetchersForCollections() {
        getCollections().forEach(GqlvCollection::addDataFetcher);
    }

    public void addDataFetchersForSafeActions() {
        getSafeActions().forEach(GqlvAction::addDataFetcher);
    }

    public void addDataFetchersForMutations() {
        getMutations().addDataFetchers();
    }


    public void registerTypesInto(GraphQLTypeRegistry graphQLTypeRegistry) {

        GraphQLObjectType graphQLObjectType = buildGqlObjectType();
        graphQLTypeRegistry.addTypeIfNotAlreadyPresent(graphQLObjectType);

        graphQLTypeRegistry.addTypeIfNotAlreadyPresent(getMeta().getMetaField().getType());
        graphQLTypeRegistry.addTypeIfNotAlreadyPresent(getGqlInputObjectType());

        getMutationsTypeIfAny().ifPresent(graphQLTypeRegistry::addTypeIfNotAlreadyPresent);
    }

    @Override
    public String toString() {
        return objectSpecification.getLogicalTypeName();
    }

}
