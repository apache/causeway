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

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;

public class AdapterUtils {

    private AdapterUtils() {
    }

    public static boolean exists(final ObjectAdapter adapter) {
        return adapter != null && adapter.getObject() != null;
    }

    public static boolean wrappedEqual(final ObjectAdapter adapter1, final ObjectAdapter adapter2) {
        final boolean defined1 = exists(adapter1);
        final boolean defined2 = exists(adapter2);
        if (defined1 && !defined2) {
            return false;
        }
        if (!defined1 && defined2) {
            return false;
        }
        if (!defined1 && !defined2) {
            return true;
        } // both null
        return adapter1.getObject().equals(adapter2.getObject());
    }

    public static Object unwrap(final ObjectAdapter adapter) {
        return adapter != null ? adapter.getObject() : null;
    }

    public static Object[] unwrap(final ObjectAdapter[] adapters) {
        if (adapters == null) {
            return null;
        }
        final Object[] unwrappedObjects = new Object[adapters.length];
        int i = 0;
        for (final ObjectAdapter adapter : adapters) {
            unwrappedObjects[i++] = unwrap(adapter);
        }
        return unwrappedObjects;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> unwrap(final List<ObjectAdapter> adapters) {
        final List<T> list = new ArrayList<T>();
        for (final ObjectAdapter adapter : adapters) {
            list.add((T) unwrap(adapter));
        }
        return list;
    }

    public static String titleString(final ObjectAdapter adapter) {
        return adapter != null ? adapter.titleString() : "";
    }

    public static boolean nullSafeEquals(final Object obj1, final Object obj2) {
        if (obj1 == null && obj2 == null) {
            return true;
        }
        if (obj1 == null || obj2 == null) {
            return false;
        }
        if (obj1.equals(obj2)) {
            return true;
        }
        if (obj1 instanceof ObjectAdapter && obj2 instanceof ObjectAdapter) {
            final ObjectAdapter adapterObj1 = (ObjectAdapter) obj1;
            final ObjectAdapter adapterObj2 = (ObjectAdapter) obj2;
            return nullSafeEquals(adapterObj1.getObject(), adapterObj2.getObject());
        }
        return false;
    }

}
