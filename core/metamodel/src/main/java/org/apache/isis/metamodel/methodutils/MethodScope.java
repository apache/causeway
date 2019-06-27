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

package org.apache.isis.metamodel.methodutils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * As used in the <tt>findAndRemove...</tt> methods.
 */
public enum MethodScope {
    CLASS, OBJECT;

    public boolean isClass() {
        return this == CLASS;
    }

    public boolean isObject() {
        return this == OBJECT;
    }

    public boolean doesNotMatchScope(final Method method) {
        return !matchesScopeOf(method);
    }

    public boolean matchesScopeOf(final Method method) {
        return isStatic(method) == this.isClass();
    }

    public static MethodScope scopeFor(final Method method) {
        return isStatic(method) ? CLASS : OBJECT;
    }

    private static boolean isStatic(final Method method) {
        final int modifiers = method.getModifiers();
        final boolean isStatic = Modifier.isStatic(modifiers);
        return isStatic;
    }

}
