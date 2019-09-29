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
package org.apache.isis.applib.services.wrapper;

import java.util.EnumSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import org.apache.isis.applib.services.wrapper.WrapperFactory.ExecutionMode;

/**
 * Binds to a domain object, to prepare for type-safe asynchronous action invocation.
 * <p> 
 * Get an instance by using {@link WrapperFactory#async(Object)}.
 * 
 * @apiNote INCUBATING
 * @since 2.0
 */
public interface AsyncWrap<T> {

    // -- METHOD REFERENCE MATCHERS
    
    @FunctionalInterface
    public static interface Invoke0<T, R> {
        R invoke(T obj);
    }
    
    @FunctionalInterface
    public static interface Invoke1<T, R, A1> {
        R invoke(T obj, A1 arg1);
    }
    
    @FunctionalInterface
    public static interface Invoke2<T, R, A1, A2> {
        R invoke(T obj, A1 arg1, A2 arg2);
    }
    
    @FunctionalInterface
    public static interface Invoke3<T, R, A1, A2, A3> {
        R invoke(T obj, A1 arg1, A2 arg2, A3 arg3);
    }
    
    @FunctionalInterface
    public static interface Invoke4<T, R, A1, A2, A3, A4> {
        R invoke(T obj, A1 arg1, A2 arg2, A3 arg3, A4 arg4);
    }
    
    // -- CONFIGURATION
    
    ExecutorService getExecutor();
    AsyncWrap<T> withExecutor(ExecutorService executor);
    
    Consumer<Exception> getExceptionHandler();
    AsyncWrap<T> withExceptionHandler(Consumer<Exception> handler);
    
    EnumSet<ExecutionMode> getExecutionMode();
    
    // -- INVOCATION
    
    <R> Future<R> invoke(Invoke0<? super T, ? extends R> action);
    
    <R, A1> Future<R> invoke(Invoke1<? super T, ? extends R, A1> action, 
            A1 arg1);
    
    <R, A1, A2> Future<R> invoke(Invoke2<? super T, ? extends R, A1, A2> action, 
            A1 arg1, A2 arg2);
    
    <R, A1, A2, A3> Future<R> invoke(Invoke3<? super T, ? extends R, A1, A2, A3> action, 
            A1 arg1, A2 arg2, A3 arg3);
    
    <R, A1, A2, A3, A4> Future<R> invoke(Invoke4<? super T, ? extends R, A1, A2, A3, A4> action, 
            A1 arg1, A2 arg2, A3 arg3, A4 arg4);
    
}
