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

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.apache.isis.commons.internal.collections._Lists;

import static org.apache.isis.commons.internal.base._With.mapIfPresentElse;
import static org.apache.isis.commons.internal.base._With.requires;

import lombok.RequiredArgsConstructor;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Framework internal concurrency support.
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 *
 * @since 2.0.0-M3
 */
public final class _Tasks {
    
    public static _Tasks create() {
        return new _Tasks();
    }

    public void addRunnable(Runnable runnable) {
        requires(runnable, "runnable");
        addRunnable(runnable, null);
    }
    
    public void addRunnable(Runnable runnable, @Nullable Supplier<String> name) {
        requires(runnable, "runnable");
        callables.add(new NamedCallable<Object>(name) {

            @Override
            public Void call() throws Exception {
                runnable.run();
                return null;
            }
            
        });
    }
    
    public List<Callable<Object>> getCallables() {
        return callables;
    }
    
    // -- IMPLEMENTATION DETAILS
    
    private final List<Callable<Object>> callables = _Lists.newArrayList();
    
    @RequiredArgsConstructor
    private abstract static class NamedCallable<T> implements Callable<T> {
        
        private final Supplier<String> name;
                
        @Override
        public String toString() {
            return mapIfPresentElse(name, Supplier::get, super.toString());
        }
        
    }


}
