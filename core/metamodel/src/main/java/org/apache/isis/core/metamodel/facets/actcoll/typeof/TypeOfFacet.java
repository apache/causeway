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

package org.apache.isis.core.metamodel.facets.actcoll.typeof;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.commons.internal.collections._Arrays;
import org.apache.isis.commons.internal.collections._Collections;
import org.apache.isis.commons.internal.reflection._Generics;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.SingleClassValueFacet;

import lombok.val;

/**
 * The type of the collection or the action.
 *
 * <p>
 * In the standard Apache Isis Programming Model, corresponds to annotating the
 * collection's accessor or the action's invoker method with the
 * {@link Collection#typeOf} annotation.
 */
public interface TypeOfFacet extends SingleClassValueFacet {

    public static class Util {
        private Util(){}
        
        public static TypeOfFacet inferFromParameterType(
                final FacetHolder holder,
                final Class<?> parameterType,
                final Type genericParameterType) {
        
            TypeOfFacet typeOfFacet = inferFromGenericParamType(holder, parameterType, genericParameterType);
            
            if(typeOfFacet == null ) {
                if (_Arrays.isArrayType(parameterType)) {
                    typeOfFacet = inferFromArrayType(holder, parameterType);
                }
            }
            
            return typeOfFacet;
        }
        
        public static TypeOfFacet inferFromMethodReturnType(
                final FacetHolder holder,
                final Class<?> methodOwner,
                final Method method) {
            
            // infer from return type
            val returnType = method.getReturnType();
            TypeOfFacet typeOfFacet = inferFromArrayType(holder, returnType);

            // infer from generic return type
            if(typeOfFacet == null) {
                typeOfFacet = inferFromGenericReturnType(methodOwner, method, holder);
            }
            
            return typeOfFacet;
        }
        

        private static TypeOfFacet inferFromGenericReturnType(
                final Class<?> cls,
                final Method method,
                final FacetHolder holder) {

            final Class<?> methodReturnType = method.getReturnType();
            if (!_Collections.isCollectionType(methodReturnType) 
                    && !_Collections.isCanType(methodReturnType)) {
                return null;
            }

            return _Generics.streamGenericTypeArgumentsOfMethodReturnType(method)
                .findFirst()
                .map(elementType->new TypeOfFacetInferredFromGenerics(elementType, holder))
                .orElse(null);
        }

        private static TypeOfFacet inferFromArrayType(
                final FacetHolder holder,
                final Class<?> type) {

            final Class<?> elementType = _Arrays.inferComponentTypeIfAny(type);
            return elementType != null
                    ? new TypeOfFacetInferredFromArray(elementType, holder)
                    : null;
        }

        private static TypeOfFacet inferFromGenericParamType(
                final FacetHolder holder,
                final Class<?> parameterType,
                final Type genericParameterType) {

            Class<?> elementType = _Collections.inferElementTypeIfAny(parameterType, genericParameterType);
            
            return elementType != null
                    ? new TypeOfFacetInferredFromGenerics(elementType, holder)
                    : null;
        }
    }
}
