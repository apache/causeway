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
package org.apache.isis.applib.services.xactn;

import java.util.concurrent.Callable;

import org.apache.isis.commons.functional.Result;
import org.apache.isis.commons.functional.ThrowingRunnable;

import lombok.val;

public interface TransactionalProcessor {
    
    /**
     * Runs given {@code callable} within an existing transactional boundary, or in the absence of such a
     * boundary creates a new one.
     *
     * @param callable
     */
    <T> Result<T> executeWithinTransaction(Callable<T> callable);
    
    /**
     * Runs given {@code runnable} within an existing transactional boundary, or in the absence of such a
     * boundary creates a new one.
     *
     * @param runnable
     */
    default Result<Void> executeWithinTransaction(ThrowingRunnable runnable) {
        val callable = ThrowingRunnable.toCallable(runnable);
        return executeWithinTransaction(callable);
    }

}
