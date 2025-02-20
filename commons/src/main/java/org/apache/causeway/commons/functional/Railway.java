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
package org.apache.causeway.commons.functional;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Function;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.util.function.ThrowingConsumer;
import org.springframework.util.function.ThrowingFunction;

import lombok.SneakyThrows;

/**
 * The {@link Railway} type represents a value of one of two possible types (a disjoint union)
 * of {@link Success} or {@link Failure}, where chaining follows the <em>Railway Pattern</em>,
 * that is, once failed, stays failed.
 * <p>
 * Factory methods {@link Railway#success(Object)} and {@link Railway#failure(Object)}
 * correspond to the two possible values.
 *
 * @apiNote It is a common functional programming convention, to map the success value <i>right</i>.
 *
 * @since 2.0 {@index}
 */
public interface Railway<F, S> {

    // -- FACTORIES

    public static <F, S> Success<F, S> success(final @Nullable S success) {
        return new Success<>(success);
    }

    public static <F, S> Failure<F, S> failure(final @NonNull F failure) {
        return new Failure<>(failure);
    }

    // -- PREDICATES

    boolean isSuccess();
    boolean isFailure();

    // -- ACCESSORS

    /**
     * Optionally returns the contained {@code value} based on presence,
     * that is, if this is a {@link Success}.
     */
    Optional<S> getSuccess();
    default S getSuccessElseFail() { return getSuccess().orElseThrow(); }
    @SneakyThrows
    default S getSuccessElseFail(final Function<F, ? extends Throwable> toThrowable) {
        var successIfAny = getSuccess();
        if(successIfAny.isPresent()) {
            return successIfAny.get();
        }
        throw toThrowable.apply(getFailureElseFail());
    }
    /**
     * Optionally returns the contained {@code failure} based on presence,
     * that is, if this is a {@link Failure}.
     */
    Optional<F> getFailure();
    default F getFailureElseFail() { return getFailure().orElseThrow(); }

    // -- PEEKING

    /**
     * If this is a {@link Success}, peeks into the contained {@code success}.
     * @apiNote Any exceptions thrown by given successConsumer are propagated without catching.
     */
    Railway<F, S> ifSuccess(final @NonNull ThrowingConsumer<S> successConsumer);
    /**
     * If this is a {@link Failure}, peeks into the contained {@code failure}.
     * @apiNote Any exceptions thrown by given failureConsumer are propagated without catching.
     */
    Railway<F, S> ifFailure(final @NonNull ThrowingConsumer<F> failureConsumer);

    // -- MAPPING

    /**
     * If this is a {@link Success}, maps this {@link Railway} to another (success).
     * Otherwise if this is a {@link Failure} acts as identity operator.
     * @apiNote Any exceptions thrown by given successMapper are propagated without catching.
     */
    <R> Railway<F, R> mapSuccess(final @NonNull ThrowingFunction<S, R> successMapper);
    /**
     * Maps this {@link Railway} to another if this is a {@link Failure}.
     * Otherwise if this is a {@link Success} acts as identity operator.
     * @apiNote Any exceptions thrown by given failureMapper are propagated without catching.
     */
    <R> Railway<R, S> mapFailure(final @NonNull ThrowingFunction<F, R> failureMapper);

    // -- FOLDING

    /**
     * Maps the contained {@code failure} or {@code success} to a new value of type {@code R}
     * using according mapping function {@code failureMapper} or {@code successMapper}.
     * @apiNote Any exceptions thrown by given failureMapper or successMapper are propagated without catching.
     */
    <R> R fold(
            final @NonNull ThrowingFunction<F, R> failureMapper,
            final @NonNull ThrowingFunction<S, R> successMapper);

    // -- CHAINING

    /**
     * <em>Railway Pattern</em>
     * If this is a {@link Success}, returns a new {@link Railway} as produced by the
     * chainingFunction, that receives the current success value as input.
     * Otherwise if this is a {@link Failure} acts as identity operator and
     * the chainingFunction is not executed.
     * <p>
     * In other words: if once failed stays failed
     * @apiNote Any exceptions thrown by given chainingFunction are propagated without catching.
     */
    Railway<F, S> chain(@NonNull ThrowingFunction<S, Railway<F, S>> chainingFunction);

    // -- SUCCESS

    final record Success<F, S>(@NonNull S success) implements Railway<F, S>, Serializable {

        @Override public boolean isSuccess() { return true; }
        @Override public boolean isFailure() { return false; }

        @Override public Optional<S> getSuccess() { return Optional.of(success); }
        @Override public Optional<F> getFailure() { return Optional.empty(); }

        @Override
        public Success<F, S> ifSuccess(final @NonNull ThrowingConsumer<S> successConsumer) {
            successConsumer.accept(success);
            return this;
        }

        @Override
        public Success<F, S> ifFailure(final @NonNull ThrowingConsumer<F> failureConsumer) {
            return this;
        }

        @Override
        public <R> Success<F, R> mapSuccess(final @NonNull ThrowingFunction<S, R> successMapper) {
            return Railway.success(successMapper.apply(success));
        }

        @Override
        public <R> Success<R, S> mapFailure(final @NonNull ThrowingFunction<F, R> failureMapper) {
            return Railway.success(success);
        }

        @Override
        public <R> R fold(
                final @NonNull ThrowingFunction<F, R> failureMapper,
                final @NonNull ThrowingFunction<S, R> successMapper) {
            return successMapper.apply(success);
        }

        @Override
        public Railway<F, S> chain(final @NonNull ThrowingFunction<S, Railway<F, S>> chainingFunction){
            return chainingFunction.apply(success);
        }

    }

    // -- FAILURE

    final record Failure<F, S>(@NonNull F failure) implements Railway<F, S>, Serializable {

        @Override public boolean isSuccess() { return false; }
        @Override public boolean isFailure() { return true; }

        @Override public Optional<S> getSuccess() { return Optional.empty(); }
        @Override public Optional<F> getFailure() { return Optional.of(failure); }

        @Override
        public Failure<F, S> ifSuccess(final @NonNull ThrowingConsumer<S> successConsumer) {
            return this;
        }

        @Override
        public Failure<F, S> ifFailure(final @NonNull ThrowingConsumer<F> failureConsumer) {
            failureConsumer.accept(failure);
            return this;
        }

        @Override
        public <R> Failure<F, R> mapSuccess(final @NonNull ThrowingFunction<S, R> successMapper) {
            return Railway.failure(failure);
        }

        @Override
        public <R> Failure<R, S> mapFailure(final @NonNull ThrowingFunction<F, R> failureMapper) {
            return Railway.failure(failureMapper.apply(failure));
        }

        @Override
        public <R> R fold(
                final @NonNull ThrowingFunction<F, R> failureMapper,
                final @NonNull ThrowingFunction<S, R> successMapper) {
            return failureMapper.apply(failure);
        }

        @Override
        public Railway<F, S> chain(final @NonNull ThrowingFunction<S, Railway<F, S>> chainingFunction){
            return this;
        }

    }

    // -- TYPE COMPOSITION

    @FunctionalInterface
    public static interface HasRailway<F, S> extends Railway<F, S> {

        Railway<F, S> railway();

        @Override default boolean isSuccess() { return railway().isSuccess(); }
        @Override default boolean isFailure() { return railway().isFailure(); }

        @Override default Optional<S> getSuccess() { return railway().getSuccess(); }
        @Override default Optional<F> getFailure() { return railway().getFailure(); }

        @Override default Railway<F, S> ifSuccess(final @NonNull ThrowingConsumer<S> successConsumer) {
            return railway().ifSuccess(successConsumer); }
        @Override default Railway<F, S> ifFailure(final @NonNull ThrowingConsumer<F> failureConsumer) {
            return railway().ifFailure(failureConsumer); }

        @Override default <R> Railway<F, R> mapSuccess(final @NonNull ThrowingFunction<S, R> successMapper) {
            return railway().mapSuccess(successMapper); }
        @Override default <R> Railway<R, S> mapFailure(final @NonNull ThrowingFunction<F, R> failureMapper) {
            return railway().mapFailure(failureMapper); }

        @Override default <R> R fold(
                final @NonNull ThrowingFunction<F, R> failureMapper,
                final @NonNull ThrowingFunction<S, R> successMapper) {
            return railway().fold(failureMapper, successMapper);
        }

        @Override default public Railway<F, S> chain(final @NonNull ThrowingFunction<S, Railway<F, S>> chainingFunction){
            return railway().chain(chainingFunction);
        }
    }

}
