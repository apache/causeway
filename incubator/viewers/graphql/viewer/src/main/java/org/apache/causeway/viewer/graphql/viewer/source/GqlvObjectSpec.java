package org.apache.causeway.viewer.graphql.viewer.source;

import graphql.Scalars;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;

import graphql.schema.GraphQLTypeReference;

import lombok.Getter;
import lombok.val;

import java.util.ArrayList;
import java.util.List;

import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static graphql.schema.GraphQLInputObjectType.newInputObject;
import static graphql.schema.GraphQLNonNull.nonNull;
import static graphql.schema.GraphQLObjectType.newObject;

import static org.apache.causeway.viewer.graphql.viewer.source.ObjectTypeFactory.GQL_INPUTTYPE_PREFIX;

/**
 * A wrapper around {@link ObjectSpecification}
 */
public class GqlvObjectSpec {

    @Getter private final ObjectSpecification objectSpec;
    @Getter private final GraphQLFieldDefinition metaField;
    @Getter private final GraphQLObjectType.Builder gqlObjectTypeBuilder;
    @Getter private final GraphQLInputObjectType gqlInputObjectType;

    public String getLogicalTypeNameSanitized() {
        val logicalTypeName = objectSpec.getLogicalTypeName();
        return _LogicalTypeName.sanitized(logicalTypeName);
    }

    public BeanSort getBeanSort() {
        return objectSpec.getBeanSort();
    }

    public GraphQLObjectType getMetaType() {
        return (GraphQLObjectType) metaField.getType();
    }


    final String mutatorsTypeName;
    final GraphQLObjectType.Builder mutatorsTypeBuilder;
    final List<GraphQLFieldDefinition> mutatorsTypeFields = new ArrayList<>();

    /**
     * Built using {@link #buildGqlObjectType()}
     */
    private GraphQLObjectType gqlObjectType;

    public GqlvObjectSpec(final ObjectSpecification objectSpec) {
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

        mutatorsTypeName = getLogicalTypeNameSanitized() + "__DomainObject_mutators";
        mutatorsTypeBuilder = newObject().name(mutatorsTypeName);

    }

    private GraphQLObjectType metaType() {
        val metaTypeBuilder = newObject().name(getLogicalTypeNameSanitized() + "__DomainObject_meta");
        metaTypeBuilder.field(ObjectTypeFactory.Fields.id);
        metaTypeBuilder.field(ObjectTypeFactory.Fields.logicalTypeName);
        if (getBeanSort() == BeanSort.ENTITY) {
            metaTypeBuilder.field(ObjectTypeFactory.Fields.version);
        }
        return metaTypeBuilder.build();
    }



    void addFields() {
        objectSpec.streamProperties(MixedIn.INCLUDED)
        .forEach(this::addField);
    }

    private void addField(OneToOneAssociation otoa) {
        ObjectSpecification otoaObjectSpec = otoa.getElementType();
        switch (otoaObjectSpec.getBeanSort()) {

            case VIEW_MODEL:
            case ENTITY:

                String logicalTypeNameOfField = otoaObjectSpec.getLogicalTypeName();

                GraphQLFieldDefinition.Builder fieldBuilder = newFieldDefinition()
                    .name(otoa.getId())
                    .type(otoa.isOptional()
                            ? GraphQLTypeReference.typeRef(
                                    _LogicalTypeName.sanitized(logicalTypeNameOfField))
                            : nonNull(GraphQLTypeReference.typeRef(
                                    _LogicalTypeName.sanitized(logicalTypeNameOfField))));
                getGqlObjectTypeBuilder().field(fieldBuilder);

                break;

            case VALUE:

                // todo: map ...

                GraphQLFieldDefinition.Builder valueBuilder = newFieldDefinition()
                    .name(otoa.getId())
                    .type(otoa.isOptional()
                            ? Scalars.GraphQLString
                            : nonNull(Scalars.GraphQLString));
                getGqlObjectTypeBuilder().field(valueBuilder);

                break;

        }
    }

    void addCollections() {
        objectSpec.streamCollections(MixedIn.INCLUDED).forEach(this::addCollection);
    }

    private void addCollection(OneToManyAssociation otom) {

        ObjectSpecification elementType = otom.getElementType();
        BeanSort beanSort = elementType.getBeanSort();
        switch (beanSort) {

            case VIEW_MODEL:
            case ENTITY:
                String logicalTypeNameOfField = elementType.getLogicalTypeName();
                GraphQLFieldDefinition.Builder fieldBuilder = newFieldDefinition()
                    .name(otom.getId())
                    .type(GraphQLList.list(GraphQLTypeReference.typeRef(
                            _LogicalTypeName.sanitized(logicalTypeNameOfField))));
                gqlObjectTypeBuilder.field(fieldBuilder);
                break;

            case VALUE:
                GraphQLFieldDefinition.Builder valueBuilder = newFieldDefinition()
                    .name(otom.getId())
                    .type(GraphQLList.list(TypeMapper.typeFor(elementType.getCorrespondingClass())));
                gqlObjectTypeBuilder.field(valueBuilder);
                break;
        }
    }

    /**
     * Should be called only after fields etc have been added.
     *
     * @see #getGqlObjectType()
     */
    GraphQLObjectType buildGqlObjectType() {
        if (gqlObjectType != null) {
            throw new IllegalArgumentException("GqlObjectType has already been built");
        }
        gqlObjectType = getGqlObjectTypeBuilder().name(getLogicalTypeNameSanitized()).build();
        return gqlObjectType;
    }

    /**
     * @see #buildGqlObjectType()
     */
    GraphQLObjectType getGqlObjectType() {
        if (gqlObjectType == null) {
            throw new IllegalStateException(String.format(
                    "GraphQLObjectType has not yet been built for %s", getLogicalTypeNameSanitized()));
        }
        return gqlObjectType;
    }

}
