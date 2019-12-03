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

package org.apache.isis.metamodel.facets;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.isis.metamodel.commons.CanBeVoid;
import org.apache.isis.metamodel.facetapi.MethodRemover;
import org.apache.isis.metamodel.methodutils.MethodScope;

import lombok.Getter;
import lombok.Setter;
import lombok.Value;

public class MethodRemoverForTesting implements MethodRemover {

    // ////////////////////////////////////////////////////////////
    // removeMethod(...): void
    // ////////////////////////////////////////////////////////////
    @Value
    static class RemoveMethodArgs {
        public MethodScope methodScope;
        public String methodName;
        public Class<?> returnType;
        public Class<?>[] parameterTypes;
    }

    @Getter
    private final List<RemoveMethodArgs> removeMethodArgsCalls = new ArrayList<>();

    @Override
    public void removeMethod(final MethodScope methodScope, final String methodName, final Class<?> returnType, final Class<?>[] parameterTypes) {
        removeMethodArgsCalls.add(new RemoveMethodArgs(methodScope, methodName, returnType, parameterTypes));
    }

    // ////////////////////////////////////////////////////////////
    // removeMethod(Method): void
    // ////////////////////////////////////////////////////////////

    @Getter
    private final List<Method> removedMethodMethodCalls = new ArrayList<Method>();

    @Override
    public void removeMethod(final Method method) {
        removedMethodMethodCalls.add(method);
    }

    // ////////////////////////////////////////////////////////////
    // removeMethods(...):List
    // ////////////////////////////////////////////////////////////

    @Setter
    private List<Method> removeMethodsReturn;


    @Value
    static class RemoveMethodsArgs {
        public MethodScope methodScope;
        public String prefix;
        public Class<?> returnType;
        public CanBeVoid canBeVoid;
        public int paramCount;
    }

    private final List<RemoveMethodsArgs> removeMethodsArgs = new ArrayList<>();

    @Override
    public void removeMethods(final MethodScope methodScope, final String prefix, final Class<?> returnType, final CanBeVoid canBeVoid, final int paramCount, Consumer<Method> onRemoval) {
        removeMethodsArgs.add(new RemoveMethodsArgs(methodScope, prefix, returnType, canBeVoid, paramCount));
    }


}
