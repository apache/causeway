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

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.facets.MethodLiteralConstants;

import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MethodUtil {

    public static void invoke(final Collection<Method> methods, final Object object) {
        methods.forEach(method->MethodExtensions.invoke(method, object));
    }

    public static void removeMethods(
            Set<Method> methods,
            Predicate<Method> filter,
            Consumer<Method> onMatch) {

        methods.removeIf(method -> { 
            val doRemove = filter.test(method);
            if(doRemove) {
                onMatch.accept(method);
            }
            return doRemove;
        });
        
    }

    public static boolean isNotStatic(final Method method) {
        return !isStatic(method);
    }

    public static boolean isStatic(final Method method) {
        final int modifiers = method.getModifiers();
        return Modifier.isStatic(modifiers);
    }

    public static boolean isPublic(Member method) {
        final int modifiers = method.getModifiers();
        return Modifier.isPublic(modifiers);
    }
    
    @UtilityClass
    public static class Predicates {
        
        public static Predicate<Method> paramCount(final int n) {
            return method -> method.getParameterCount() == n;
        }
        
        public static Predicate<Method> matchParamTypes(
                final int paramIndexOffset, 
                final Can<Class<?>> matchingParamTypes) {
            return method -> {
                // check params (if required)

                if(matchingParamTypes.isEmpty()) {
                    return true;
                }
                
                if(method.getParameterCount()<(paramIndexOffset+matchingParamTypes.size())) {
                    return false;
                }
                
                final Class<?>[] parameterTypes = method.getParameterTypes();
                
                for (int c = 0; c < matchingParamTypes.size(); c++) {
                    val left = parameterTypes[paramIndexOffset + c];
                    val right = matchingParamTypes.getElseFail(paramIndexOffset);
                    
                    if(!Objects.equals(left, right)) {
                        return false;
                    }
                }
                
                return true;
                
            };
        }
        
        /**
         * @param methodName
         * @param returnType
         * @param paramTypes
         * @return whether the method under test matches the given signature
         */
        public static Predicate<Method> signature(
                final String methodName, 
                final Class<?> returnType, 
                final Class<?>[] paramTypes) {

            return method -> {

                if (!isPublic(method)) {
                    return false;
                }

                if (isStatic(method)) {
                    return false;
                }

                // check for name
                if (!method.getName().equals(methodName)) {
                    return false;
                }

                // check for return type
                if (returnType != null && returnType != method.getReturnType()) {
                    return false;
                }

                // check params (if required)
                if (paramTypes != null) {
                    final Class<?>[] parameterTypes = method.getParameterTypes();
                    if (paramTypes.length != parameterTypes.length) {
                        return false;
                    }

                    for (int c = 0; c < paramTypes.length; c++) {
                        if ((paramTypes[c] != null) && (paramTypes[c] != parameterTypes[c])) {
                            return false;
                        }
                    }
                }
                
                return true;
            };
            
        }
        
        /**
         * 
         * @param prefix
         * @param returnType
         * @param canBeVoid
         * @param paramCount
         * @return whether the method under test matches the given constraints
         */
        public static Predicate<Method> prefixed(
                String prefix, Class<?> returnType, CanBeVoid canBeVoid, int paramCount) {
            
            return method -> {
            
                if (MethodUtil.isStatic(method)) {
                    return false;
                }
                if(!method.getName().startsWith(prefix)) {
                    return false;
                }
                if(method.getParameterTypes().length != paramCount) {
                    return false;
                }
                val type = method.getReturnType();
                if(!ClassExtensions.isCompatibleAsReturnType(returnType, canBeVoid, type)) {
                    return false;
                }
                
                return true;
                
            };
            
        }
        
        public static Predicate<Method> getter(Class<?> returnType) {
            return prefixed(MethodLiteralConstants.GET_PREFIX, returnType, CanBeVoid.FALSE, 0);
        }
        
        
    }
    
}
