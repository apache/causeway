package org.apache.causeway.viewer.graphql.model.domain;

import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.viewer.graphql.model.types.ScalarMapper;
import org.apache.causeway.viewer.graphql.model.util.TypeNames;

import org.springframework.lang.Nullable;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLTypeReference.typeRef;

public class GqlvCollection extends GqlvAssociation<OneToManyAssociation, GqlvCollectionHolder> {

    public GqlvCollection(
            final GqlvCollectionHolder domainObject,
            final OneToManyAssociation oneToManyAssociation,
            final GraphQLCodeRegistry.Builder codeRegistryBuilder
    ) {
        super(domainObject, oneToManyAssociation, fieldDefinition(domainObject, oneToManyAssociation), codeRegistryBuilder);
    }

    @Nullable private static GraphQLFieldDefinition fieldDefinition(
            final GqlvCollectionHolder holder,
            final OneToManyAssociation otom) {
        ObjectSpecification elementType = otom.getElementType();

        GraphQLFieldDefinition fieldDefinition = null;
        GraphQLList type = listTypeFor(elementType);
        if (type != null) {
                fieldDefinition = newFieldDefinition()
                        .name(otom.getId())
                        .type(type).build();
                holder.addField(fieldDefinition);
        }
        return fieldDefinition;
    }

    @Nullable private static GraphQLList listTypeFor(ObjectSpecification elementType) {
        switch (elementType.getBeanSort()) {
            case VIEW_MODEL:
            case ENTITY:
                return GraphQLList.list(typeRef(TypeNames.objectTypeNameFor(elementType)));
            case VALUE:
                return GraphQLList.list(ScalarMapper.typeFor(elementType.getCorrespondingClass()));
        }
        return null;
    }

    public OneToManyAssociation getOneToManyAssociation() {
        return getObjectAssociation();
    }

}
