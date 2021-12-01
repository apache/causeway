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

import java.util.function.BiConsumer;
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

    // -- NOOP

    public static <T> Consumer<T> noopConsumer() {
        return t->{};
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

        default <U extends RuntimeException> Function<T, R> toUnchecked(final Function<Exception, U> toUncheckedException) {
            return uncheckedFunction(this, toUncheckedException);
        }

    }

    public static <T, R, U extends RuntimeException> Function<T, R> uncheckedFunction(
            final CheckedFunction<T, R> checkedFunction,
            final Function<Exception, U> toUncheckedException) {
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

        default <U extends RuntimeException> Runnable toUnchecked(final Function<Exception, U> toUncheckedException) {
            return uncheckedRunnable(this, toUncheckedException);
        }

    }

    public static <U extends RuntimeException> Runnable uncheckedRunnable(
            final CheckedRunnable checkedRunnable,
            final Function<Exception, U> toUncheckedException) {
        return ()->{
            try {
                checkedRunnable.run();
            } catch (Exception e) {
                throw toUncheckedException.apply(e);
            }
        };
    }

    public static  Runnable uncheckedRunnable(final CheckedRunnable checkedRunnable) {
        return ()->{
            try {
                checkedRunnable.run();
            } catch (Exception e) {
                throw new RuntimeException(e);
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

        default <U extends RuntimeException> Consumer<T> toUnchecked(final Function<Exception, U> toUncheckedException) {
            return uncheckedConsumer(this, toUncheckedException);
        }

    }

    public static <T, U extends RuntimeException> Consumer<T> uncheckedConsumer(
            final CheckedConsumer<T> checkedConsumer,
            final Function<Exception, U> toUncheckedException) {
        return t->{
            try {
                checkedConsumer.accept(t);
            } catch (Exception e) {
                throw toUncheckedException.apply(e);
            }
        };
    }

    /**
    *
    * Similar to {@link BiConsumer}, but allows checked exceptions to be thrown.
    *
    * @param <T>
    */
   @FunctionalInterface
   public interface CheckedBiConsumer<T, U> {

       void accept(T t, U u) throws Exception;

       default <V extends RuntimeException> BiConsumer<T, U> toUnchecked(
               final Function<Exception, V> toUncheckedException) {
           return uncheckedBiConsumer(this, toUncheckedException);
       }

   }

   public static <T, U, V extends RuntimeException> BiConsumer<T, U> uncheckedBiConsumer(
           final CheckedBiConsumer<T, U> checkedBiConsumer,
           final Function<Exception, V> toUncheckedException) {
       return (t, u)->{
           try {
               checkedBiConsumer.accept(t, u);
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

        default <U extends RuntimeException> Supplier<T> toUnchecked(final Function<Exception, U> toUncheckedException) {
            return uncheckedSupplier(this, toUncheckedException);
        }

    }

    public static <T, U extends RuntimeException> Supplier<T> uncheckedSupplier(
            final CheckedSupplier<T> checkedSupplier,
            final Function<Exception, U> toUncheckedException) {
        return ()->{
            try {
                return checkedSupplier.get();
            } catch (Exception e) {
                throw toUncheckedException.apply(e);
            }
        };
    }

    public static <T, U extends RuntimeException> Supplier<T> uncheckedSupplier(
            final CheckedSupplier<T> checkedSupplier) {
        return ()->{
            try {
                return checkedSupplier.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }


}
