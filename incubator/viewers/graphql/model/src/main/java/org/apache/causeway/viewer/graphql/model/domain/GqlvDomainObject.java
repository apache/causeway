package org.apache.causeway.viewer.graphql.model.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
import org.apache.causeway.core.metamodel.spec.ActionScope;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.graphql.model.registry.GraphQLTypeRegistry;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;
import org.apache.causeway.viewer.graphql.model.types._Constants;
import org.apache.causeway.viewer.graphql.model.util._LTN;

import static org.apache.causeway.viewer.graphql.model.types._Constants.GQL_INPUTTYPE_PREFIX;

import lombok.Getter;

import graphql.Scalars;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static graphql.schema.GraphQLInputObjectType.newInputObject;
import static graphql.schema.GraphQLNonNull.nonNull;
import static graphql.schema.GraphQLObjectType.newObject;
import static graphql.schema.GraphQLTypeReference.typeRef;

/**
 * A wrapper around {@link ObjectSpecification}
 */
public class GqlvDomainObject implements GqlvActionHolder, GqlvPropertyHolder, GqlvCollectionHolder, GqlvMutatorsHolder {

    @Getter private final ObjectSpecification objectSpecification;
    private final GraphQLCodeRegistry.Builder codeRegistryBuilder;

    @Getter private final GqlvMeta meta;
    @Getter private final GqlvMutators mutators;

    @Getter private final GraphQLObjectType.Builder objectTypeBuilder;
    @Getter private final GraphQLInputObjectType gqlInputObjectType;

    String getLogicalTypeName() {
        return objectSpecification.getLogicalTypeName();
    }

    public String getLogicalTypeNameSanitized() {
        return _LTN.sanitized(objectSpecification);
    }

    public BeanSort getBeanSort() {
        return objectSpecification.getBeanSort();
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



    public GqlvDomainObject(
            final ObjectSpecification objectSpecification,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder,
            final BookmarkService bookmarkService,
            final ObjectManager objectManager) {
        this.objectSpecification = objectSpecification;
        this.codeRegistryBuilder = codeRegistryBuilder;
        this.objectTypeBuilder = newObject().name(getLogicalTypeNameSanitized());

        meta = new GqlvMeta(this, codeRegistryBuilder, bookmarkService, objectManager);
        mutators = new GqlvMutators(this, codeRegistryBuilder);

        // input object type
        String inputTypeName = GQL_INPUTTYPE_PREFIX + getLogicalTypeNameSanitized();
        GraphQLInputObjectType.Builder inputTypeBuilder = newInputObject().name(inputTypeName);
        inputTypeBuilder
                .field(newInputObjectField()
                        .name("id")
                        .type(nonNull(Scalars.GraphQLID))
                        .build());
        gqlInputObjectType = inputTypeBuilder.build();

        // object type's meta field
        objectTypeBuilder.field(meta.getMetaField());

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

                GraphQLTypeReference fieldTypeRef = typeRef(_LTN.sanitized(otoaObjectSpec));
                fieldDefinition = newFieldDefinition()
                        .name(otoa.getId())
                        .type(otoa.isOptional() ? fieldTypeRef : nonNull(fieldTypeRef)).build();
                getObjectTypeBuilder().field(
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
                getObjectTypeBuilder().field(fieldDefinition);

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
                GraphQLTypeReference typeRef = typeRef(_LTN.sanitized(elementType));
                fieldDefinition = newFieldDefinition()
                        .name(otom.getId())
                        .type(GraphQLList.list(typeRef)).build();
                objectTypeBuilder.field(fieldDefinition);
                break;

            case VALUE:
                GraphQLType wrappedType = TypeMapper.typeFor(elementType.getCorrespondingClass());
                fieldDefinition = newFieldDefinition()
                        .name(otom.getId())
                        .type(GraphQLList.list(wrappedType)).build();
                objectTypeBuilder.field(fieldDefinition);
                break;
        }

        if (fieldDefinition != null) {
            collections.add(new GqlvCollection(this, otom, fieldDefinition, codeRegistryBuilder));
        }
    }



    void addAction(final ObjectAction objectAction) {

        if (objectAction.getSemantics().isSafeInNature()) {
            safeActions.add(new GqlvAction(this, objectAction, objectTypeBuilder, codeRegistryBuilder));
        } else {
            mutators.addAction(objectAction);
        }
    }


    boolean hasMutators() {
        return mutators.hasActions();
    }


    /**
     * Should be called only after fields etc have been added.
     *
     * @see #getGqlObjectType()
     */
    private GraphQLObjectType buildGqlObjectType() {
        if (gqlObjectType != null) {
            throw new IllegalArgumentException(String.format("GqlObjectType has already been built for %s", getLogicalTypeName()));
        }
        return gqlObjectType = getObjectTypeBuilder().name(getLogicalTypeNameSanitized()).build();
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
     * @see #buildMutatorsTypeIfAny()
     */
    public Optional<GraphQLObjectType> getMutatorsTypeIfAny() {
        return mutators.getMutatorsTypeIfAny();
    }

    /**
     * @see #getMutatorsTypeIfAny()
     */
    public Optional<GraphQLObjectType> buildMutatorsTypeIfAny() {
        return mutators.buildMutatorsTypeIfAny();
    }

    public void addActions() {

        objectSpecification.streamActions(ActionScope.PRODUCTION, MixedIn.INCLUDED)
                .forEach(this::addAction);

        Optional<GraphQLObjectType> mutatorsTypeIfAny = buildMutatorsTypeIfAny();
        mutatorsTypeIfAny.ifPresent(mutatorsType -> {

            GraphQLFieldDefinition gql_mutations = newFieldDefinition()
                    .name(_Constants.GQL_MUTATIONS_FIELDNAME)
                    .type(mutatorsType)
                    .build();
            getObjectTypeBuilder().field(gql_mutations);

        });

    }

    public void addDataFetchersForMetaData() {
        meta.addDataFetchers();
    }

    public void addDataFetchersForProperties() {
        getProperties().forEach(GqlvAssociation::addDataFetcher);
    }

    public void addDataFetchersForCollections() {
        getCollections().forEach(GqlvCollection::addDataFetcher);
    }


    public void addDataFetchersForMutators() {

        // TODO: something similar to addFetchers for safe actions, but applied to GqlvMutators instead; perhaps they share a common interface?

        // earlier code...

//            codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(graphQLTypeReference, gql_mutations), new DataFetcher<Object>() {
//                @Override
//                public Object get(DataFetchingEnvironment environment) throws Exception {
//
//                    Bookmark bookmark = bookmarkService.bookmarkFor(environment.getSource()).orElse(null);
//                    if (bookmark == null) return null; //TODO: is this correct ?
//                    return new GqlvMutations(bookmark, bookmarkService, mutatorsTypeFields);
//                }
//            });
//
//            // for each field something like
//            codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(mutatorsType, idField), new DataFetcher<Object>() {
//                @Override
//                public Object get(DataFetchingEnvironment environment) throws Exception {
//
//                    GqlvMeta gqlMeta = environment.getSource();
//
//                    return gqlMeta.id();
//                }
//            });


    }


    GraphQLObjectType createAndRegisterMutatorsType(
            final Set<GraphQLType> graphQLObjectTypes) {

        //TODO: this is not going to work, because we need to dynamically add fields
        String mutatorsTypeName = getLogicalTypeNameSanitized() + "__DomainObject_mutators";
        GraphQLObjectType.Builder mutatorsTypeBuilder = newObject().name(mutatorsTypeName);
        GraphQLObjectType mutatorsType = mutatorsTypeBuilder.build();
        graphQLObjectTypes.add(mutatorsType);
        return mutatorsType;
    }

    public void registerTypesInto(GraphQLTypeRegistry graphQLTypeRegistry) {

        GraphQLObjectType graphQLObjectType = buildGqlObjectType();
        graphQLTypeRegistry.addTypeIfNotAlreadyPresent(graphQLObjectType);

        graphQLTypeRegistry.addTypeIfNotAlreadyPresent(getMeta().getMetaField().getType());
        graphQLTypeRegistry.addTypeIfNotAlreadyPresent(getGqlInputObjectType());

        getMutatorsTypeIfAny().ifPresent(graphQLTypeRegistry::addTypeIfNotAlreadyPresent);
    }

}
