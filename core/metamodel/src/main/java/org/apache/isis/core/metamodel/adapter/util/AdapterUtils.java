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

package org.apache.isis.core.metamodel.adapter.util;

import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;

public class AdapterUtils {

    private AdapterUtils() {
    }
    
    /**
     * @deprecated - use {@link ObjectAdapter.Util#exists(ObjectAdapter)}
     */
    @Deprecated
    public static boolean exists(final ObjectAdapter adapter) {
        return ObjectAdapter.Util.exists(adapter);
    }
    
    /**
     * @deprecated - use {@link ObjectAdapter.Util#wrappedEqual(ObjectAdapter, ObjectAdapter)}
     */
    @Deprecated
    public static boolean wrappedEqual(final ObjectAdapter adapter1, final ObjectAdapter adapter2) {
        return ObjectAdapter.Util.wrappedEqual(adapter1, adapter2);
    }

    /**
     * @deprecated - use {@link ObjectAdapter.Util#unwrap(ObjectAdapter)}
     */
    @Deprecated
    public static Object unwrap(final ObjectAdapter adapter) {
        return ObjectAdapter.Util.unwrap(adapter);
    }

    /**
     * @deprecated - use {@link ObjectAdapter.Util#unwrap(ObjectAdapter[])}
     */
    public static Object[] unwrap(final ObjectAdapter[] adapters) {
        return ObjectAdapter.Util.unwrap(adapters);
    }

    /**
     * @deprecated - use {@link ObjectAdapter.Util#unwrapT(List)}
     */
    @Deprecated
    public static <T> List<T> unwrap(final List<ObjectAdapter> adapters) {
        return ObjectAdapter.Util.unwrapT(adapters);
    }

    /**
     * @deprecated - use {@link ObjectAdapter.Util#titleString(ObjectAdapter)}
     */
    @Deprecated
    public static String titleString(final ObjectAdapter adapter) {
        return ObjectAdapter.Util.titleString(adapter);
    }

    /**
     * @deprecated - use {@link ObjectAdapter.Util#nullSafeEquals(Object, Object)}
     */
    @Deprecated
    public static boolean nullSafeEquals(final Object obj1, final Object obj2) {
        return ObjectAdapter.Util.nullSafeEquals(obj1, obj2);
    }

}
