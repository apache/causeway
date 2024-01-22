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
package org.apache.causeway.commons.functional;

import java.util.concurrent.Callable;

import lombok.SneakyThrows;

/**
 * Similar to a {@link Runnable}, except that it can also throw a checked {@link Exception}.
 *
 * @since 2.x {@index}
 */
@FunctionalInterface
public interface ThrowingRunnable {

    // -- INTERFACE

    void run() throws Exception;

    // -- VARIANTS

    /**
     * Does <b>not</b> silently swallow, wrap into RuntimeException,
     * or otherwise modify any exceptions of the wrapped {@link #run()} method.
     * @see lombok.SneakyThrows
     */
    @SneakyThrows
    default void runUncatched() {
        run();
    }

    /**
     * Does <b>not</b> silently swallow, wrap into RuntimeException,
     * or otherwise modify any exceptions of the wrapped {@link #run()} method.
     * @see lombok.SneakyThrows
     */
    @SneakyThrows
    default Void callUncatched() {
        run();
        return null;
    }

    // -- CONVERSION

    /**
     * The resulting {@link Runnable} does <b>not</b> silently swallow, wrap into RuntimeException,
     * or otherwise modify any exceptions of the wrapped {@link #run()} method.
     * @see lombok.SneakyThrows
     */
    default Runnable toRunnable() {
        return this::runUncatched;
    }

    /**
     * The resulting {@link Callable} does <b>not</b> silently swallow, wrap into RuntimeException,
     * or otherwise modify any exceptions of the wrapped {@link #run()} method.
     * @see lombok.SneakyThrows
     */
    default Callable<Void> toCallable() {
        return this::callUncatched;
    }

}
