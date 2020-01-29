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

package org.apache.isis.core.internaltestsupport.jmocking;

import java.lang.reflect.Method;

import org.hamcrest.Description;
import org.jmock.api.Action;
import org.jmock.api.Invocation;

public final class InjectIntoJMockAction implements Action {
    @Override
    public void describeTo(final Description description) {
        description.appendText("inject self");
    }

    // x.injectInto(y) ---> y.setXxx(x)
    @Override
    public Object invoke(final Invocation invocation) throws Throwable {
        final Object injectable = invocation.getInvokedObject();
        final Object toInjectInto = invocation.getParameter(0);
        final Method[] methods = toInjectInto.getClass().getMethods();
        for (final Method method : methods) {
            if (!method.getName().startsWith("set")) {
                continue;
            }
            if (method.getParameterTypes().length != 1) {
                continue;
            }
            final Class<?> methodParameterType = method.getParameterTypes()[0];
            if (methodParameterType.isAssignableFrom(injectable.getClass())) {
                method.invoke(toInjectInto, injectable);
                break;
            }
        }
        return null;
    }

    /**
     * Factory
     */
    public static Action injectInto() {
        return new InjectIntoJMockAction();
    }

}
