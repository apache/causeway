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
package org.apache.causeway.core.metamodel._testing;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.core.metamodel.commons.CanBeVoid;
import org.apache.causeway.core.metamodel.facetapi.MethodRemover;

import lombok.Getter;
import lombok.Setter;
import lombok.Value;

public class MethodRemover_forTesting
implements MethodRemover {

    @Value
    static class RemoveMethodArgs {
        public String methodName;
        public Class<?> returnType;
        public Class<?>[] parameterTypes;
    }

    @Getter
    private final List<RemoveMethodArgs> removeMethodArgsCalls = new ArrayList<>();

    @Override
    public void removeMethod(final String methodName, final Class<?> returnType, final Class<?>[] parameterTypes) {
        removeMethodArgsCalls.add(new RemoveMethodArgs( methodName, returnType, parameterTypes));
    }

    @Getter
    private final List<ResolvedMethod> removedMethodMethodCalls = new ArrayList<ResolvedMethod>();

    @Override
    public void removeMethod(final @Nullable ResolvedMethod method) {
        if(method==null) return;
        removedMethodMethodCalls.add(method);
    }

    @Setter
    private List<ResolvedMethod> removeMethodsReturn;

    @Value
    static class RemoveMethodsArgs {
        public String prefix;
        public Class<?> returnType;
        public CanBeVoid canBeVoid;
        public int paramCount;
    }

    @Override
    public void removeMethods(final Predicate<ResolvedMethod> filter, final Consumer<ResolvedMethod> onRemoval) {
        removeMethodArgsCalls.add(new RemoveMethodArgs("", void.class, new Class[0]));
    }

    @Override
    public Can<ResolvedMethod> snapshotMethodsRemaining() {
        // creates a defensive copy, but as far as I know is not thread-safe
        return Can.ofStream(removedMethodMethodCalls.stream());
    }

}
