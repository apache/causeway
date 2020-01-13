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

    // -- METHOD REFERENCE MATCHERS (WITHOUT RETURN VALUE)
    
    @FunctionalInterface
    public static interface Run0<T> {
        void run(T obj);
    }
    
    @FunctionalInterface
    public static interface Run1<T, A1> {
        void run(T obj, A1 arg1);
    }
    
    @FunctionalInterface
    public static interface Run2<T, A1, A2> {
        void run(T obj, A1 arg1, A2 arg2);
    }
    
    @FunctionalInterface
    public static interface Run3<T, A1, A2, A3> {
        void run(T obj, A1 arg1, A2 arg2, A3 arg3);
    }
    
    @FunctionalInterface
    public static interface Run4<T, A1, A2, A3, A4> {
        void run(T obj, A1 arg1, A2 arg2, A3 arg3, A4 arg4);
    }
    
    // -- METHOD REFERENCE MATCHERS (WITH RETURN VALUE)
    
    @FunctionalInterface
    public static interface Call0<T, R> {
        R call(T obj);
    }
    
    @FunctionalInterface
    public static interface Call1<T, R, A1> {
        R call(T obj, A1 arg1);
    }
    
    @FunctionalInterface
    public static interface Call2<T, R, A1, A2> {
        R call(T obj, A1 arg1, A2 arg2);
    }
    
    @FunctionalInterface
    public static interface Call3<T, R, A1, A2, A3> {
        R call(T obj, A1 arg1, A2 arg2, A3 arg3);
    }
    
    @FunctionalInterface
    public static interface Call4<T, R, A1, A2, A3, A4> {
        R call(T obj, A1 arg1, A2 arg2, A3 arg3, A4 arg4);
    }
    
    // -- CONFIGURATION
    
    ExecutorService getExecutor();
    AsyncWrap<T> withExecutor(ExecutorService executor);
    
    Consumer<Exception> getExceptionHandler();
    AsyncWrap<T> withExceptionHandler(Consumer<Exception> handler);
    
    EnumSet<ExecutionMode> getExecutionMode();
    
    // -- INVOCATION (WITH RETURN VALUE)
    
    <R> Future<R> call(Call0<? super T, ? extends R> action);
    
    <R, A1> Future<R> call(Call1<? super T, ? extends R, A1> action, 
            A1 arg1);
    
    <R, A1, A2> Future<R> call(Call2<? super T, ? extends R, A1, A2> action, 
            A1 arg1, A2 arg2);
    
    <R, A1, A2, A3> Future<R> call(Call3<? super T, ? extends R, A1, A2, A3> action, 
            A1 arg1, A2 arg2, A3 arg3);
    
    <R, A1, A2, A3, A4> Future<R> call(Call4<? super T, ? extends R, A1, A2, A3, A4> action, 
            A1 arg1, A2 arg2, A3 arg3, A4 arg4);
    
    // -- INVOCATION (WITHOUT RETURN VALUE)
    
    default Future<Void> run(Run0<? super T> action) {
        return call(obj->{
            action.run(obj); 
            return null;
        });
    }
    
    default <A1> Future<Void> run(Run1<? super T, A1> action, 
            A1 arg1) {
        
        return call(obj->{
            action.run(obj, arg1); 
            return null;
        });
    }
    
    default <A1, A2> Future<Void> run(Run2<? super T, A1, A2> action, 
            A1 arg1, A2 arg2) {
        
        return call(obj->{
            action.run(obj, arg1, arg2); 
            return null;
        });
    }
    
    default <A1, A2, A3> Future<Void> run(Run3<? super T, A1, A2, A3> action, 
            A1 arg1, A2 arg2, A3 arg3) {
        
        return call(obj->{
            action.run(obj, arg1, arg2, arg3); 
            return null;
        });
    }
    
    default <A1, A2, A3, A4> Future<Void> run(Run4<? super T, A1, A2, A3, A4> action, 
            A1 arg1, A2 arg2, A3 arg3, A4 arg4) {
        
        return call(obj->{
            action.run(obj, arg1, arg2, arg3, arg4); 
            return null;
        });
    }
    
}
