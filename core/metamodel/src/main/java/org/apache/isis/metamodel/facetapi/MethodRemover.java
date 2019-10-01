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

package org.apache.isis.metamodel.facetapi;

import java.lang.reflect.Method;
import java.util.function.Consumer;

import org.apache.isis.metamodel.methodutils.MethodScope;

/**
 * Removes the methods from further processing by subsequent {@link Facet}s.
 */
public interface MethodRemover {

    /**
     * Locate all methods (that the implementation should somehow know about)
     * that match the criteria and remove them from the implementation's list so
     * that they are not considered for subsequent scans.
     *
     * @param methodScope
     *            - whether looking for <tt>static</tt> (class) or
     *            instance-level methods.
     * @param onRemoval receives any methods that were removed
     */
    void removeMethods(
            MethodScope methodScope, 
            String prefix, 
            Class<?> returnType, 
            boolean canBeVoid, 
            int paramCount,
            Consumer<Method> onRemoval
            );

    default void removeMethods(
            MethodScope methodScope, 
            String prefix, 
            Class<?> returnType, 
            boolean canBeVoid, 
            int paramCount) {
        
        removeMethods(methodScope, prefix, returnType, canBeVoid, paramCount, whatever -> {});
    }
    
    
    /**
     * Locate all methods (that the implementation should somehow know about)
     * that match the criteria and remove them from the implementation's list so
     * that they are not considered for subsequent scans.
     *
     * @param methodScope
     *            - whether looking for <tt>static</tt> (class) or
     *            instance-level methods.
     */
    void removeMethod(MethodScope methodScope, String methodName, Class<?> returnType, Class<?>[] parameterTypes);

    void removeMethod(Method method);

}
