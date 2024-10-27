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
package org.apache.causeway.testing.integtestsupport.applib;

import java.util.concurrent.Callable;

import org.junit.jupiter.api.extension.ExtendWith;

import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.commons.functional.ThrowingRunnable;
import org.apache.causeway.commons.functional.Try;

/**
 * Convenient base class to extend for integration tests.
 *
 * <p>
 *     Unlike {@link CausewayIntegrationTestAbstract}, this class is not {@link ExtendWith extended with} {@link CausewayInteractionHandler},
 *     which means that there is <i>no</i> {@link org.apache.causeway.applib.services.iactn.Interaction} set up implicitly.
 *     Instead the {@link #given(Callable)}, {@link #when(Callable)} and {@link #then(Callable)} (and their various overrides) can be used
 *     with the provided {@link Callable} or {@link ThrowingRunnable} being called within its own {@link org.apache.causeway.applib.services.iactn.Interaction}.
 * </p>
 *
 * <p>
 *     Note that this means that references to entities must be re-retrieved within each given/when/then phase.
 *     {@link org.apache.causeway.applib.services.bookmark.Bookmark}s and the {@link org.apache.causeway.applib.services.bookmark.BookmarkService} can be a good way to do perform this re-retrieval.
 * </p>
 *
 * @since 2.0 {@index}
 * @see CausewayIntegrationTestAbstract
 */
@ExtendWith({ExceptionRecognizerTranslate.class})
public abstract class CausewayIntegrationGwtAbstract extends CausewayIntegrationTestBase {

    protected void given(final ThrowingRunnable runnable) {
        interactionService.runAnonymous(runnable);
    }

    protected <T> T given(final Callable<T> callable) {
        return interactionService.callAnonymous(callable);
    }

    protected void given(final InteractionContext interactionContext, final ThrowingRunnable runnable) {
        interactionService.run(interactionContext, runnable);
    }

    protected <T> T given(final InteractionContext interactionContext, final Callable<T> callable) {
        return interactionService.call(interactionContext, callable);
    }

    protected Try<Void> givenAndCatch(final ThrowingRunnable runnable) {
        return interactionService.runAnonymousAndCatch(runnable);
    }

    protected <T> Try<T> givenAndCatch(final Callable<T> callable) {
        return interactionService.callAnonymousAndCatch(callable);
    }

    protected Try<Void> givenAndCatch(final InteractionContext interactionContext, final ThrowingRunnable runnable) {
        return interactionService.runAndCatch(interactionContext, runnable);
    }

    protected <T> Try<T> givenAndCatch(final InteractionContext interactionContext, final Callable<T> callable) {
        return interactionService.callAndCatch(interactionContext, callable);
    }

    protected void when(final ThrowingRunnable runnable) {
        interactionService.runAnonymous(runnable);
    }

    protected <T> T when(final Callable<T> callable) {
        return interactionService.callAnonymous(callable);
    }

    protected void when(final InteractionContext interactionContext, final ThrowingRunnable runnable) {
        interactionService.run(interactionContext, runnable);
    }

    protected <T> T when(final InteractionContext interactionContext, final Callable<T> callable) {
        return interactionService.call(interactionContext, callable);
    }

    protected Try<Void> whenAndCatch(final ThrowingRunnable runnable) {
        return interactionService.runAnonymousAndCatch(runnable);
    }

    protected <T> Try<T> whenAndCatch(final Callable<T> callable) {
        return interactionService.callAnonymousAndCatch(callable);
    }

    protected Try<Void> whenAndCatch(final InteractionContext interactionContext, final ThrowingRunnable runnable) {
        return interactionService.runAndCatch(interactionContext, runnable);
    }

    protected <T> Try<T> whenAndCatch(final InteractionContext interactionContext, final Callable<T> callable) {
        return interactionService.callAndCatch(interactionContext, callable);
    }

    protected void then(final ThrowingRunnable runnable) {
        interactionService.runAnonymous(runnable);
    }

    protected <T> T then(final Callable<T> callable) {
        return interactionService.callAnonymous(callable);
    }

    protected void then(final InteractionContext interactionContext, final ThrowingRunnable runnable) {
        interactionService.run(interactionContext, runnable);
    }

    protected <T> T then(final InteractionContext interactionContext, final Callable<T> callable) {
        return interactionService.call(interactionContext, callable);
    }

    protected Try<Void> thenAndCatch(final ThrowingRunnable runnable) {
        return interactionService.runAnonymousAndCatch(runnable);
    }

    protected <T> Try<T> thenAndCatch(final Callable<T> callable) {
        return interactionService.callAnonymousAndCatch(callable);
    }

    protected Try<Void> thenAndCatch(final InteractionContext interactionContext, final ThrowingRunnable runnable) {
        return interactionService.runAndCatch(interactionContext, runnable);
    }

    protected <T> Try<T> thenAndCatch(final InteractionContext interactionContext, final Callable<T> callable) {
        return interactionService.callAndCatch(interactionContext, callable);
    }

}
