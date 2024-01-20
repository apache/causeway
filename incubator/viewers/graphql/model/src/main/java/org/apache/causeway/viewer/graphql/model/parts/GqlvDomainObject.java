package org.apache.causeway.viewer.graphql.model.parts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
import org.apache.causeway.core.metamodel.spec.ActionScope;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.viewer.graphql.model.util._LTN;
import org.apache.causeway.viewer.graphql.model.types.TypeMapper;

import static graphql.schema.FieldCoordinates.coordinates;

import static org.apache.causeway.viewer.graphql.model.parts._Constants.GQL_INPUTTYPE_PREFIX;

import graphql.schema.DataFetcher;
import graphql.schema.GraphQLCodeRegistry;

import lombok.Getter;
import lombok.val;
import lombok.experimental.UtilityClass;

import graphql.Scalars;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
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
public class GqlvDomainObject {

    @UtilityClass
    static class Fields {
        static GraphQLFieldDefinition id =
                newFieldDefinition()
                        .name("id")
                        .type(nonNull(Scalars.GraphQLString))
                        .build();
        static GraphQLFieldDefinition logicalTypeName =
                newFieldDefinition()
                        .name("logicalTypeName")
                        .type(nonNull(Scalars.GraphQLString))
                        .build();
        static GraphQLFieldDefinition version =
                newFieldDefinition()
                        .name("version")
                        .type(Scalars.GraphQLString).build();
    }

    private final ObjectSpecification objectSpec;
    private final GraphQLCodeRegistry.Builder codeRegistryBuilder;
    private final BookmarkService bookmarkService;
    private final ObjectManager objectManager;
    private final SpecificationLoader specificationLoader;

    @Getter private final GraphQLFieldDefinition metaField;
    @Getter private final GraphQLObjectType.Builder gqlObjectTypeBuilder;
    @Getter private final GraphQLInputObjectType gqlInputObjectType;

    private String getLogicalTypeName() {
        return objectSpec.getLogicalTypeName();
    }

    public String getLogicalTypeNameSanitized() {
        return _LTN.sanitized(objectSpec);
    }

    public BeanSort getBeanSort() {
        return objectSpec.getBeanSort();
    }

    public GraphQLObjectType getMetaType() {
        return (GraphQLObjectType) metaField.getType();
    }

    String getMutatorsTypeName() {
        return getLogicalTypeNameSanitized() + "__DomainObject_mutators";
    }

    final GraphQLObjectType.Builder mutatorsTypeBuilder;

    private final List<GqlvProperty> properties = new ArrayList<>();
    public List<GqlvProperty> getProperties() {return Collections.unmodifiableList(properties);}

    private final List<GqlvCollection> collections = new ArrayList<>();
    public List<GqlvCollection> getCollections() {return Collections.unmodifiableList(collections);}

    private final List<GqlvAction> safeActions = new ArrayList<>();
    public List<GqlvAction> getSafeActions() {return Collections.unmodifiableList(safeActions);}

    private final List<GqlvAction> mutatorActions = new ArrayList<>();
    public List<GqlvAction> getMutatorActions() {return Collections.unmodifiableList(mutatorActions);}

    /**
     * Built using {@link #buildGqlObjectType()}
     */
    private GraphQLObjectType gqlObjectType;

    /**
     * Built lazily using {@link #buildMutatorsTypeIfAny()}
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<GraphQLObjectType> mutatorsTypeIfAny;


    public GqlvDomainObject(
            final ObjectSpecification objectSpec,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder,
            final BookmarkService bookmarkService,
            final ObjectManager objectManager,
            final SpecificationLoader specificationLoader) {
        this.objectSpec = objectSpec;
        this.codeRegistryBuilder = codeRegistryBuilder;
        this.bookmarkService = bookmarkService;
        this.objectManager = objectManager;
        this.specificationLoader = specificationLoader;
        this.gqlObjectTypeBuilder = newObject().name(getLogicalTypeNameSanitized());

        // object type's meta field
        metaField = newFieldDefinition().name("_gql_meta").type(metaType()).build();
        gqlObjectTypeBuilder.field(metaField);

        // input object type
        String inputTypeName = GQL_INPUTTYPE_PREFIX + getLogicalTypeNameSanitized();
        GraphQLInputObjectType.Builder inputTypeBuilder = newInputObject().name(inputTypeName);
        inputTypeBuilder
                .field(newInputObjectField()
                        .name("id")
                        .type(nonNull(Scalars.GraphQLID))
                        .build());
        gqlInputObjectType = inputTypeBuilder.build();

        mutatorsTypeBuilder = newObject().name(getMutatorsTypeName());
    }


    private GraphQLObjectType metaType() {
        val metaTypeBuilder = newObject().name(getLogicalTypeNameSanitized() + "__DomainObject_meta");
        metaTypeBuilder.field(Fields.id);
        metaTypeBuilder.field(Fields.logicalTypeName);
        if (getBeanSort() == BeanSort.ENTITY) {
            metaTypeBuilder.field(Fields.version);
        }
        return metaTypeBuilder.build();
    }


    public void addPropertiesAsFields() {
        objectSpec.streamProperties(MixedIn.INCLUDED).forEach(this::addPropertyAsField);
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
                getGqlObjectTypeBuilder().field(
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
                getGqlObjectTypeBuilder().field(fieldDefinition);

                break;
        }
        if (fieldDefinition != null) {
            properties.add(new GqlvProperty(otoa, fieldDefinition));
        }
    }


    public void addCollectionsAsLists() {
        objectSpec.streamCollections(MixedIn.INCLUDED).forEach(this::addCollection);
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
            collections.add(new GqlvCollection(otom, fieldDefinition));
        }
    }



    void addAction(final ObjectAction objectAction) {

        val fieldName = objectAction.getId();
        GraphQLFieldDefinition.Builder fieldBuilder = newFieldDefinition()
                .name(fieldName)
                .type((GraphQLOutputType) TypeMapper.typeForObjectAction(objectAction));
        addGqlArguments(objectAction, fieldBuilder);
        GraphQLFieldDefinition fieldDefinition = fieldBuilder.build();

        if (objectAction.getSemantics().isSafeInNature()) {
            addSafeActionAsField(objectAction, fieldDefinition);
        } else {
            addNonSafeActionAsMutatorField(objectAction, fieldDefinition);
        }
    }

    private static void addGqlArguments(
            final ObjectAction objectAction,
            final GraphQLFieldDefinition.Builder builder) {

        Can<ObjectActionParameter> parameters = objectAction.getParameters();

        if (parameters.isNotEmpty()) {
            builder.arguments(parameters.stream()
                    .map(GqlvDomainObject::gqlArgumentFor)
                    .collect(Collectors.toList()));
        }
    }

    private static GraphQLArgument gqlArgumentFor(final ObjectActionParameter objectActionParameter) {
        return GraphQLArgument.newArgument()
                .name(objectActionParameter.getId())
                .type(objectActionParameter.isOptional()
                        ? TypeMapper.inputTypeFor(objectActionParameter)
                        : nonNull(TypeMapper.inputTypeFor(objectActionParameter)))
                .build();
    }


    public void addSafeActionAsField(
            final ObjectAction objectAction,
            final GraphQLFieldDefinition fieldDefinition) {
        getGqlObjectTypeBuilder().field(fieldDefinition);
        safeActions.add(new GqlvAction(objectAction, fieldDefinition));
    }

    public void addNonSafeActionAsMutatorField(
            final ObjectAction objectAction,
            final GraphQLFieldDefinition fieldDefinition) {
        mutatorsTypeBuilder.field(fieldDefinition);
        mutatorActions.add(new GqlvAction(objectAction, fieldDefinition));
    }

    boolean hasMutators() {
        return !mutatorActions.isEmpty();
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
        return gqlObjectType = getGqlObjectTypeBuilder().name(getLogicalTypeNameSanitized()).build();
    }

    /**
     * @see #buildGqlObjectType()
     */
    GraphQLObjectType getGqlObjectType() {
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
        //noinspection OptionalAssignedToNull
        if (mutatorsTypeIfAny == null) {
            throw new IllegalArgumentException(String.format("Gql MutatorsType has not yet been built for %s", getLogicalTypeName()));
        }
        return mutatorsTypeIfAny;
    }

    /**
     * @see #getMutatorsTypeIfAny()
     */
    public Optional<GraphQLObjectType> buildMutatorsTypeIfAny() {
        //noinspection OptionalAssignedToNull
        if (mutatorsTypeIfAny != null) {
            throw new IllegalArgumentException("Gql MutatorsType has already been built for " + getLogicalTypeName());
        }
        return mutatorsTypeIfAny = hasMutators()
                ? Optional.of(mutatorsTypeBuilder.build())
                : Optional.empty();
    }

    public void addActionsAsFields() {

        objectSpec.streamActions(ActionScope.PRODUCTION, MixedIn.INCLUDED)
                .forEach(this::addAction);

        Optional<GraphQLObjectType> mutatorsTypeIfAny = buildMutatorsTypeIfAny();
        mutatorsTypeIfAny.ifPresent(mutatorsType -> {

            GraphQLFieldDefinition gql_mutations = newFieldDefinition()
                    .name(_Constants.GQL_MUTATIONS_FIELDNAME)
                    .type(mutatorsType)
                    .build();
            getGqlObjectTypeBuilder().field(gql_mutations);

        });

    }

    public void createAndRegisterDataFetchersForMetaData() {

        codeRegistryBuilder.dataFetcher(
                coordinates(getGqlObjectType(), getMetaField()),
                (DataFetcher<Object>) environment -> {
                    return bookmarkService.bookmarkFor(environment.getSource())
                            .map(bookmark -> new GqlvMeta(bookmark, bookmarkService, objectManager))
                            .orElse(null); //TODO: is this correct ?
                });

        codeRegistryBuilder.dataFetcher(
                coordinates(getMetaType(), GqlvDomainObject.Fields.id),
                (DataFetcher<Object>) environment -> {
                    GqlvMeta gqlvMeta = environment.getSource();
                    return gqlvMeta.id();
                });

        codeRegistryBuilder.dataFetcher(
                coordinates(getMetaType(), GqlvDomainObject.Fields.logicalTypeName),
                (DataFetcher<Object>) environment -> {
                    GqlvMeta gqlvMeta = environment.getSource();
                    return gqlvMeta.logicalTypeName();
                });

        if (getBeanSort() == BeanSort.ENTITY) {
            codeRegistryBuilder.dataFetcher(
                    coordinates(getMetaType(), GqlvDomainObject.Fields.version),
                    (DataFetcher<Object>) environment -> {
                        GqlvMeta gqlvMeta = environment.getSource();
                        return gqlvMeta.version();
                    });
        }

    }


    public void createAndRegisterDataFetchersForField() {
        getProperties().forEach(this::createAndRegisterDataFetcherForAssociation);
    }

    public void createAndRegisterDataFetchersForCollection() {
        getCollections().forEach(
                this::createAndRegisterDataFetcherForAssociation);
    }

    private void createAndRegisterDataFetcherForAssociation(final GqlvAssociation<?> property) {

        final ObjectAssociation association = property.getObjectMember();
        final GraphQLFieldDefinition field = property.getFieldDefinition();

        final GraphQLObjectType graphQLObjectType = getGqlObjectType();

        ObjectSpecification fieldObjectSpecification = association.getElementType();
        BeanSort beanSort = fieldObjectSpecification.getBeanSort();
        switch (beanSort) {

            case VALUE: //TODO: does this work for values as well?

            case VIEW_MODEL:

            case ENTITY:

                codeRegistryBuilder.dataFetcher(
                        coordinates(graphQLObjectType, field),
                        (DataFetcher<Object>) environment -> {

                            Object domainObjectInstance = environment.getSource();

                            Class<?> domainObjectInstanceClass = domainObjectInstance.getClass();
                            ObjectSpecification specification = specificationLoader.loadSpecification(domainObjectInstanceClass);

                            ManagedObject owner = ManagedObject.adaptSingular(specification, domainObjectInstance);
                            ManagedObject managedObject = association.get(owner);

                            return managedObject!=null ? managedObject.getPojo() : null;
                        });

                break;

        }
    }

    public void createAndRegisterDataFetchersForMutators() {

        // something like:

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

}
