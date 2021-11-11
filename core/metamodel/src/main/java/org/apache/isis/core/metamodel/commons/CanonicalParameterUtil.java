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

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Arrays;
import org.apache.isis.commons.internal.collections._Collections;
import org.apache.isis.commons.internal.reflection._Reflect;

import static org.apache.isis.commons.internal.base._NullSafe.isEmpty;

import lombok.NonNull;
import lombok.val;

/**
 * Utility for method invocation pre-processing.
 * <p>
 * For a given array of parameters, we intercept and adapt those,
 * that are not compatible with the expected target parameter type.
 * <p>
 * We do this for collection parameter types List, Set, SortedSet, Collection, Can and Arrays.
 */
public final class CanonicalParameterUtil {

    public static <T> T construct(final Constructor<T> constructor, final Object[] executionParameters) {
        val adaptedExecutionParameters = preprocess(constructor, executionParameters);

        // supports effective private constructors as well
        return _Reflect.invokeConstructor(constructor, adaptedExecutionParameters)
        .mapFailure(ex->toVerboseException(constructor, adaptedExecutionParameters, ex))
        .presentElseFail();
    }

    public static Object invoke(final Method method, final Object targetPojo, final Object[] executionParameters)
            throws IllegalAccessException, InvocationTargetException {

        val adaptedExecutionParameters = preprocess(method, executionParameters);

        // supports effective private methods as well
        return _Reflect.invokeMethodOn(method, targetPojo, adaptedExecutionParameters)
        .mapFailure(ex->toVerboseException(method, adaptedExecutionParameters, ex))
        .optionalElseFail()
        .orElse(null);
    }

    private static Object[] preprocess(final Executable executable, final Object[] executionParameters) {
        if (isEmpty(executionParameters)) {
            return executionParameters;
        }
        val parameterTypes = executable.getParameterTypes();
        val paramCount = parameterTypes.length;
        val adaptedExecutionParameters = new Object[paramCount];

        for(int i=0; i<paramCount; ++i) {
            val origParam = _Arrays.get(executionParameters, i).orElse(null);
            adaptedExecutionParameters[i] = adapt(origParam, parameterTypes[i]);
        }
        return adaptedExecutionParameters;
    }

    // -- OBJECT ADAPTER


    /**
     * Replaces obj (if required) to be conform with the parameterType
     * @param obj
     * @param parameterType
     */
    private static Object adapt(Object obj, final Class<?> parameterType) {

        if(obj==null) {
            return null;
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

    private static Throwable toVerboseException(
            final Executable executable,
            final Object[] adaptedExecutionParameters,
            final Throwable e) {

        final Class<?>[] parameterTypes = executable.getParameterTypes();
        final int expectedParamCount = _NullSafe.size(parameterTypes);
        final int actualParamCount = _NullSafe.size(adaptedExecutionParameters);
        if(expectedParamCount!=actualParamCount) {
            return new IllegalArgumentException(String.format(
                    "param-count mismatch: expected %d, got %d\n",
                    expectedParamCount, actualParamCount), e);
        }

        // if method or constructor was invoked with incompatible param types, then the Throwable
        // we receive here is of type IllegalArgumentException; in which case we can provide additional
        // information, but also at the expense of a potentially hiding the original cause, namely when the
        // IllegalArgumentException has a different origin and the param incompatibility check is a
        // false positive
        if(e instanceof IllegalArgumentException) {
            boolean paramTypeMismatchEncountered = false;
            val sb = new StringBuilder();
            for(int j=0;j<parameterTypes.length;++j) {
                final Class<?> parameterType = parameterTypes[j];
                val incompatible = !isValueCompatibleWithType(
                        _Arrays.get(adaptedExecutionParameters, j),
                        parameterTypes[j]);

                paramTypeMismatchEncountered = paramTypeMismatchEncountered
                        || incompatible;

                if(incompatible) {

                    final String parameterValueTypeLiteral = _Arrays.get(adaptedExecutionParameters, j)
                            .map(Object::getClass)
                            .map(Class::getName)
                            .orElse("missing or null");

                    final String expected = parameterType.getName();
                    final String actual = parameterValueTypeLiteral;

                    sb.append(String.format("param-type[%d]: expected '%s', got '%s'\n",
                        j, expected, actual));
                }
            }
            if(paramTypeMismatchEncountered) {
                sb.insert(0, String.format("expected param type mismatch in %s\n", executable));
                return new IllegalArgumentException(sb.toString(), e);
            }
        }

        // re-throw more verbose
        return e;
    }

    private static boolean isValueCompatibleWithType(
            final @NonNull Optional<Object> value,
            final @NonNull Class<?> type) {

        if(!value.isPresent()) {
            // null is not compatible with an expected primitive type
            // but null is compatible with any other expected type
            return !type.isPrimitive();
        }

        val runtimeType = value.get().getClass();

        if(ClassExtensions.equalsWhenBoxing(runtimeType, type)) {
            return true;
        }

        return type.isAssignableFrom(runtimeType);
    }


}
