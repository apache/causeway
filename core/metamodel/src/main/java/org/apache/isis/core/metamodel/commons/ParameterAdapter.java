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
package org.apache.isis.core.metamodel.commons;

import java.lang.reflect.Executable;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.springframework.lang.Nullable;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal._Constants;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Arrays;
import org.apache.isis.commons.internal.collections._Collections;

import lombok.NonNull;
import lombok.val;

/**
 * Helper interface for {@link CanonicalInvoker}.
 */
@FunctionalInterface
public interface ParameterAdapter {

    // -- INTERFACE

    /**
     * Replaces {@code parameterValue} (if required) to be conform with the {@code parameterType}.
     */
    <T> T adaptToType(final Class<T> parameterType, Object parameterValue);

    // -- UTILITY

    default Object[] adaptAll(
            final @NonNull Executable executable,
            final @Nullable Object[] executionParameters) {
        final int paramCount = executable.getParameterCount();
        if(paramCount==0) {
            return _Constants.emptyObjects;
        }
        val parameterTypes = executable.getParameterTypes();
        val adaptedExecutionParameters = new Object[paramCount];
        for(int i=0; i<paramCount; ++i) {
            val origParam = _Arrays.get(executionParameters, i).orElse(null);
            adaptedExecutionParameters[i] = adaptToType(parameterTypes[i], origParam);
        }
        return adaptedExecutionParameters;
    }

    // -- DEFAULT IMPL

    static ParameterAdapter DEFAULT = new Default();

    static class Default implements ParameterAdapter {

        @Override
        public <T> T adaptToType(final Class<T> parameterType, final Object parameterValue) {
            return _Casts.uncheckedCast(_adaptToType(parameterType, parameterValue));
        }

        private Object _adaptToType(final Class<?> parameterType, final Object parameterValue) {

            var obj = parameterValue;

            if(obj==null) {
                return parameterType.isPrimitive()
                        ? ClassUtil.defaultByPrimitive.get(parameterType)
                        : null;
            }

            if(parameterType == Can.class) {
                if(obj instanceof Can) {
                    return obj;
                }
                return Can.ofStream(_NullSafe.streamAutodetect(obj));
            }

            if(obj instanceof Can) {
                obj = ((Can<?>)obj).toList();
            }

            if(_Arrays.isArrayType(parameterType)) {
                final Class<?> elementType = _Arrays.inferComponentType(parameterType).orElse(null);
                if(elementType==null) {
                    return obj;
                }
                @SuppressWarnings("rawtypes") final List list = (List)obj;
                return _Arrays.toArray(_Casts.uncheckedCast(list), elementType);
            }

            // allow no side effects on Collection arguments
            if(Collection.class.equals(parameterType)) {
                return _Collections.asUnmodifiableCollection((List<?>)obj);
            }

            // allow no side effects on List arguments
            if(List.class.equals(parameterType)) {
                return _Collections.asUnmodifiableList((List<?>)obj);
            }

            // adapt as Set (unmodifiable)
            if(Set.class.equals(parameterType)) {
                return _Collections.asUnmodifiableSet((List<?>)obj);
            }

            // adapt as SortedSet (unmodifiable)
            if(SortedSet.class.equals(parameterType)) {
                return _Collections.asUnmodifiableSortedSet((List<?>)obj);
            }

            return obj;
        }

    }

}
