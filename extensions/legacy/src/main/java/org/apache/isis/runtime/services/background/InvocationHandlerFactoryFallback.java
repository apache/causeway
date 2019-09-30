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
package org.apache.isis.runtime.services.background;

import java.lang.reflect.InvocationHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.isis.applib.services.xactn.TransactionService;

import lombok.RequiredArgsConstructor;

/**
 * only used if there is no BackgroundCommandService available
 * @since 2.0
 *
 */
@RequiredArgsConstructor
final class InvocationHandlerFactoryFallback implements InvocationHandlerFactory {

    final static class SimpleExecutor {

        /*
         * For the fixed thread-pool let there be 1-4 concurrent threads,
         * limited by the number of available (logical) processor cores.
         *
         * Note: Future improvements might make these values configurable,
         * but for now lets try to be reasonably nice here.
         *
         */
        private final int minThreadCount = 1;
        private final int maxThreadCount = 4;

        private final int threadCount =
                Math.max(minThreadCount,
                        Math.min(maxThreadCount,
                                Runtime.getRuntime().availableProcessors()));

        public final ExecutorService backgroundExecutorService =
                Executors.newFixedThreadPool(threadCount);

        public void shutdown() {
            backgroundExecutorService.shutdownNow();
        }

    }

    private final SimpleExecutor simpleExecutor = new SimpleExecutor();
    private final TransactionService transactionService;

    @Override
    public <T> InvocationHandler newMethodHandler(
            T target, 
            Object mixedInIfAny) {

        return new ForkingInvocationHandler<T>(
                target, 
                mixedInIfAny, 
                simpleExecutor.backgroundExecutorService,
                transactionService);
    }

    @Override
    public void close() {
        simpleExecutor.shutdown();
    }

}
