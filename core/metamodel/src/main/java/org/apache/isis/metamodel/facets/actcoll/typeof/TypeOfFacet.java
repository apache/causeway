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

package org.apache.isis.metamodel.facets.actcoll.typeof;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.commons.internal.collections._Arrays;
import org.apache.isis.commons.internal.collections._Collections;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.SingleClassValueFacet;

/**
 * The type of the collection or the action.
 *
 * <p>
 * In the standard Apache Isis Programming Model, corresponds to annotating the
 * collection's accessor or the action's invoker method with the
 * <tt>@TypeOf</tt> annotation.
 */
public interface TypeOfFacet extends SingleClassValueFacet {

    public static class Util {
        private Util(){}

        @Programmatic
        public static TypeOfFacet inferFromGenericReturnType(
                final Class<?> cls,
                final Method method,
                final FacetHolder holder) {

            final Class<?> methodReturnType = method.getReturnType();
            if (!_Collections.isCollectionType(methodReturnType)) {
                return null;
            }

            final Type type = method.getGenericReturnType();
            if (!(type instanceof ParameterizedType)) {
                return null;
            }

            final ParameterizedType methodParameterizedType = (ParameterizedType) type;
            final Type[] methodActualTypeArguments = methodParameterizedType.getActualTypeArguments();

            if (methodActualTypeArguments.length == 0) {
                return null;
            }

            final Object methodActualTypeArgument = methodActualTypeArguments[0];
            if (methodActualTypeArgument instanceof Class) {
                final Class<?> actualType = (Class<?>) methodActualTypeArgument;
                return new TypeOfFacetInferredFromGenerics(actualType, holder);
            }

            if (methodActualTypeArgument instanceof TypeVariable) {

                final TypeVariable<?> methodTypeVariable = (TypeVariable<?>) methodActualTypeArgument;
                final GenericDeclaration methodGenericClassDeclaration = methodTypeVariable.getGenericDeclaration();

                // try to match up with the actual type argument of the generic superclass.
                final Type genericSuperclass = cls.getGenericSuperclass();
                if(genericSuperclass instanceof ParameterizedType) {
                    final ParameterizedType parameterizedTypeOfSuperclass = (ParameterizedType)genericSuperclass;
                    if(parameterizedTypeOfSuperclass.getRawType() == methodGenericClassDeclaration) {
                        final Type[] genericSuperClassActualTypeArguments = parameterizedTypeOfSuperclass.getActualTypeArguments();
                        // simplification: if there's just one, then use it.
                        if(methodActualTypeArguments.length == 1) {
                            final Type actualType = genericSuperClassActualTypeArguments[0];
                            if(actualType instanceof Class) {
                                // just being safe
                                final Class<?> actualCls = (Class<?>) actualType;
                                return new TypeOfFacetInferredFromGenerics(actualCls, holder);
                            }
                        }
                    }
                }
                // otherwise, what to do?
            }
            return null;
        }

        @Programmatic
        public static TypeOfFacet inferFromArrayType(
                final FacetHolder holder,
                final Class<?> type) {
            
            final Class<?> elementType = _Arrays.inferComponentTypeIfAny(type);
            return elementType != null
                    ? new TypeOfFacetInferredFromArray(elementType, holder)
                            : null;
        }

        @Programmatic
        public static TypeOfFacet inferFromGenericParamType(
                final FacetHolder holder,
                final Class<?> parameterType,
                final Type genericParameterType) {

            final Class<?> elementType = _Collections.inferElementTypeIfAny(parameterType, genericParameterType);
            return elementType != null
                    ? new TypeOfFacetInferredFromGenerics(elementType, holder)
                            : null;
        }
    }
}
