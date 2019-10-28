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
package org.apache.isis.commons.internal.ioc.cdi;

import javax.enterprise.inject.spi.Bean;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.ioc.ManagedBeanAdapter;

import lombok.Value;
import lombok.val;

@Value(staticConstructor="of")
final class BeanAdapterCDI implements ManagedBeanAdapter {

    private final String id;
    private final Bean<?> bean;

    @Override
    public Can<?> getInstance() {
        val type = bean.getBeanClass();
        return _CDI.select(type, bean.getQualifiers());
    }

    @Override
    public boolean isCandidateFor(Class<?> requiredType) {
        return bean.getTypes().stream()
                .filter(type -> type instanceof Class)
                .map(type->(Class<?>)type)
                .anyMatch(type->requiredType.isAssignableFrom(type));
    }

    @Override
    public Class<?> getBeanClass() {
        return bean.getBeanClass(); //TODO[2033] does not work for 'produced' beans
    }

}
