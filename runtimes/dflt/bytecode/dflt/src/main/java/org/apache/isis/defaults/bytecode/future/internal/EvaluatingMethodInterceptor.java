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

package org.apache.isis.defaults.bytecode.future.internal;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.apache.isis.core.commons.futures.FutureResultFactory;

public final class EvaluatingMethodInterceptor<T> implements MethodInterceptor {

    private final FutureResultFactory<T> resultFactory;
    private T result;

    public EvaluatingMethodInterceptor(FutureResultFactory<T> resultFactory) {
        this.resultFactory = resultFactory;
    }

    @Override
    public synchronized Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        if (result == null) {
            result = resultFactory.getResult();
        }
        return method.invoke(result, args);
    }
}