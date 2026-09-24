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
package org.apache.causeway.applib.services.span;

import java.util.concurrent.Callable;

import org.apache.causeway.commons.functional.ThrowingRunnable;

/**
 * Executes synchronous application work within an application-defined child span.
 *
 * <p>
 * The suffix must be non-null, non-blank, no longer than 46 characters, and
 * identify a static operation or phase such as {@code load} or {@code process}.
 * It must not contain object identifiers, titles, bookmarks, arguments, record
 * values, user or tenant identities, or other per-invocation data.
 * </p>
 *
 * <p>
 * The service follows the exception convention of
 * {@link org.apache.causeway.applib.services.iactnlayer.InteractionService}:
 * checked exceptions are not declared, but the original checked exception,
 * runtime exception, or error escapes unchanged and unwrapped.
 * </p>
 *
 * <p>
 * Spans are synchronous and cannot outlive a service call.
 * When Causeway observation is inactive, supplied work still runs exactly once
 * with unchanged result and exception behavior.
 * </p>
 *
 * @since 2.2 {@index}
 */
public interface ApplicationSpanService {

    /**
     * Executes a value-returning operation within an application-defined span.
     *
     * @param suffix static operation or phase identifier, at most 46 characters
     * @param callable work to execute exactly once
     */
    <T> T call(String suffix, Callable<T> callable);

    /**
     * Executes a non-returning operation within an application-defined span.
     *
     * @param suffix static operation or phase identifier, at most 46 characters
     * @param runnable work to execute exactly once
     */
    void run(String suffix, ThrowingRunnable runnable);
}
