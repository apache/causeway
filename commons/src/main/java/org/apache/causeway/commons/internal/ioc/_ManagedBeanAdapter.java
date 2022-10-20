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
package org.apache.causeway.commons.internal.ioc;

import java.util.function.Supplier;

import org.apache.causeway.commons.collections.Can;

/**
 * @since 2.0
 */
public interface _ManagedBeanAdapter {

    String getId();
    Can<?> getInstance();
    Class<?> getBeanClass();

    boolean isCandidateFor(Class<?> requiredType);

    // -- TEST FACTORIES

    public static <T> _ManagedBeanAdapter forTestingLazy(final String logicalTypeName, final Class<T> beanClass, final Supplier<T> beanProvider) {
        return _ManagedBeanAdapter_forTestingLazy.of(logicalTypeName, beanClass, beanProvider);
    }

    public static <T> _ManagedBeanAdapter forTestingLazy(final Class<T> beanClass, final Supplier<T> beanProvider) {
        return _ManagedBeanAdapter_forTestingLazy.of(beanClass.getName(), beanClass, beanProvider);
    }

    public static <T> _ManagedBeanAdapter forTesting(final T bean) {
        return _ManagedBeanAdapter_forTestingLazy.of(bean.getClass().getName(), bean.getClass(), ()->bean);
    }

}