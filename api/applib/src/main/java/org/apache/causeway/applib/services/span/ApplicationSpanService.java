/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.causeway.applib.services.span;

import java.util.concurrent.Callable;

import org.apache.causeway.commons.functional.ThrowingRunnable;

import lombok.SneakyThrows;

/**
 * Executes synchronous application work within an application-defined child span.
 *
 * <p>Suffixes must be non-null, nonblank and at most 46 characters, identifying
 * static operations or phases such as {@code load} or {@code process}.
 * Never include object identifiers, titles, bookmarks, arguments, record values,
 * user or tenant identities, or other per-invocation data in a suffix.
 *
 * <p>As with {@link org.apache.causeway.applib.services.iactn.InteractionService},
 * checked exceptions are not declared but escape unchanged and unwrapped,
 * as do runtime exceptions and errors.
 * Spans cannot outlive a service call and do not propagate to asynchronous work.
 * With observation inactive, work still executes exactly once with unchanged
 * result and exception behavior.
 *
 * @since 4.0 {@index}
 */
public interface ApplicationSpanService {

    /** Executes value-returning work once within an application-defined span. */
    <T> T call(String suffix, Callable<T> callable);

    /** Executes non-returning work once within an application-defined span. */
    void run(String suffix, ThrowingRunnable runnable);

    /**
     * Standalone unit-test helper that executes work directly, without suffix
     * validation or observation infrastructure.
     */
    ApplicationSpanService NOOP = new ApplicationSpanService() {
        @Override @SneakyThrows
        public <T> T call(final String suffix, final Callable<T> callable) {
            return callable.call();
        }
        @Override @SneakyThrows
        public void run(final String suffix, final ThrowingRunnable runnable) {
            runnable.run();
        }
    };
}
