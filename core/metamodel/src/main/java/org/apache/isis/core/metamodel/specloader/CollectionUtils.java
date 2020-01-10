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

import java.lang.reflect.Type;

import javax.annotation.Nullable;

import org.apache.isis.core.commons.internal.collections._Arrays;
import org.apache.isis.core.commons.internal.collections._Collections;

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

    /**
     *
     * @param parameterType
     * @param genericParameterType
     * @return whether the parameter is a (collection or array) and has an infer-able element type
     */
    public static boolean isParamCollection(
            @Nullable final Class<?> parameterType,
            @Nullable final Type genericParameterType) {
        if(_Arrays.inferComponentTypeIfAny(parameterType) != null) {
            return true;
        }
        if(_Collections.inferElementTypeIfAny(parameterType, genericParameterType)!=null) {
            return true;
        }
        return false;
    }


}
