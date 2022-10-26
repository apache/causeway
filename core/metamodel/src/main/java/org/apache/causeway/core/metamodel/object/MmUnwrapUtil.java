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
package org.apache.causeway.core.metamodel.object;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.collections._Arrays;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.collections._Sets;

import lombok.NonNull;
import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class MmUnwrapUtil {

    // -- SINGLE

    @Nullable
    public static Object single(final @Nullable ManagedObject adapter) {
        return ManagedObjects.isSpecified(adapter)
                ? adapter.getPojo()
                : null;
    }

    @Nullable
    public static String singleAsStringOrElse(final @Nullable ManagedObject adapter, final @Nullable String orElse) {
        final Object obj = MmUnwrapUtil.single(adapter);
        if (obj == null) {
            return null;
        }
        if (obj instanceof String) {
            return (String) obj;
        }
        return orElse;
    }

    // -- AS ARRAY

    @Nullable
    public static Object[] multipleAsArray(final @NonNull Can<ManagedObject> adapters) {
        val unwrappedObjects = _Arrays.mapCollection(adapters.toList(), MmUnwrapUtil::single);
        return unwrappedObjects;
    }

    @Nullable
    public static Object[] multipleAsArray(final @Nullable Collection<ManagedObject> adapters) {
        val unwrappedObjects = _Arrays.mapCollection(adapters, MmUnwrapUtil::single);
        return unwrappedObjects;
    }

    @Nullable
    public static Object[] multipleAsArray(final @Nullable ManagedObject[] adapters) {
        val unwrappedObjects = _Arrays.map(adapters, MmUnwrapUtil::single);
        return unwrappedObjects;
    }

    // -- AS LIST

    /**
     *
     * @param adapters
     * @return non-null, unmodifiable
     */
    public static List<Object> multipleAsList(final @Nullable Collection<? extends ManagedObject> adapters) {
        if (adapters == null) {
            return Collections.emptyList();
        }
        return adapters.stream()
                .map(MmUnwrapUtil::single)
                .collect(_Lists.toUnmodifiable());
    }

    /**
     *
     * @param adapters
     * @return non-null, unmodifiable
     */
    public static List<Object> multipleAsList(final @Nullable Can<? extends ManagedObject> adapters) {
        if (adapters == null) {
            return Collections.emptyList();
        }
        return adapters.stream()
                .map(MmUnwrapUtil::single)
                .collect(_Lists.toUnmodifiable());
    }


    /**
     *
     * @param adapters
     * @return non-null, unmodifiable
     */
    public static Set<Object> multipleAsSet(final @Nullable Collection<? extends ManagedObject> adapters) {
        if (adapters == null) {
            return Collections.emptySet();
        }
        return adapters.stream()
                .map(MmUnwrapUtil::single)
                .collect(_Sets.toUnmodifiable());
    }

}