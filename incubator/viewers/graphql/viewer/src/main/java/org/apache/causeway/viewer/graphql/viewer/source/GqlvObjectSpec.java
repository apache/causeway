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

    @Getter private final GraphQLObjectType.Builder objectTypeBuilder;

    @Getter private final GraphQLObjectType metaType;

    static GqlvObjectSpec gqlv(final ObjectSpecification objectSpec) {
        return new GqlvObjectSpec(objectSpec);
    }

    GqlvObjectSpec(final ObjectSpecification objectSpec) {
        this.objectSpec = objectSpec;
        this.objectTypeBuilder = newObject().name(getLogicalTypeNameSanitized());
        this.metaType = _GraphQLObjectType.create(getLogicalTypeNameSanitized(), getBeanSort());
    }


    void addFields() {
        objectSpec.streamProperties(MixedIn.INCLUDED)
        .forEach(this::addField);
    }

    private void addField(OneToOneAssociation otoa) {
        ObjectSpecification fieldObjectSpecification = otoa.getElementType();
        BeanSort beanSort = fieldObjectSpecification.getBeanSort();
        switch (beanSort) {

            case VIEW_MODEL:
            case ENTITY:

                String logicalTypeNameOfField = fieldObjectSpecification.getLogicalTypeName();

                GraphQLFieldDefinition.Builder fieldBuilder = newFieldDefinition()
                    .name(otoa.getId())
                    .type(otoa.isOptional()
                            ? GraphQLTypeReference.typeRef(
                                    _LogicalTypeName.sanitized(logicalTypeNameOfField))
                            : nonNull(GraphQLTypeReference.typeRef(
                                    _LogicalTypeName.sanitized(logicalTypeNameOfField))));
                getObjectTypeBuilder().field(fieldBuilder);

                break;

            case VALUE:

                // todo: map ...

                GraphQLFieldDefinition.Builder valueBuilder = newFieldDefinition()
                    .name(otoa.getId())
                    .type(otoa.isOptional()
                            ? Scalars.GraphQLString
                            : nonNull(Scalars.GraphQLString));
                getObjectTypeBuilder().field(valueBuilder);

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
                objectTypeBuilder.field(fieldBuilder);
                break;

            case VALUE:
                GraphQLFieldDefinition.Builder valueBuilder = newFieldDefinition()
                    .name(otom.getId())
                    .type(GraphQLList.list(TypeMapper.typeFor(elementType.getCorrespondingClass())));
                objectTypeBuilder.field(valueBuilder);
                break;
        }
    }
}
