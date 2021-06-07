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
package org.apache.isis.core.security.authentication.manager;

import java.util.concurrent.Callable;

import org.apache.isis.commons.functional.ThrowingRunnable;

import lombok.NonNull;
import lombok.SneakyThrows;

/**
 * Inversion of dependency pattern.
 * <p>
 * Introduced to allow the AuthenticationManager (module: security) access to the
 * InteractionFactory service (module: interaction),
 * which otherwise was not possible, due to Maven module dependencies (preventing circles).
 *
 * @apiNote This is a framework internal class and so does not constitute a formal API.
 *
 * @since 2.0 {@index}
 */
public interface AnonymousInteractionFactory {

    /**
     * Executes a block of code with anonymous credentials.
     *
     * @param runnable
     */
    void runAnonymous(@NonNull ThrowingRunnable runnable);

    /**
     * Executes a block of code with anonymous credentials.
     *
     * @param <R>
     * @param callable (non-null)
     */
    <R> R callAnonymous(@NonNull Callable<R> callable);


    // -- JUNIT SUPPORT

    /**
     * Returns a pass-through implementation, free of side-effects,
     * in support of simple JUnit tests.
     */
    static AnonymousInteractionFactory forTesting() {
        return new AnonymousInteractionFactory() {

            @Override @SneakyThrows
            public void runAnonymous(@NonNull ThrowingRunnable runnable) {
                runnable.run();
            }

            @Override @SneakyThrows
            public <R> R callAnonymous(@NonNull Callable<R> callable) {
                return callable.call();
            }

        };
    }
}
