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
import java.util.Set;
import java.util.SortedSet;

import org.apache.isis.core.commons.internal.base._Casts;
import org.apache.isis.core.commons.internal.base._NullSafe;
import org.apache.isis.core.commons.internal.collections._Arrays;
import org.apache.isis.core.commons.internal.collections._Collections;
import org.apache.isis.core.commons.internal.reflection._Reflect;

import static org.apache.isis.core.commons.internal.base._NullSafe.isEmpty;

import lombok.val;

/**
 * Utility for method invocation pre-processing.
 * <p>
 * For a given array of parameters, we intercept and adapt those,
 * that are not compatible with the expected target parameter type.
 * </p>
 * <p>
 * By now we do this for collection parameter types List, Set, SortedSet, Collection and Arrays.
 * </p>
 */
public class MethodInvocationPreprocessor {

    public static <T> T invoke(Constructor<T> constructor, Object[] executionParameters)
            throws IllegalAccessException, InvocationTargetException, InstantiationException {

        val adaptedExecutionParameters = preprocess(constructor, executionParameters);
        try {
            // this utility supports effective private constructors as well
            return _Reflect.invokeConstructor(constructor, adaptedExecutionParameters);
        } catch (IllegalArgumentException e) {
            throw verboseArgumentException(constructor.getParameterTypes(), adaptedExecutionParameters, e);
        }
    }
    
    public static Object invoke(Method method, Object targetPojo, Object[] executionParameters)
            throws IllegalAccessException, InvocationTargetException {

        val adaptedExecutionParameters = preprocess(method, executionParameters);

        try {
            return method.invoke(targetPojo, adaptedExecutionParameters);
        } catch (IllegalArgumentException e) {
            throw verboseArgumentException(method.getParameterTypes(), adaptedExecutionParameters, e);
        }
    }
    
    private static Object[] preprocess(Executable executable, Object[] executionParameters) {
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
     * @return
     */

    private static Object adapt(Object obj, Class<?> parameterType) {

        if(obj==null) {
            return null;
        }

        if(_Arrays.isArrayType(parameterType)) {
            final Class<?> componentType = _Arrays.inferComponentTypeIfAny(parameterType);
            if(componentType==null) {
                return obj;
            }
            @SuppressWarnings("rawtypes") final List list = (List)obj;
            return _Arrays.toArray(_Casts.uncheckedCast(list), componentType);
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

    private static IllegalArgumentException verboseArgumentException(
            Class<?>[] parameterTypes, 
            Object[] adaptedExecutionParameters,
            IllegalArgumentException e) {

        val sb = new StringBuilder();
        
        final int expectedParamCount = _NullSafe.size(parameterTypes);
        final int actualParamCount = _NullSafe.size(adaptedExecutionParameters);
        if(expectedParamCount!=actualParamCount) {
            sb.append(String.format("param-count mismatch: expected %d, got %d\n", 
                    expectedParamCount, actualParamCount));
        } else {
            sb.append("expected param type mismatch\n");
        }
        
        for(int j=0;j<parameterTypes.length;++j) {
            final Class<?> parameterType = parameterTypes[j];
            final String parameterValueTypeLiteral = _Arrays.get(adaptedExecutionParameters, j)
                    .map(Object::getClass)
                    .map(Class::getName)
                    .orElse("missing or null");

            sb.append(String.format("param-type[%d]: '%s', got '%s'\n", 
                    j, parameterType.getName(), parameterValueTypeLiteral));
        }
        

        // re-throw more verbose
        return new IllegalArgumentException(sb.toString(), e);
    }


}
