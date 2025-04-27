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
package org.apache.causeway.core.runtimeservices.wrapper.handlers;

import java.lang.reflect.Field;

import org.apache.causeway.applib.services.wrapper.WrappingObject;
import org.apache.causeway.applib.services.wrapper.control.AsyncControl;
import org.apache.causeway.applib.services.wrapper.control.SyncControl;
import org.apache.causeway.commons.internal.proxy._ProxyFactoryService;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
@Getter
public class WrapperInvocationContext {

    /**
     * Either a domain object (entity, view model, service) or a mixin.
     */
    final Object targetPojo;
    /**
     * Not applicable if a domain object.
     */
    final Object mixeePojo;

    final SyncControl syncControl;
    final AsyncControl<?> asyncControl;


    @SneakyThrows
    public static <T> T set(T proxyObject, WrapperInvocationContext wic)  {
        if(proxyObject instanceof WrappingObject) {
            getField(proxyObject).set(proxyObject, wic);
        }
        return proxyObject;
    }

    @SneakyThrows
    public static WrapperInvocationContext get(Object proxyObject)  {
        if(!(proxyObject instanceof WrappingObject)) {
            return null;
        }
        final var wic = getField(proxyObject).get(proxyObject);
        return (WrapperInvocationContext) wic;
    }

    @SneakyThrows
    private static Field getField(Object proxyObject) {
        final var proxyObjectClass = proxyObject.getClass();
        final var field = proxyObjectClass.getDeclaredField(_ProxyFactoryService.WRAPPER_INVOCATION_CONTEXT_FIELD_NAME);
        field.setAccessible(true);
        return field;
    }

}
