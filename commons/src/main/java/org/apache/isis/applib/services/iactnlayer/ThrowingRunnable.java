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
package org.apache.isis.applib.services.iactnlayer;

import java.util.concurrent.Callable;

import org.apache.isis.commons.functional.Result;

import lombok.NonNull;

/**
 * Similar to a {@link Runnable}, except that it can also throw exceptions.
 *
 * @since 2.x [@index}
 */
@FunctionalInterface
public interface ThrowingRunnable {


    // -- INTERFACE

    void run() throws Exception;

    // -- UTILITY

    static Callable<Void> toCallable(final @NonNull ThrowingRunnable runnable) {
        return ()->{
            runnable.run();
            return null;
        };
    }

    static Result<Void> resultOf(final @NonNull ThrowingRunnable runnable) {
        return Result.of(toCallable(runnable));
    }
}
