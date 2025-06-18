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
package org.apache.causeway.core.runtime.wrap;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Objects;

import org.jspecify.annotations.NonNull;

import org.apache.causeway.applib.services.wrapper.WrappingObject;
import org.apache.causeway.applib.services.wrapper.control.ExecutionMode;
import org.apache.causeway.commons.internal._Constants;

public interface WrapperInvocationHandler extends InvocationHandler {
    
    ClassMetaData classMetaData();
    
    Object invoke(WrapperInvocation wrapperInvocation) throws Throwable;
    
    @Override
    default Object invoke(Object target, Method method, Object[] args) throws Throwable {
        return invoke(WrapperInvocation.of(target, method, args));
    }
    
    public record ClassMetaData(
            /** underlying class that is to be proxied */
            Class<?> pojoClass,

            Method equalsMethod,
            Method hashCodeMethod,
            Method toStringMethod) {
        
        public static ClassMetaData of(
                final @NonNull Object pojo) {

            var pojoClass = pojo.getClass();
            try {
                var equalsMethod = pojoClass.getMethod("equals", _Constants.classesOfObject);
                var hashCodeMethod = pojoClass.getMethod("hashCode", _Constants.emptyClasses);
                var toStringMethod = pojoClass.getMethod("toString", _Constants.emptyClasses);
                
                return new WrapperInvocationHandler
                        .ClassMetaData(pojoClass, equalsMethod, hashCodeMethod, toStringMethod);
                
            } catch (final NoSuchMethodException e) {
                // ///CLOVER:OFF
                throw new RuntimeException("An Object method could not be found: " + e.getMessage());
                // ///CLOVER:ON
            }
        }
        
        public boolean isObjectMethod(final Method method) {
            return toStringMethod().equals(method) 
                    || hashCodeMethod().equals(method) 
                    || equalsMethod().equals(method);
        }
        
    }
    
    public record WrapperInvocation(
        WrappingObject.Origin origin,
        Method method,
        Object[] args) {

        static WrapperInvocation of(Object target, Method method, Object[] args) {
            Objects.requireNonNull(target);
            var origin = target instanceof WrappingObject wrappingObject 
                    ? WrappingObject.getOrigin(wrappingObject)
                    : WrappingObject.Origin.fallback(target);
            return new WrapperInvocation(origin, method, args);
        }
        
        public boolean shouldEnforceRules() {
            return !origin().syncControl().getExecutionModes().contains(ExecutionMode.SKIP_RULE_VALIDATION);
        }

        public boolean shouldExecute() {
            return !origin().syncControl().getExecutionModes().contains(ExecutionMode.SKIP_EXECUTION);
        }
    }
    
}
