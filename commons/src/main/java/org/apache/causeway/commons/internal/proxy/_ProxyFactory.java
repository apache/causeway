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
package org.apache.causeway.commons.internal.proxy;

import java.lang.reflect.InvocationHandler;

/**
 * Generates dynamic classes and corresponding instances by rebasing a given 'base' class.
 *
 * @since 2.0
 * @param <T> type of proxy objects this factory creates
 */
public interface _ProxyFactory<T> {

    // -- INTERFACE

    /**
     * @param handler
     * @param initialize whether to call a constructor on object instantiation
     * @return new proxy instance of type T
     * @throws IllegalArgumentException when using initialize=true and the number of
     * constructorArgTypes specified while building this factory is greater than 0.
     */
    public T createInstance(InvocationHandler handler, boolean initialize);

    /**
     * @param handler
     * @param constructorArgs passed to the constructor with matching signature on object instantiation
     * @return new proxy instance of type T (always uses a constructor)
     * @throws IllegalArgumentException if constructorArgs is null or empty or does not
     * conform to the number of constructorArgTypes specified while building this factory.
     */
    public T createInstance(InvocationHandler handler, Object[] constructorArgs);

}
