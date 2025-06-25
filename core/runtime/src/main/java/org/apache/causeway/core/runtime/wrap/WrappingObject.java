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
package org.apache.causeway.core.runtime.wrap;

import java.lang.reflect.Modifier;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.applib.services.wrapper.control.SyncControl;
import org.apache.causeway.commons.internal.proxy._ProxyFactoryService.AdditionalField;
import org.apache.causeway.commons.internal.reflection._Reflect;
import org.apache.causeway.core.metamodel.object.ManagedObject;

import lombok.SneakyThrows;

/**
 * Implemented by all objects that have been viewed as per
 * {@link WrapperFactory#wrap(Object)}.
 *
 * @implNote domain classes may not have methods with <tt>__causeway_</tt> prefix
 *
 * @apiNote requires the mechanism that creates proxies implementing {@link WrappingObject}
 *      to additionally create the {@code __causeway_origin_field}.
 *
 * @since 1.x, revised for 3.4 {@index}
 */
public interface WrappingObject {

    final static String ORIGIN_GETTER_NAME = "__causeway_origin";
    final static String ORIGIN_FIELD_NAME = "__causeway_origin_field";
    final static String SAVE_METHOD_NAME = "__causeway_save";

    final static List<AdditionalField> ADDITIONAL_FIELDS = List.of(
            new AdditionalField(ORIGIN_FIELD_NAME, WrappingObject.Origin.class, Modifier.PROTECTED));

    record Origin(
            Object pojo,
            /**
             * The mixee adapted as a ManagedObject, used only if pojo is a mixin.
             */
            @Nullable ManagedObject managedMixee,
            SyncControl syncControl,
            boolean isFallback) {
        /**
         * fallback, used for non-proxied target, with no execute (no verify no rule checking).
         */
        public static Origin fallback(Object target) {
            return new Origin(target, null, SyncControl.control().withNoExecute(), true);
        }
        /**
         * fallback, used for non-proxied target as mixin, with no execute (no verify no rule checking)
         */
        public static Origin fallbackMixin(Object target, ManagedObject managedMixee) {
            return new Origin(target, managedMixee, SyncControl.control().withNoExecute(), true);
        }
        public Origin(Object pojo, SyncControl syncControl) {
            this(pojo, null, syncControl, false);
        }
        public Origin(Object pojo, ManagedObject managedMixee, SyncControl syncControl) {
            this(pojo, managedMixee, syncControl, false);
        }
    }

    /**
     * Getter for the underlying {@link Origin}.
     */
    Origin __causeway_origin();

    /**
     * Getter for the underlying {@link Origin}.
     */
    @SneakyThrows
    static Origin getOrigin(WrappingObject proxyObject)  {
        var field = proxyObject.getClass().getDeclaredField(ORIGIN_FIELD_NAME);
        return (Origin) _Reflect.getFieldOn(field, proxyObject);
    }

    /**
     * Wither for the underlying {@link Origin}.
     */
    @SneakyThrows
    static <T> T withOrigin(T proxyObject, Origin origin) {
        var field = proxyObject.getClass().getDeclaredField(ORIGIN_FIELD_NAME);
        _Reflect.setFieldOn(field, proxyObject, origin);
        return proxyObject;
    }

    /**
     * Programmatic equivalent of invoking save for a transient object .
     */
    void __causeway_save();

}
