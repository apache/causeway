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
package org.apache.isis.runtime.system.transaction;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;

/**
 * Provides a read-only view on a {@link CountDownLatch}.
 * @since 2.0
 *
 */
@RequiredArgsConstructor(staticName = "of")
public final class TransactionLatch {

	private final CountDownLatch countDownLatch;  

	public static TransactionLatch unlocked() {
		return of(new CountDownLatch(0));
	}
	
	/**
	 * {@link CountDownLatch#await()}
	 * @throws InterruptedException
	 */
    public void await() throws InterruptedException {
    	countDownLatch.await();
    }

    /**
     * {@link CountDownLatch#await(long, TimeUnit)}
     * @throws InterruptedException
     */
    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
    	return countDownLatch.await(timeout, unit);
    }
	
}
