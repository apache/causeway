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
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;
import org.apache.causeway.viewer.graphql.model.util.TypeNames;

import graphql.schema.FieldCoordinates;

import lombok.Getter;

import graphql.Scalars;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;

import lombok.val;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static graphql.schema.GraphQLInputObjectType.newInputObject;
import static graphql.schema.GraphQLNonNull.nonNull;
import static graphql.schema.GraphQLObjectType.newObject;
import static graphql.schema.GraphQLTypeReference.typeRef;

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
        ObjectSpecification otoaObjectSpec = otoa.getElementType();

        GraphQLFieldDefinition fieldDefinition = null;
        switch (otoaObjectSpec.getBeanSort()) {

            case VIEW_MODEL:
            case ENTITY:

                GraphQLTypeReference fieldTypeRef = typeRef(TypeNames.objectTypeNameFor(otoaObjectSpec));
                fieldDefinition = newFieldDefinition()
                        .name(otoa.getId())
                        .type(otoa.isOptional() ? fieldTypeRef : nonNull(fieldTypeRef)).build();
                gqlObjectTypeBuilder.field(
                        fieldDefinition
                        );

                break;

            case VALUE:

                // todo: map ...

                GraphQLFieldDefinition.Builder valueBuilder = newFieldDefinition()
                        .name(otoa.getId())
                        .type(otoa.isOptional()
                                ? Scalars.GraphQLString
                                : nonNull(Scalars.GraphQLString));
                fieldDefinition = valueBuilder.build();
                gqlObjectTypeBuilder.field(fieldDefinition);

                break;
        }
        if (fieldDefinition != null) {
            properties.add(new GqlvProperty(this, otoa, fieldDefinition, codeRegistryBuilder));
        }
    }


    public void addCollectionsAsLists() {
        objectSpecification.streamCollections(MixedIn.INCLUDED).forEach(this::addCollection);
    }

    private void addCollection(OneToManyAssociation otom) {

        ObjectSpecification elementType = otom.getElementType();
        GraphQLFieldDefinition fieldDefinition = null;

        switch (elementType.getBeanSort()) {

            case VIEW_MODEL:
            case ENTITY:
                GraphQLTypeReference typeRef = typeRef(TypeNames.objectTypeNameFor(elementType));
                fieldDefinition = newFieldDefinition()
                        .name(otom.getId())
                        .type(GraphQLList.list(typeRef)).build();
                gqlObjectTypeBuilder.field(fieldDefinition);
                break;

            case VALUE:
                GraphQLType wrappedType = TypeMapper.typeFor(elementType.getCorrespondingClass());
                fieldDefinition = newFieldDefinition()
                        .name(otom.getId())
                        .type(GraphQLList.list(wrappedType)).build();
                gqlObjectTypeBuilder.field(fieldDefinition);
                break;
        }

        if (fieldDefinition != null) {
            collections.add(new GqlvCollection(this, otom, fieldDefinition, codeRegistryBuilder));
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
    public void addMetaField(GraphQLFieldDefinition metaField) {
        gqlObjectTypeBuilder.field(metaField);
    }

    @Override
    public void addMutationsField(GraphQLFieldDefinition mutationsField) {
        gqlObjectTypeBuilder.field(mutationsField);
    }

    @Override
    public FieldCoordinates coordinatesFor(final GraphQLFieldDefinition fieldDefinition) {
        return FieldCoordinates.coordinates(gqlObjectType, fieldDefinition);
    }

    @Override
    public void addActionField(GraphQLFieldDefinition mutationsField) {

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
