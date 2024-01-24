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
package org.apache.causeway.viewer.graphql.model.types;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

import graphql.Scalars;
import graphql.schema.*;

import lombok.experimental.UtilityClass;

import javax.ws.rs.NotSupportedException;

import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneActionParameter;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneFeature;
import org.apache.causeway.viewer.graphql.model.util.TypeNames;

import org.springframework.lang.Nullable;

import static graphql.schema.GraphQLNonNull.nonNull;
import static graphql.schema.GraphQLTypeReference.typeRef;

@UtilityClass
public class TypeMapper {

    private static <K,V> Map.Entry<K,V> pair(K key, V value) {
        return new Map.Entry<K, V>() {
            @Override public K getKey() {return key;}
            @Override public V getValue() {return value;}
            @Override public V setValue(V value) { throw new NotSupportedException("Immutable"); }
        };
    }

    private static final Map<Class<?>, GraphQLScalarType> SCALAR_BY_CLASS = _Maps.unmodifiableEntries(
            pair(int.class, Scalars.GraphQLInt),
            pair(Integer.class, Scalars.GraphQLInt),
            pair(Short.class, Scalars.GraphQLInt),
            pair(short.class, Scalars.GraphQLInt),
            pair(BigInteger.class, Scalars.GraphQLInt),
            pair(long.class, Scalars.GraphQLFloat),
            pair(Long.class, Scalars.GraphQLFloat),
            pair(BigDecimal.class, Scalars.GraphQLFloat),
            pair(boolean.class, Scalars.GraphQLBoolean),
            pair(Boolean.class, Scalars.GraphQLBoolean)
    );

    public static GraphQLScalarType scalarTypeFor(final Class<?> c){
        return SCALAR_BY_CLASS.getOrDefault(c, Scalars.GraphQLString);
    }

    public static GraphQLOutputType outputTypeFor(final OneToOneFeature oneToOneFeature) {
        ObjectSpecification otoaObjectSpec = oneToOneFeature.getElementType();
        switch (otoaObjectSpec.getBeanSort()) {

            case VIEW_MODEL:
            case ENTITY:

                GraphQLTypeReference fieldTypeRef = typeRef(TypeNames.objectTypeNameFor(otoaObjectSpec));
                return oneToOneFeature.isOptional()
                        ? fieldTypeRef
                        : nonNull(fieldTypeRef);

            case VALUE:

                GraphQLScalarType scalarType = scalarTypeFor(otoaObjectSpec.getCorrespondingClass());

                return oneToOneFeature.isOptional()
                        ? scalarType
                        : nonNull(scalarType);
        }
        return null;
    }

    @Nullable
    public static GraphQLOutputType outputTypeFor(final ObjectSpecification objectSpecification){

        switch (objectSpecification.getBeanSort()){
            case ABSTRACT:
            case ENTITY:
            case VIEW_MODEL:
                return typeRef(TypeNames.objectTypeNameFor(objectSpecification));

            case VALUE:
                return scalarTypeFor(objectSpecification.getCorrespondingClass());

            case COLLECTION:
                // should be noop
                return null;

            default:
                // for now
                return Scalars.GraphQLString;
        }
    }

    @Nullable public static GraphQLList listTypeForElementTypeOf(OneToManyAssociation oneToManyAssociation) {
        ObjectSpecification elementType = oneToManyAssociation.getElementType();
        return TypeMapper.listTypeFor(elementType);
    }

    @Nullable public static GraphQLList listTypeFor(ObjectSpecification elementType) {
        switch (elementType.getBeanSort()) {
            case VIEW_MODEL:
            case ENTITY:
                return GraphQLList.list(typeRef(TypeNames.objectTypeNameFor(elementType)));
            case VALUE:
                return GraphQLList.list(TypeMapper.scalarTypeFor(elementType.getCorrespondingClass()));
        }
        return null;
    }

    public static GraphQLInputType inputTypeFor(final OneToOneFeature oneToOneFeature) {
        return oneToOneFeature.isOptional()
                ? inputTypeFor_(oneToOneFeature)
                : nonNull(inputTypeFor_(oneToOneFeature));
    }

    private static GraphQLInputType inputTypeFor_(final OneToOneFeature oneToOneFeature){
        ObjectSpecification elementType = oneToOneFeature.getElementType();
        switch (elementType.getBeanSort()) {
            case ABSTRACT:
            case ENTITY:
            case VIEW_MODEL:
                return typeRef(TypeNames.inputTypeNameFor(elementType));

            case VALUE:
                return scalarTypeFor(elementType.getCorrespondingClass());

            case COLLECTION:
                // TODO ...
            default:
                // for now
                return Scalars.GraphQLString;
        }
    }

}
