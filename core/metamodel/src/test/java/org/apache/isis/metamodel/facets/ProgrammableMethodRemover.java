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

import org.apache.isis.metamodel.facetapi.MethodRemover;
import org.apache.isis.metamodel.methodutils.MethodScope;

public class ProgrammableMethodRemover implements MethodRemover {

    // ////////////////////////////////////////////////////////////
    // removeMethod(...): void
    // ////////////////////////////////////////////////////////////

    static class RemoveMethodArgs {
        public RemoveMethodArgs(final MethodScope methodScope, final String methodName, final Class<?> returnType, final Class<?>[] parameterTypes) {
            this.methodScope = methodScope;
            this.methodName = methodName;
            this.returnType = returnType;
            this.parameterTypes = parameterTypes;
        }

        public MethodScope methodScope;
        public String methodName;
        public Class<?> returnType;
        public Class<?>[] parameterTypes;
    }

    private final List<RemoveMethodArgs> removeMethodArgsCalls = new ArrayList<RemoveMethodArgs>();

    @Override
    public void removeMethod(final MethodScope methodScope, final String methodName, final Class<?> returnType, final Class<?>[] parameterTypes) {
        removeMethodArgsCalls.add(new RemoveMethodArgs(methodScope, methodName, returnType, parameterTypes));
    }

    public List<RemoveMethodArgs> getRemoveMethodArgsCalls() {
        return removeMethodArgsCalls;
    }

    // ////////////////////////////////////////////////////////////
    // removeMethod(Method): void
    // ////////////////////////////////////////////////////////////

    private final List<Method> removeMethodMethodCalls = new ArrayList<Method>();

    @Override
    public void removeMethod(final Method method) {
        removeMethodMethodCalls.add(method);
    }

    public List<Method> getRemovedMethodMethodCalls() {
        return removeMethodMethodCalls;
    }

    // ////////////////////////////////////////////////////////////
    // removeMethods(...):List
    // ////////////////////////////////////////////////////////////

    private List<Method> removeMethodsReturn;

    public void setRemoveMethodsReturn(final List<Method> removeMethodsReturn) {
        this.removeMethodsReturn = removeMethodsReturn;
    }

    static class RemoveMethodsArgs {
        public RemoveMethodsArgs(final MethodScope methodScope, final String prefix, final Class<?> returnType, final boolean canBeVoid, final int paramCount) {
            this.methodScope = methodScope;
            this.prefix = prefix;
            this.returnType = returnType;
            this.canBeVoid = canBeVoid;
            this.paramCount = paramCount;
        }

        public MethodScope methodScope;
        public String prefix;
        public Class<?> returnType;
        public boolean canBeVoid;
        public int paramCount;
    }

    private final List<RemoveMethodsArgs> removeMethodsArgs = new ArrayList<RemoveMethodsArgs>();

    @Override
    public List<Method> removeMethods(final MethodScope methodScope, final String prefix, final Class<?> returnType, final boolean canBeVoid, final int paramCount) {
        removeMethodsArgs.add(new RemoveMethodsArgs(methodScope, prefix, returnType, canBeVoid, paramCount));
        return removeMethodsReturn;
    }

    // ////////////////////////////////////////////////////////////
    // removeMethods(List):void
    // ////////////////////////////////////////////////////////////

    @Override
    public void removeMethods(final List<Method> methods) {
        for (final Method method : methods) {
            removeMethod(method);
        }
    }

}
