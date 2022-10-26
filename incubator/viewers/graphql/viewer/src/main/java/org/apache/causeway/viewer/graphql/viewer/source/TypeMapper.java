/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.viewer.graphql.viewer.source;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import org.apache.causeway.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;

import graphql.Scalars;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;

public class TypeMapper {

    private static List<Class<?>> mapToInteger = Arrays.asList(
            int.class, Integer.class, Short.class, short.class, BigInteger.class);
    private static List<Class<?>> mapToLong = Arrays.asList(Long.class, long.class, BigDecimal.class);
    private static List<Class<?>> mapToBoolean = Arrays.asList(Boolean.class, boolean.class);

    public static GraphQLType typeFor(final Class<?> c){
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

                return GraphQLTypeReference.typeRef(_Utils.GQL_INPUTTYPE_PREFIX + _Utils.logicalTypeNameSanitized(elementType.getLogicalTypeName()));

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
                ObjectSpecification objectSpecificationForElementWhenCollection = facet.elementSpec();
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
                return GraphQLTypeReference.typeRef(_Utils.logicalTypeNameSanitized(objectSpecification.getLogicalTypeName()));

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
