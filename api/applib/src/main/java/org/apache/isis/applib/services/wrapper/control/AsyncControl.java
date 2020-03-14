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
package org.apache.isis.applib.services.wrapper.control;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import org.apache.isis.core.commons.collections.ImmutableEnumSet;
import org.apache.isis.schema.common.v2.OidDto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 *
 * @param <R> - return value.
 */
@Log4j2
public class AsyncControl<R> {

    public static <X> AsyncControl<X> toReturn(final Class<X> clazz) {
        return new AsyncControl<>();
    }

    public static AsyncControl<Void> ignoreReturn() {
        return new AsyncControl<>();
    }

    public static AsyncControl<Void> voidReturn() {
        return new AsyncControl<>();
    }

    private AsyncControl() {
    }

    /**
     * Set by framework.
     */
    @Setter(AccessLevel.PACKAGE)
    private Method method;

    /**
     * Set by framework.
     */
    @Setter(AccessLevel.PACKAGE)
    private OidDto oidDto;


    @Getter @NonNull
    private ImmutableEnumSet<ExecutionMode> executionModes = ExecutionModes.EXECUTE;
    public AsyncControl<R> with(ImmutableEnumSet<ExecutionMode> executionModes) {
        this.executionModes = executionModes;
        return this;
    }

    @Getter @NonNull
    private ExecutorService executorService = ForkJoinPool.commonPool();
    public AsyncControl<R> with(ExecutorService executorService) {
        this.executorService = executorService;
        return this;
    }

    @Getter @NonNull
    private RuleCheckingPolicy ruleCheckingPolicy = RuleCheckingPolicy.ASYNC;
    public AsyncControl<R> with(RuleCheckingPolicy ruleCheckingPolicy) {
        this.ruleCheckingPolicy = ruleCheckingPolicy;
        return this;
    }

    @Getter @NonNull
    private Consumer<Exception> exceptionHandler = (exception) -> {
        log.error(logMessage(), exception);
    };
    public AsyncControl<R> with(Consumer<Exception> exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        return this;
    }

    private String logMessage() {
        StringBuilder buf = new StringBuilder("Failed to execute ");
        if(method != null) {
            buf.append(" ").append(method.getName()).append(" ");
            if(oidDto != null) {
                buf.append(" on '")
                    .append(oidDto.getType())
                    .append(":")
                    .append(oidDto.getId())
                    .append("'");
            }
        }
        return buf.toString();
    }

    /**
     * Set by framework
     */
    @Getter @Setter(AccessLevel.PACKAGE)
    private Future<R> future;

}
