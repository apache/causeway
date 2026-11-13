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

import java.lang.reflect.Method;
import java.util.Objects;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.services.wrapper.control.SyncControl;
import org.apache.causeway.commons.internal._Constants;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.commons.internal.proxy.CachableInvocationHandler;

public interface WrapperInvocationHandler extends CachableInvocationHandler {

    ClassMetaData classMetaData();

    Object invoke(WrapperInvocation wrapperInvocation) throws Throwable;

    @Override
    default Object invoke(Object target, Method method, Object[] args) throws Throwable {
        return invoke(WrapperInvocation.of(target, method, args));
    }

    public record ClassMetaData(
            /** underlying class that is to be proxied */
            Class<?> pojoClass,

            Method equalsMethod,
            Method hashCodeMethod,
            Method toStringMethod,

            /**
             * The <tt>title()</tt> method; may be <tt>null</tt>.
             */
            @Nullable Method titleMethod) {

        /**
         * The <tt>__causeway_origin()</tt> method from {@link WrappingObject#__causeway_origin()}.
         */
        static final _Lazy<Method> __causeway_originMethod = _Lazy.threadSafe(()->
                WrappingObject.class.getMethod(WrappingObject.ORIGIN_GETTER_NAME, _Constants.emptyClasses));

        /**
         * The <tt>__causeway_save()</tt> method from {@link WrappingObject#__causeway_save()}.
         */
        static final _Lazy<Method> __causeway_saveMethod = _Lazy.threadSafe(()->
                WrappingObject.class.getMethod(WrappingObject.SAVE_METHOD_NAME, _Constants.emptyClasses));

        public static ClassMetaData of(
                final @NonNull Class<?> pojoClass) {
            try {
                var equalsMethod = pojoClass.getMethod("equals", _Constants.classesOfObject);
                var hashCodeMethod = pojoClass.getMethod("hashCode", _Constants.emptyClasses);
                var toStringMethod = pojoClass.getMethod("toString", _Constants.emptyClasses);

                var titleMethod = (Method)null;
                try {
                    titleMethod = pojoClass.getMethod("title", _Constants.emptyClasses);
                } catch (final NoSuchMethodException e) {
                    // ignore
                }
                return new WrapperInvocationHandler
                        .ClassMetaData(pojoClass, equalsMethod, hashCodeMethod, toStringMethod, titleMethod);

            } catch (final NoSuchMethodException e) {
                // ///CLOVER:OFF
                throw new RuntimeException("An Object method could not be found: " + e.getMessage());
                // ///CLOVER:ON
            }
        }

        public boolean isObjectMethod(final Method method) {
            return toStringMethod().equals(method)
                    || hashCodeMethod().equals(method)
                    || equalsMethod().equals(method);
        }

        public boolean isTitleMethod(Method method) {
            return method.equals(titleMethod);
        }
        public boolean isOriginMethod(Method method) {
            return method.equals(__causeway_originMethod.get());
        }
        public boolean isSaveMethod(Method method) {
            return method.equals(__causeway_saveMethod.get());
        }
    }

    public record WrapperInvocation(
        WrappingObject.@NonNull Origin origin,
        @NonNull Method method,
        @NonNull Object[] args) {

        static WrapperInvocation of(Object target, Method method, Object[] args) {
            Objects.requireNonNull(target);
            var origin = target instanceof WrappingObject wrappingObject
                    ? WrappingObject.getOrigin(wrappingObject)
                    : WrappingObject.Origin.fallback(target);
            return new WrapperInvocation(origin, method, args!=null ? args : _Constants.emptyObjects);
        }

        public SyncControl syncControl() {
            return origin().syncControl();
        }
    }

}
