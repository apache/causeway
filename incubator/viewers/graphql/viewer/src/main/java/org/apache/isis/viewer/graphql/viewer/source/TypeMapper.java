package org.apache.isis.viewer.graphql.viewer.source;

import graphql.Scalars;
import graphql.schema.*;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

public class TypeMapper {

    private static List<Class> mapToInteger = Arrays.asList(int.class, Integer.class, Short.class, short.class, BigInteger.class);
    private static List<Class> mapToLong = Arrays.asList(Long.class, long.class, BigDecimal.class);
    private static List<Class> mapToBoolean = Arrays.asList(Boolean.class, boolean.class);

    public static GraphQLType typeFor(final Class c){
        if (mapToInteger.contains(c)){
            return Scalars.GraphQLInt;
        }
        if (mapToLong.contains(c)){
            return Scalars.GraphQLFloat;
        }
        if (mapToBoolean.contains(c)){
            return Scalars.GraphQLBoolean;
        }
        return Scalars.GraphQLString;
    }

    public static GraphQLInputType inputTypeFor(final ObjectActionParameter objectActionParameter){
        ObjectSpecification elementType = objectActionParameter.getElementType();
        switch (elementType.getBeanSort()) {
            case ABSTRACT:
            case ENTITY:
            case VIEW_MODEL:

                return GraphQLTypeReference.typeRef(Utils.GQL_INPUTTYPE_PREFIX + Utils.logicalTypeNameSanitized(elementType.getLogicalTypeName()));

            case VALUE:
                return (GraphQLInputType) typeFor(elementType.getCorrespondingClass());

            case COLLECTION:
                // TODO ...
            default:
                // for now
                return Scalars.GraphQLString;
        }

    }

    public static GraphQLType typeForObjectAction(final ObjectAction objectAction){
        ObjectSpecification objectSpecification = objectAction.getReturnType();
        switch (objectSpecification.getBeanSort()){

            case COLLECTION:

                TypeOfFacet facet = objectAction.getFacet(TypeOfFacet.class);
                if (facet == null) return GraphQLList.list(Scalars.GraphQLString); // TODO: for now ... Investigate why this can happen
                ObjectSpecification objectSpecificationForElementWhenCollection = facet.valueSpec();
                return GraphQLList.list(outputTypeFor(objectSpecificationForElementWhenCollection));

            case VALUE:
            case ENTITY:
            case VIEW_MODEL:
            default:
                return outputTypeFor(objectSpecification);

        }
    }

    public static GraphQLType outputTypeFor(final ObjectSpecification objectSpecification){

        switch (objectSpecification.getBeanSort()){
            case ABSTRACT:
            case ENTITY:
            case VIEW_MODEL:
                return GraphQLTypeReference.typeRef(Utils.logicalTypeNameSanitized(objectSpecification.getLogicalTypeName()));

            case VALUE:
                return typeFor(objectSpecification.getCorrespondingClass());

            case COLLECTION:
                // should be noop
                return null;

            default:
                // for now
                return Scalars.GraphQLString;
        }
    }


}
