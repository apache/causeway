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
package org.apache.isis.core.runtime.context;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.core.runtime.iactn.IsisInteraction;

import lombok.experimental.UtilityClass;

/**
 * Context Utility
 */
@UtilityClass
public final class IsisContext {

    /**
     *
     * @return Isis's default class loader
     */
    public static ClassLoader getClassLoader() {
        return _Context.getDefaultClassLoader();
    }

    /**
     * Non-blocking call.
     * <p>
     * Returns a new CompletableFuture that is asynchronously completed by a task running in the 
     * ForkJoinPool.commonPool() with the value obtained by calling the given Supplier {@code computation}.
     * <p>
     * If the calling thread is within an open {@link IsisInteraction} then the ForkJoinPool does make this
     * session also available for any forked threads, via means of {@link InheritableThreadLocal}.
     * 
     * @param computation
     */
    public static <T> CompletableFuture<T> compute(Supplier<T> computation){
        return CompletableFuture.supplyAsync(computation);
    }


}
