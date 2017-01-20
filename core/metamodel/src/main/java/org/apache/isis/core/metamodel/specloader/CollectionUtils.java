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

package org.apache.isis.core.metamodel.specloader;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;

/**
 * Defines the types which are considered to be collections.
 * 
 * <p>
 * In this way there are similarities with the way in which value types are
 * specified using <tt>@Value</tt>. However, we need to maintain a repository of
 * these collection types once nominated so that when we introspect classes we
 * look for collections first, and then properties second.
 */
public final class CollectionUtils {

    private CollectionUtils() {}

    public static boolean isCollectionType(final Class<?> cls) {
        return java.util.Collection.class.isAssignableFrom(cls);
    }

    public static boolean isArrayType(final Class<?> cls) {
        return cls.isArray();
    }

    public static Class<?> inferFromArrayType(final Class<?> type) {
        if(!isArrayType(type)) {
            return null;
        }
        if (type.isArray()) {
            final Class<?> componentType = type.getComponentType();
            return componentType;
        }
        return null;
    }

    @Programmatic
    public static Class<?> inferFromGenericParamType(
            final Class<?> parameterType,
            final Type genericParameterType) {

        if(!isCollectionType(parameterType)) {
            return null;
        }

        if(genericParameterType instanceof ParameterizedType) {
            final ParameterizedType parameterizedType = (ParameterizedType) genericParameterType;
            final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if(actualTypeArguments.length == 1) {
                final Type actualTypeArgument = actualTypeArguments[0];
                if(actualTypeArgument instanceof Class) {
                    final Class<?> actualType = (Class<?>) actualTypeArgument;
                    return actualType;
                }
            }
        }
        return null;
    }

    /**
     * @return true means that either {@link CollectionUtils#inferFromGenericParamType(Class, Type)} or {@link TypeOfFacet.Util#inferFromArrayType(FacetHolder, Class, SpecificationLoader)} will return a non-null value.
     */
    public static boolean isParamCollection(
            final Class<?> parameterType,
            final Type genericParameterType) {
        return inferFromGenericParamType(parameterType, genericParameterType) != null || inferFromArrayType(parameterType) != null;
    }
}
