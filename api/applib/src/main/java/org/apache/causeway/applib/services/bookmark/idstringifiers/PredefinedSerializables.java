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
package org.apache.causeway.applib.services.bookmark.idstringifiers;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.apache.causeway.applib.graph.tree.TreeState;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.commons.internal.collections._Sets;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PredefinedSerializables {

    /**
     * Whether given {@code cls} can be serialized ad-hoc,
     * which usually is true for simple value types,
     * but not per-se for domain object's (entities and viewmodels)
     * even if {@link Serializable}.
     * For the latter a more sophisticated - {@link Bookmark}
     * based - mechanism is required instead.
     */
    public boolean isPredefinedSerializable(final Class<?> cls) {
        if(!Serializable.class.isAssignableFrom(cls)) {
            return false;
        }
        // primitive ... boolean, byte, char, short, int, long, float, and double.
        if(cls.isPrimitive() || Number.class.isAssignableFrom(cls)) {
            return true;
        }
        // any non-scalar values could be problematic, so we are careful with wild-cards here
        if(cls.getName().startsWith("java.time.")) {
            return true;
        }
        if(cls.getName().startsWith("org.joda.time.")) {
            return true;
        }
        if(serializableFinalTypes.contains(cls)) {
            return true;
        }
        return serializableTypes.stream().anyMatch(t->t.isAssignableFrom(cls));
    }

    // -- HELPER

    private static final Set<Class<? extends Serializable>> serializableFinalTypes = _Sets.of(
            String.class, String[].class,
            Class.class, Class[].class,
            Character.class, Character[].class, char[].class,
            Boolean.class, Boolean[].class, boolean[].class,
            // Numbers
            Byte[].class, byte[].class,
            Short[].class, short[].class,
            Integer[].class, int[].class,
            Long[].class, long[].class,
            Float[].class, float[].class,
            Double[].class, double[].class
            );

    private static final List<Class<? extends Serializable>> serializableTypes = List.of(
            java.util.Date.class,
            java.sql.Date.class,
            Enum.class,
            Bookmark.class,
            TreeState.class
            );

}
