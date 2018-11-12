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
package org.apache.isis.applib.anyio;

import java.util.function.Function;

/**
 * Immutable data class, holds either a result or a failure object.
 * 
 * @since 2.0.0-M2
 *
 * @param <T>
 */
public final class Try<T> {
    
    private final T result;
    private final Exception failure;
    
    public static <T> Try<T> success(T result) {
        return new Try<>(result, null);
    }
    
    public static <T> Try<T> failure(Exception failure) {
        return new Try<>(null, failure);
    }
    
    private Try(T result, Exception failure) {
        this.result = result;
        this.failure = failure;
    }
    
    public boolean isSuccess() {
        return failure==null;
    }
    
    public boolean isFailure() {
        return failure!=null;
    }
    
    public Exception getFailure() {
        return failure;
    }
    
    public T getResult() {
        return result;
    }

    public void throwIfFailure() throws Exception {
        if(isFailure()) {
            throw failure;
        }
    }

    public <R> Try<R> map(Function<T, R> mapper) {
        return isSuccess() ? Try.success(mapper.apply(getResult())) : Try.failure(getFailure());
    }
    
}
