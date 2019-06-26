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
package org.apache.isis.commons.internal.functions;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Common Function idioms.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 *
 * @since 2.0
 */
public final class _Functions {

    // -- INDEX AWARE

    @FunctionalInterface
    public interface IndexAwareFunction<T, R> {
        public R apply(int index, T t);
    }

    /**
     * Converts an IndexAwareFunction into a Function, having its index start at 0,
     * and incremented after each function call.
     * @param indexAwareFunction
     * @return
     */
    public static <T, R> Function<T, R> indexAwareToFunction(IndexAwareFunction<T, R> indexAwareFunction){
        return new _Functions_IndexAwareFunctionAdapter<T, R>(indexAwareFunction);
    }

    // -- CHECKED EXCEPTION ADAPTERS (FUNCTION)

    /**
     *
     * Similar to {@link Function}, but allows checked exceptions to be thrown.
     *
     * @param <T>
     * @param <R>
     */
    @FunctionalInterface
    public interface CheckedFunction<T, R> {

        R apply(T t) throws Exception;

        default <U extends RuntimeException> Function<T, R> toUnchecked(Function<Exception, U> toUncheckedException) {
            return uncheckedFunction(this, toUncheckedException);
        }

    }

    public static <T, R, U extends RuntimeException> Function<T, R> uncheckedFunction(
            CheckedFunction<T, R> checkedFunction,
            Function<Exception, U> toUncheckedException) {
        return t->{
            try {
                return checkedFunction.apply(t);
            } catch (Exception e) {
                throw toUncheckedException.apply(e);
            }
        };
    }

    // -- CHECKED EXCEPTION ADAPTERS (RUNNABLE)

    /**
     *
     * Similar to {@link Runnable}, but allows checked exceptions to be thrown.
     *
     */
    @FunctionalInterface
    public interface CheckedRunnable {

        void run() throws Exception;

        default <U extends RuntimeException> Runnable toUnchecked(Function<Exception, U> toUncheckedException) {
            return uncheckedRunnable(this, toUncheckedException);
        }

    }

    public static <U extends RuntimeException> Runnable uncheckedRunnable(
            CheckedRunnable checkedRunnable,
            Function<Exception, U> toUncheckedException) {
        return ()->{
            try {
                checkedRunnable.run();
            } catch (Exception e) {
                throw toUncheckedException.apply(e);
            }
        };
    }

    // -- CHECKED EXCEPTION ADAPTERS (CONSUMER)

    /**
     *
     * Similar to {@link Consumer}, but allows checked exceptions to be thrown.
     *
     * @param <T>
     */
    @FunctionalInterface
    public interface CheckedConsumer<T> {

        void accept(T t) throws Exception;

        default <U extends RuntimeException> Consumer<T> toUnchecked(Function<Exception, U> toUncheckedException) {
            return uncheckedConsumer(this, toUncheckedException);
        }

    }

    public static <T, U extends RuntimeException> Consumer<T> uncheckedConsumer(
            CheckedConsumer<T> checkedConsumer,
            Function<Exception, U> toUncheckedException) {
        return t->{
            try {
                checkedConsumer.accept(t);
            } catch (Exception e) {
                throw toUncheckedException.apply(e);
            }
        };
    }

    // -- CHECKED EXCEPTION ADAPTERS (SUPPLIER)

    /**
     *
     * Similar to {@link Supplier}, but allows checked exceptions to be thrown.
     *
     * @param <T>
     */
    @FunctionalInterface
    public interface CheckedSupplier<T> {

        T get() throws Exception;

        default <U extends RuntimeException> Supplier<T> toUnchecked(Function<Exception, U> toUncheckedException) {
            return uncheckedSupplier(this, toUncheckedException);
        }

    }

    public static <T, U extends RuntimeException> Supplier<T> uncheckedSupplier(
            CheckedSupplier<T> checkedSupplier,
            Function<Exception, U> toUncheckedException) {
        return ()->{
            try {
                return checkedSupplier.get();
            } catch (Exception e) {
                throw toUncheckedException.apply(e);
            }
        };
    }

    // --


}
