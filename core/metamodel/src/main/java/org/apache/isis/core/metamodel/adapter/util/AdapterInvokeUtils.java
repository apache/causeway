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

    public static void invokeAll(final List<Method> methods, final ObjectAdapter adapter) {
        InvokeUtils.invoke(methods, AdapterUtils.unwrap(adapter));
    }

    public static Object invoke(final Method method, final ObjectAdapter adapter) {
        return InvokeUtils.invoke(method, AdapterUtils.unwrap(adapter));
    }

    public static Object invoke(final Method method, final ObjectAdapter adapter, final Object arg0) {
        return InvokeUtils.invoke(method, AdapterUtils.unwrap(adapter), new Object[] {arg0});
    }

    public static Object invoke(final Method method, final ObjectAdapter adapter, final ObjectAdapter arg0Adapter) {
        return invoke(method, adapter, AdapterUtils.unwrap(arg0Adapter));
    }

    public static Object invoke(final Method method, final ObjectAdapter adapter, final ObjectAdapter[] argumentAdapters) {
        return InvokeUtils.invoke(method, AdapterUtils.unwrap(adapter), AdapterUtils.unwrap(argumentAdapters));
    }
    
    /**
     * Invokes the method, adjusting arguments as required.
     * 
     * <p>
     * That is:
     * <ul>
     * <li>if the method declares parameters but no arguments are provided, then will provide 'null' defaults for these.
     * <li>if the method does not declare parameters but arguments were provided, then will ignore those argumens.
     * </ul>
     */
    public static Object invokeWithDefaults(final Method method, final ObjectAdapter adapter, final ObjectAdapter[] argumentAdapters) {
        final int numParams = method.getParameterTypes().length;
        ObjectAdapter[] adapters;
        
        if(argumentAdapters == null || argumentAdapters.length == 0) {
            adapters = new ObjectAdapter[numParams];
        } else if(numParams == 0) {
            // ignore any arguments, even if they were supplied.
            // eg used by contributee actions, but 
            // underlying service 'default' action declares no params 
            adapters = new ObjectAdapter[0];
        } else if(argumentAdapters.length == numParams){
            adapters = argumentAdapters;
        } else {
            throw new IllegalArgumentException("Method has " + numParams + " params but " + argumentAdapters.length + " arguments provided");
        }

        return AdapterInvokeUtils.invoke(method, adapter, adapters);
    }

}
