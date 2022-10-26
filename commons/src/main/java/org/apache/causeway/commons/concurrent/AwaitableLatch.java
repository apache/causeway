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
package org.apache.causeway.commons.concurrent;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.causeway.commons.internal.exceptions._Exceptions;

import lombok.RequiredArgsConstructor;

/**
 * Provides a read-only view on a {@link CountDownLatch}.
 * @since 2.0
 *
 */
@RequiredArgsConstructor(staticName = "of")
public final class AwaitableLatch {

    private final CountDownLatch countDownLatch;

    public static AwaitableLatch unlocked() {
        return of(new CountDownLatch(0));
    }

    /**
     * {@link AwaitableLatch#await()}
     * @throws RuntimeException when an InterruptedException occurred
     */
    public void await() {
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw _Exceptions.unrecoverable(e);
        }
    }

    /**
     * {@link AwaitableLatch#await(long, TimeUnit)}
     * @param timeout the maximum time to wait
     * @param unit the time unit of the {@code timeout} argument
     * @throws RuntimeException when an InterruptedException occurred
     */
    public boolean await(final long timeout, final TimeUnit unit) {
        try {
            return countDownLatch.await(timeout, unit);
        } catch (InterruptedException e) {
            throw _Exceptions.unrecoverable(e);
        }
    }

}
