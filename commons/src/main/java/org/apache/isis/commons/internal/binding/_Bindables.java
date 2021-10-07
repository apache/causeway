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
package org.apache.isis.commons.internal.binding;

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
public class _Bindables {

    private static class SimpleBindable<T> extends _BindableAbstract<T> {}

    public static class BooleanBindable extends _BindableAbstract<Boolean> {
        public boolean toggleThenGet() {
            setValue(!getValue());
            return getValue();
        }
    }

    public static <T> _BindableAbstract<T> empty() {
        return new SimpleBindable<T>();
    }

    public static <T> _BindableAbstract<T> forValue(final T initialValue) {
        val bindable = new SimpleBindable<T>();
        bindable.setValue(initialValue);
        return bindable;
    }

    public static BooleanBindable forBoolean(final boolean initialValue) {
        val bindable = new BooleanBindable();
        bindable.setValue(initialValue);
        return bindable;
    }



}
