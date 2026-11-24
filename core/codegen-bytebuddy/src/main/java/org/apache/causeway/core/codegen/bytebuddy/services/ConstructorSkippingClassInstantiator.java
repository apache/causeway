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
package org.apache.causeway.core.codegen.bytebuddy.services;

import org.springframework.objenesis.ObjenesisStd;

interface ConstructorSkippingClassInstantiator {

    <T> T instantiate(Class<T> proxyClass) throws InstantiationException;

    // -- FACTORIES

    static ConstructorSkippingClassInstantiator unsafe() {
        return new UnsafeConstructorSkippingClassInstantiator(SunMiscUnsafeUtil.unsafe());
    }

    static ConstructorSkippingClassInstantiator objenesis() {
        return new ObjenesoisConstructorSkippingClassInstantiator(new ObjenesisStd());
    }

    // -- IMPLEMENTATIONS

    /** known to work for JVM 28-ea */
    record UnsafeConstructorSkippingClassInstantiator(sun.misc.Unsafe delegate)
    implements ConstructorSkippingClassInstantiator {
        @SuppressWarnings("unchecked")
        @Override public <T> T instantiate(final Class<T> proxyClass) throws InstantiationException {
            return (T) delegate.allocateInstance(proxyClass);
        }
    }

    /** known to work up to JVM 27 max; might change with newer versions */
    record ObjenesoisConstructorSkippingClassInstantiator(ObjenesisStd delegate)
    implements ConstructorSkippingClassInstantiator {
        @Override public <T> T instantiate(final Class<T> proxyClass) {
            return delegate.newInstance(proxyClass);
        }
    }

}
