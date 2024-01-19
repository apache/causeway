package org.apache.causeway.viewer.graphql.viewer.source;

import graphql.Scalars;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;

import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.spec.ActionScope;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static graphql.schema.GraphQLInputObjectType.newInputObject;
import static graphql.schema.GraphQLNonNull.nonNull;
import static graphql.schema.GraphQLObjectType.newObject;

import static graphql.schema.GraphQLTypeReference.typeRef;
import static org.apache.causeway.viewer.graphql.viewer.source._Constants.GQL_INPUTTYPE_PREFIX;

/**
 * A wrapper around {@link ObjectSpecification}
 */
public class GqlvObjectStructure {

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

    @Getter private final ObjectSpecification objectSpec;
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

    private final Map<OneToOneAssociation, GraphQLFieldDefinition> propertyToField = new LinkedHashMap<>();
    private final Map<OneToManyAssociation, GraphQLFieldDefinition> collectionToField = new LinkedHashMap<>();
    private final Map<ObjectAction, GraphQLFieldDefinition> safeActionToField = new LinkedHashMap<>();
    private final Map<ObjectAction, GraphQLFieldDefinition> mutatorActionToField = new LinkedHashMap<>();

    /**
     * Built using {@link #buildGqlObjectType()}
     */
    private GraphQLObjectType gqlObjectType;

    /**
     * Built lazily using {@link #buildMutatorsTypeIfAny()}
     */
    private Optional<GraphQLObjectType> mutatorsTypeIfAny;

    public GqlvObjectStructure(final ObjectSpecification objectSpec) {
        this.objectSpec = objectSpec;
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


    void addPropertiesAsFields() {
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
            propertyToField.put(otoa, fieldDefinition);
        }
    }


    void addCollectionsAsLists() {
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
            collectionToField.put(otom, fieldDefinition);
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
                    .map(GqlvObjectStructure::gqlArgumentFor)
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
        safeActionToField.put(objectAction, fieldDefinition);
    }

    public void addNonSafeActionAsMutatorField(
            final ObjectAction objectAction,
            final GraphQLFieldDefinition fieldDefinition) {
        mutatorsTypeBuilder.field(fieldDefinition);
        mutatorActionToField.put(objectAction, fieldDefinition);
    }

    boolean hasMutators() {
        return !mutatorActionToField.isEmpty();
    }


    /**
     * Should be called only after fields etc have been added.
     *
     * @see #getGqlObjectType()
     */
    GraphQLObjectType buildGqlObjectType() {
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

    void addActionsAsFields() {

        getObjectSpec().streamActions(ActionScope.PRODUCTION, MixedIn.INCLUDED)
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
}
