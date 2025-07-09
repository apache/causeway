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
 *
 * @since 4.0
 */
public record RetryHandler(
    int maxAttempts,
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

    public <T, E extends Throwable> Try<T> retryUntilValid(Callable<T> task, Predicate<T> isValid, Supplier<String> onInvalidMessage) {

        for(int attemptCount = 1; attemptCount<=maxAttempts; ++attemptCount) {
            Try<T> tryT = Try.call(task);
            if(tryT.isFailure()) return tryT; // if we don't even get to validate, return immediately

            var optionalT = tryT.getValue();

            if(optionalT.isPresent()
                && isValid.test(optionalT.get())) {
                return Try.success(optionalT.get());
            }

            if(attemptCount < maxAttempts) {
                // if not last try, delay next try
                try {
                    Thread.sleep(delay.toMillis());
                } catch (InterruptedException e) {
                    return Try.failure(e);
                }
            }
        }

        // last attempt failed
        return Try.failure(new RetryException(onInvalidMessage.get()));
    }

}
