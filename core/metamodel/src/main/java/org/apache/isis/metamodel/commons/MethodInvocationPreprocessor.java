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

package org.apache.isis.metamodel.commons;

import static org.apache.isis.commons.internal.base._NullSafe.isEmpty;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.collections._Arrays;
import org.apache.isis.commons.internal.collections._Collections;

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

    public static Object invoke(Method method, Object targetPojo, Object[] executionParameters)
            throws IllegalAccessException, InvocationTargetException {

        if (isEmpty(executionParameters)) {
            return method.invoke(targetPojo, executionParameters);
        }

        final Class<?>[] parameterTypes = method.getParameterTypes();
        final Object[] adaptedExecutionParameters = new Object[executionParameters.length];

        int i=0;

        for(Object param : executionParameters) {
            adaptedExecutionParameters[i] = adapt(param, parameterTypes[i]);
            ++i;
        }

        try {
            return method.invoke(targetPojo, adaptedExecutionParameters);
        } catch (IllegalArgumentException e) {
            throw verboseArgumentException(parameterTypes, adaptedExecutionParameters, e);
        }
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
        
        final StringBuilder sb = new StringBuilder();
        
        for(int j=0;j<parameterTypes.length;++j) {
            final Class<?> parameterType = parameterTypes[j];
            final Object parameterValue = adaptedExecutionParameters[j];
        
            sb.append(String.format("expected-param-type[%d]: %s, got %s\n", 
                    j, parameterType.getName(), parameterValue.getClass().getName()));
        }
        
        // re-throw more verbose
        return new IllegalArgumentException(sb.toString(), e);
    }

    
}
