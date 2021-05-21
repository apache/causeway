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
package org.apache.isis.commons.internal.concurrent;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

import org.apache.isis.commons.internal.base._With;

import lombok.Getter;
import lombok.val;

/**
 * <h1>- internal use only -</h1>
 *
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 *
 * @since 2.0
 */
public abstract class _ConcurrentTask<T> implements Runnable {

    public static enum State {
        NOT_STARTED,
        STARTED,
        FAILED,
        SUCCEEDED
    }

    public abstract String getName();

    @Getter private State status = State.NOT_STARTED;
    @Getter private long startedAtNanos;
    @Getter private long completedAtNanos;
    @Getter private long failedAtNanos;
    @Getter private T completedWith;
    @Getter private Throwable failedWith;

    protected synchronized void preCall() {
        if(startedAtNanos>0L) {
            val msg = String.format(
                    "Cannot start task '%s' again, was already started before",
                    getName());
            throw new IllegalStateException(msg);
        }
        startedAtNanos = System.nanoTime();
        status = State.STARTED;
    }

    protected void postCall(T completedWith, Throwable failedWith) {
        if(failedWith!=null) {
            this.failedAtNanos = System.nanoTime();
            this.failedWith = failedWith;
            this.status = State.FAILED;
        } else {
            this.completedAtNanos = System.nanoTime();
            this.completedWith = completedWith;
            this.status = State.SUCCEEDED;
        }
    }

    abstract T innerCall() throws Exception;

    @Override
    public final void run() {

        preCall();
        try {
            val completedWith = innerCall();
            postCall(completedWith, /*failedWith*/ null);
        } catch (Throwable e) {
            postCall(/*completedWith*/ null, e);
        }

    }

    @Override
    public String toString() {
        return getName();
    }

    // -- NAMING

    public _ConcurrentTask<T> withName(String name) {

        _With.requires(name, "name");

        val delegate = this;

        return new _ConcurrentTask<T>() {

            @Override
            public T innerCall() throws Exception {
                return delegate.innerCall();
            }

            @Override
            public String getName() {
                return name;
            }

        };

    }

    public _ConcurrentTask<T> withName(Supplier<String> nameSupplier) {

        _With.requires(nameSupplier, "nameSupplier");

        val delegate = this;

        return new _ConcurrentTask<T>() {

            @Override
            public T innerCall() throws Exception {
                return delegate.innerCall();
            }

            @Override
            public String getName() {
                return nameSupplier.get();
            }

        };

    }


    // -- FACTORIES

    public static _ConcurrentTask<Void> of(Runnable runnable) {

        _With.requires(runnable, "runnable");

        return new _ConcurrentTask<Void>() {

            @Override
            public Void innerCall() throws Exception {
                runnable.run();
                return null;
            }

            @Override
            public String getName() {
                return runnable.toString();
            }

        };
    }

    public static <X> _ConcurrentTask<X> of(Callable<X> callable) {

        _With.requires(callable, "callable");

        return new _ConcurrentTask<X>() {

            @Override
            public X innerCall() throws Exception {
                return callable.call();
            }

            @Override
            public String getName() {
                return callable.toString();
            }

        };
    }

}
