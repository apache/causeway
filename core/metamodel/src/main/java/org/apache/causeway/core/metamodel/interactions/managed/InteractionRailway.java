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
package org.apache.causeway.core.metamodel.interactions.managed;

import java.io.Serializable;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.jspecify.annotations.NonNull;

import org.springframework.util.function.ThrowingFunction;

import org.apache.causeway.commons.functional.Railway;

/**
 * Thread-safe state machine that follows the railway pattern: once failed stays failed.
 * @see Railway
 */
record InteractionRailway<T extends ManagedMember>(
        AtomicReference<Railway<InteractionVeto, T>> state)
implements Serializable {

    public static <T extends ManagedMember> InteractionRailway<T> success(final T managedMember) {
        return new InteractionRailway<T>(new AtomicReference<>(Railway.success(managedMember)));
    }

    public static <T extends ManagedMember> InteractionRailway<T> veto(final InteractionVeto veto) {
        return new InteractionRailway<>(new AtomicReference<>(Railway.failure(veto)));
    }

    public boolean isVeto() { return internal().isFailure(); }
    public boolean isSuccess() { return internal().isSuccess(); }

    public Optional<InteractionVeto> getVeto() {
        return internal().getFailure();
    }

    public Optional<T> getSuccess() {
        return internal().getSuccess();
    }

    public T getSuccessElseFail() {
        return internal().getSuccessElseFail();
    }

    public T getSuccessElseFail(final Function<InteractionVeto, ? extends Throwable> toThrowable) {
        return internal().getSuccessElseFail(toThrowable);
    }

    /**
     * Only updates state from success. If successMapper result is non-empty, transitions to failed state.
     */
    public void update(final @NonNull ThrowingFunction<T, Optional<InteractionVeto>> successMapper) {
        state.getAndUpdate(railway->{
            try {
                return railway.chain(t->
                    successMapper
                        .apply(t)
                        .<Railway<InteractionVeto, T>>map(Railway::failure)
                        .orElse(railway));
            } catch(Throwable e) {
                return Railway.failure(InteractionVeto.invocationException(e));
            }
        });
    }

    // -- HELPER

    private Railway<InteractionVeto, T> internal() { return state.get(); }

}
