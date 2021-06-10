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
package org.apache.isis.core.security._testing;

import java.util.concurrent.Callable;

import org.apache.isis.applib.services.iactnlayer.InteractionContext;
import org.apache.isis.applib.services.iactnlayer.InteractionLayer;
import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.applib.services.iactnlayer.ThrowingRunnable;

import lombok.NonNull;
import lombok.SneakyThrows;

/**
 * A pass-through implementation, free of side-effects,
 * in support of simple JUnit tests.
 */
public class InteractionService_forTesting
implements InteractionService {

    private boolean isInInteraction = false;

    @Override
    public InteractionLayer openInteraction() {
        isInInteraction = true;
        return null;
    }

    @Override
    public InteractionLayer openInteraction(@NonNull InteractionContext interactionContext) {
        return openInteraction();
    }

    @Override
    public void closeInteractionLayers() {
        isInInteraction = false;
    }

    @Override
    public boolean isInInteraction() {
        return isInInteraction;
    }

    @Override @SneakyThrows
    public <R> R call(@NonNull InteractionContext interactionContext, @NonNull Callable<R> callable) {
        return callable.call();
    }

    @Override @SneakyThrows
    public void run(@NonNull InteractionContext interactionContext, @NonNull ThrowingRunnable runnable) {
        runnable.run();
    }


    @Override @SneakyThrows
    public void runAnonymous(@NonNull ThrowingRunnable runnable) {
        runnable.run();
    }

    @Override @SneakyThrows
    public <R> R callAnonymous(@NonNull Callable<R> callable) {
        return callable.call();
    }

}