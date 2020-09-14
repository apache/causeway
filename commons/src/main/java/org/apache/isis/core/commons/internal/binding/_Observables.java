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
package org.apache.isis.core.commons.internal.binding;

import java.util.function.Supplier;

import org.apache.isis.core.commons.internal.base._Lazy;

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
public class _Observables {

    public static class LazyObservable<T> extends _BindableAbstract<T> {

        private final _Lazy<T> lazyValue;
        
        public LazyObservable(Supplier<T> factory) {
            this.lazyValue = _Lazy.threadSafe(factory);
        }
        
        @Override
        public T getValue() {
            return lazyValue.get();
        }
        
        public void invalidate() {
            lazyValue.clear();
            super.fireValueChanged();
        }
                
    }
    
    public static <T> LazyObservable<T> forFactory(Supplier<T> factory) {
        val bindable = new LazyObservable<T>(factory);
        return bindable;
    }
    
}
