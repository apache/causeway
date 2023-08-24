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
package org.apache.causeway.testdomain;

import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.springframework.transaction.annotation.Propagation;

import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.inject.ServiceInjector;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.commons.functional.ThrowingRunnable;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;

/**
 * Manage interactions yourself, by either wrapping your code blocks
 * with {@link #run(ThrowingRunnable)} or {@link #call(Callable)}.
 */
public abstract class RegressionTestAbstract {

    protected void run(final ThrowingRunnable runnable) {
        transactionService.runTransactional(Propagation.REQUIRES_NEW, ()->
            interactionService.runAnonymous(runnable))
        .ifFailureFail();
    }

    protected <T> T call(final Callable<T> callable) {
        return transactionService.callTransactional(Propagation.REQUIRES_NEW, ()->
            interactionService.callAnonymous(callable))
        // assuming return value of callable is not nullable
        .valueAsNonNullElseFail();
    }

    // -- ASSERTIONS

    // -- DEPENDENCIES

    @Inject protected BookmarkService bookmarkService;
    @Inject protected TransactionService transactionService;
    @Inject protected RepositoryService repositoryService;
    @Inject protected FactoryService factoryService;
    @Inject protected ServiceInjector serviceInjector;
    @Inject protected InteractionService interactionService;
    @Inject protected ObjectManager objectManager;

}
