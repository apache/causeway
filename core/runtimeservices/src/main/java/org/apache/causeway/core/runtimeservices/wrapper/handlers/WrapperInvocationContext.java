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
package org.apache.causeway.core.runtimeservices.wrapper.handlers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.concurrent.Callable;

import org.apache.causeway.applib.services.wrapper.control.SyncControl;

@RequiredArgsConstructor
@Getter
public class WrapperInvocationContext {

    /**
     * Either a domain object or a mixin.
     */
    final Object targetPojo;
    /**
     * Not applicable if a domain object.
     */
    final Object mixeePojo;
    final SyncControl syncControl;

    private static final ThreadLocal<WrapperInvocationContext> THREAD_LOCAL = new ThreadLocal<>();

    public void run(Runnable runnable) {
        THREAD_LOCAL.set(this);
        try {
            runnable.run();
        } finally {
            THREAD_LOCAL.remove();
        }
    }
    @SneakyThrows
    public <T> T call(Callable<T> callable) {
        THREAD_LOCAL.set(this);
        try {
            return callable.call();
        } finally {
            THREAD_LOCAL.remove();
        }
    }
}
