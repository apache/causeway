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

package org.apache.isis.metamodel.specloader.traverser;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Abstract base class factoring out common functionality for helper methods
 * that extract parameterized types.
 *
 */
abstract class TypeExtractorAbstract implements Iterable<Class<?>> {
    private final Method method;
    private final List<Class<?>> classes = new ArrayList<Class<?>>();

    public TypeExtractorAbstract(final Method method) {
        this.method = method;
    }

    protected void addParameterizedTypes(final Type... genericTypes) {
        for (final Type genericType : genericTypes) {
            if (genericType instanceof ParameterizedType) {
                final ParameterizedType parameterizedType = (ParameterizedType) genericType;
                final Type[] typeArguments = parameterizedType.getActualTypeArguments();
                for (final Type type : typeArguments) {
                    if (type instanceof Class) {
                        final Class<?> cls = (Class<?>) type;
                        add(cls);
                    }
                }
            }
        }
    }

    /**
     * Adds to {@link #getClasses() list of classes}, provided not {@link Void}.
     */
    protected void add(final Class<?> cls) {
        if (cls == void.class) {
            return;
        }
        classes.add(cls);
    }

    /**
     * The {@link Method} provided in the {@link #TypeExtractorAbstract(Method)
     * constructor.}
     */
    protected Method getMethod() {
        return method;
    }

    public List<Class<?>> getClasses() {
        return Collections.unmodifiableList(classes);
    }

    @Override
    public Iterator<Class<?>> iterator() {
        return getClasses().iterator();
    }
}
