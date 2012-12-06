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

import java.lang.reflect.Method;
import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;

public final class AdapterInvokeUtils {

    private AdapterInvokeUtils() {
    }

    public static Object invoke(final Method method, final ObjectAdapter adapter) {
        return InvokeUtils.invoke(method, AdapterUtils.unwrap(adapter));
    }

    public static Object invoke(final Method method, final ObjectAdapter adapter, final Object arg0) {
        final Object[] args = new Object[1];
        args[0] = arg0;
        return InvokeUtils.invoke(method, AdapterUtils.unwrap(adapter), args);
    }

    public static void invoke(final List<Method> methods, final ObjectAdapter adapter) {
        InvokeUtils.invoke(methods, AdapterUtils.unwrap(adapter));
    }

    public static Object invoke(final Method method, final ObjectAdapter adapter, final ObjectAdapter arg0Adapter) {
        return InvokeUtils.invoke(method, AdapterUtils.unwrap(adapter), new Object[] { AdapterUtils.unwrap(arg0Adapter) });
    }

    public static Object invoke(final Method method, final ObjectAdapter adapter, final ObjectAdapter[] argumentAdapters) {
        return InvokeUtils.invoke(method, AdapterUtils.unwrap(adapter), AdapterUtils.unwrap(argumentAdapters));
    }

}
