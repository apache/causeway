package org.apache.causeway.viewer.graphql.viewer.source;

import graphql.Scalars;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;

import graphql.schema.GraphQLTypeReference;

import lombok.Getter;
import lombok.val;

import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLNonNull.nonNull;
import static graphql.schema.GraphQLObjectType.newObject;

/**
 * A wrapper around {@link ObjectSpecification}
 */
public class GqlvObjectSpec {

    private final ObjectSpecification objectSpec;

    public String getLogicalTypeNameSanitized() {
        val logicalTypeName = objectSpec.getLogicalTypeName();
        return _LogicalTypeName.sanitized(logicalTypeName);
    }

    public BeanSort getBeanSort() {
        return objectSpec.getBeanSort();
    }

    @Getter private final GraphQLObjectType metaType;
    @Getter private final GraphQLFieldDefinition metaField;

    @Getter private final GraphQLObjectType.Builder gqlObjectTypeBuilder;

    /**
     * Build using {@link #buildGqlObjectType()}
     */
    private GraphQLObjectType gqlObjectType;

    public GraphQLObjectType getGqlObjectType() {
        if (gqlObjectType == null) {
            throw new IllegalStateException("GraphQLObjectType has not yet been built for " + getLogicalTypeNameSanitized());
        }
        return gqlObjectType;
    }

    public GqlvObjectSpec(final ObjectSpecification objectSpec) {
        this.objectSpec = objectSpec;
        this.gqlObjectTypeBuilder = newObject().name(getLogicalTypeNameSanitized());

        // meta type
        val metaTypeBuilder = newObject().name(getLogicalTypeNameSanitized() + "__DomainObject_meta");
        metaTypeBuilder.field(ObjectTypeFactory.Fields.id);
        metaTypeBuilder.field(ObjectTypeFactory.Fields.logicalTypeName);
        if (getBeanSort() == BeanSort.ENTITY) {
            metaTypeBuilder.field(ObjectTypeFactory.Fields.version);
        }
        this.metaType = metaTypeBuilder.build();

        // meta field
        metaField = newFieldDefinition().name("_gql_meta").type(metaType).build();
        gqlObjectTypeBuilder.field(metaField);

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

    public GraphQLObjectType buildGqlObjectType() {
        if (gqlObjectType != null) {
            throw new IllegalArgumentException("GqlObjectType has already been built");
        }
        gqlObjectType = getGqlObjectTypeBuilder().name(getLogicalTypeNameSanitized()).build();
        return gqlObjectType;
    }
}
