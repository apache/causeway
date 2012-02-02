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

package org.apache.isis.viewer.wicket.model.util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public final class ClassLoaders {

    private static Map<String, Class<?>> primitives = Maps.newHashMap();

    static {
        @SuppressWarnings("unchecked")
        final List<Class> primitiveClasses = Arrays.<Class> asList(boolean.class, byte.class, short.class, int.class, long.class, float.class, double.class, char.class);
        for (final Class<?> cls : primitiveClasses) {
            primitives.put(cls.getName(), cls);
        }
    }

    public static Class<?> forName(final ObjectSpecification noSpec) {
        final String fullName = noSpec.getFullIdentifier();
        return forName(fullName);
    }

    public static Class<?> forName(final String fullName) {
        final Class<?> primitiveCls = primitives.get(fullName);
        if (primitiveCls != null) {
            return primitiveCls;
        }
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(fullName);
        } catch (final ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Class<?> forNameElseNull(final String fullName) {
        if (fullName == null) {
            return null;
        }
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(fullName);
        } catch (final ClassNotFoundException e) {
            return null;
        }
    }

}
