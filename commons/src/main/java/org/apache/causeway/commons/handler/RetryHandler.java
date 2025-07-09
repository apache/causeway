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
package org.apache.causeway.commons.handler;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.springframework.core.retry.RetryException;

import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.exceptions._Exceptions;

/**
 * Retries a given task until its return value is valid.
 *
 * @since 4.0
 */
public record RetryHandler(
    int maxAttempts,
    /**
     * Time delay between attempts.
     */
    Duration delay) {

    // canonical constructor with argument validation
    public RetryHandler(int maxAttempts, Duration delay) {
        if(maxAttempts<1) {
            throw _Exceptions.illegalArgument("invalid argument 'maxAttempts' %d, at least one attempt is required", maxAttempts);
        }
        if(delay.isNegative()) {
            throw _Exceptions.illegalArgument("invalid argument 'delay' %s, must be non negative", delay);
        }
        this.maxAttempts = maxAttempts;
        this.delay = delay;
    }

    /**
     * Executes given {@code task} for {@link #maxAttempts} until the {@code isValid} predicate passes its test on the
     * {@code task}'s return value. If all attempts fail, returns a failed {@link Try}, wrapping a {@link RetryException}
     * with a message as provided by {@code onInvalidMessage}.
     *
     * <p> The initial {@code task} execution is without delay.
     *
     * <p> It the task throws any exception, this method immediately returns a failed {@link Try}, wrapping that exception.
     *
     * <p> If the {@link Thread#sleep(long)}, as used for the delay, throws any exception,
     * this method immediately returns a failed {@link Try}, wrapping that exception.
     *
     * @param <T> task return type
     * @param task that will be retried until {@code isValid} predicate tests positive
     * @param isValid predicate to validate the task return value
     * @param onInvalidMessage provides the exception message for when all attempts had failed
     */
    public <T> Try<T> retryUntilValid(Callable<T> task, Predicate<T> isValid, Supplier<String> onInvalidMessage) {

        for(int attemptCount = 1; attemptCount<=maxAttempts; ++attemptCount) {
            Try<T> tryT = Try.call(task);
            if(tryT.isFailure()) return tryT; // if we don't even get to validate, return immediately

            var t = tryT.getValue().orElse(null);

            if(isValid.test(t)) return Try.success(t);

            // if not last attempt, delay next attempt (that is, if delay is non-zero)
            if(attemptCount < maxAttempts
                && !delay.isZero()) {
                try {
                    Thread.sleep(delay.toMillis());
                } catch (Throwable e) {
                    return Try.failure(e);
                }
            }
        }

        // last attempt failed
        return Try.failure(new RetryException(onInvalidMessage.get()));
    }

}
