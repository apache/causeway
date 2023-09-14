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
package org.apache.causeway.core.metamodel.facetapi;

import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.core.metamodel.commons.MethodUtil;

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
            Predicate<ResolvedMethod> removeIf,
            Consumer<ResolvedMethod> onRemoval);

    /** variant with noop consumer */
    default void removeMethods(
            final Predicate<ResolvedMethod> removeIf) {
        removeMethods(removeIf, removedMethod -> {});
    }

    /**
     * Locate all methods (that the implementation should somehow know about)
     * that match the criteria and remove them from the implementation's list so
     * that they are not considered for subsequent scans.
     *
     */
    default void removeMethod(
            final String methodName,
            final Class<?> returnType,
            final Class<?>[] parameterTypes) {

        removeMethods(MethodUtil.Predicates.signature(methodName, returnType, parameterTypes));
    }

    void removeMethod(ResolvedMethod method);

    /**
     * Returns a defensive copy of the current internal state.
     * @apiNote introduced for debugging purposes
     */
    Can<ResolvedMethod> snapshotMethodsRemaining();

    // -- NOOP IMPLEMENTATION

    public static final MethodRemover NOOP = new MethodRemover() {

        @Override
        public void removeMethod(final ResolvedMethod method) {
        }

        @Override
        public void removeMethods(final Predicate<ResolvedMethod> filter, final Consumer<ResolvedMethod> onRemoval) {
        }

        @Override
        public Can<ResolvedMethod> snapshotMethodsRemaining() {
            return Can.empty();
        }

    };

}
