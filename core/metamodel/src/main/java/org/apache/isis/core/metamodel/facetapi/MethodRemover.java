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
package org.apache.isis.core.metamodel.facetapi;

import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.commons.MethodUtil;

/**
 * Removes the methods from further processing by subsequent {@link Facet}s.
 */
public interface MethodRemover {

    /**
     * Locate all methods (that the implementation should somehow know about)
     * that match the criteria and remove them from the implementation's list so
     * that they are not considered for subsequent scans.
     * @param filter - predefined ones are available with MethodUtil
     * @param onRemoval receives any methods that were removed
     */
    void removeMethods(
            Predicate<Method> removeIf,
            Consumer<Method> onRemoval);

    /** variant with noop consumer */
    default void removeMethods(
            Predicate<Method> removeIf) {
        removeMethods(removeIf, removedMethod -> {});
    }

    /**
     * Locate all methods (that the implementation should somehow know about)
     * that match the criteria and remove them from the implementation's list so
     * that they are not considered for subsequent scans.
     *
     */
    default void removeMethod(
            String methodName,
            Class<?> returnType,
            Class<?>[] parameterTypes) {

        removeMethods(MethodUtil.Predicates.signature(methodName, returnType, parameterTypes));
    }

    void removeMethod(Method method);
    
    /**
     * Returns a defensive copy of the current internal state.
     * @apiNote introduced for debugging purposes
     */
    Can<Method> snapshotMethodsRemaining();

    // -- NOOP IMPLEMENTATION

    public static final MethodRemover NOOP = new MethodRemover() {

        @Override
        public void removeMethod(final Method method) {
        }

        @Override
        public void removeMethods(Predicate<Method> filter, Consumer<Method> onRemoval) {
        }

        @Override
        public Can<Method> snapshotMethodsRemaining() {
            return Can.empty();
        }

    };

}
