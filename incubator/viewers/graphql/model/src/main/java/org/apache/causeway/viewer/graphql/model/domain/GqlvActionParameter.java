package org.apache.causeway.viewer.graphql.model.domain;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.viewer.graphql.model.types.ScalarMapper;
import org.apache.causeway.viewer.graphql.model.util.TypeNames;

import graphql.Scalars;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLTypeReference;

public class GqlvActionParameter {
    public static GraphQLInputType inputTypeFor(final ObjectActionParameter objectActionParameter){
        ObjectSpecification elementType = objectActionParameter.getElementType();
        switch (elementType.getBeanSort()) {
            case ABSTRACT:
            case ENTITY:
            case VIEW_MODEL:

                return GraphQLTypeReference.typeRef(TypeNames.inputTypeNameFor(elementType));

            case VALUE:
                return (GraphQLInputType) ScalarMapper.typeFor(elementType.getCorrespondingClass());

            case COLLECTION:
                // TODO ...
            default:
                // for now
                return Scalars.GraphQLString;
        }

    }
}
