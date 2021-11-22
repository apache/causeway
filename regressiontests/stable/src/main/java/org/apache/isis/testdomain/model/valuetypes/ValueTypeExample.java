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
package org.apache.isis.testdomain.model.valuetypes;

import java.util.List;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Property;

public abstract class ValueTypeExample<T> {

    @Property
    public abstract T getValue();
    public abstract void setValue(T value);

    @Programmatic
    public abstract T getUpdateValue();

    @Action
    public final void updateValue(final T value) {
        setValue(value);
    }

    @Collection
    public final List<T> getValues() {
        return List.of(getValue(), getUpdateValue());
    }

    @SuppressWarnings("unchecked")
    @Programmatic
    public final Class<T> getValueType() {
        return (Class<T>) getValue().getClass();
    }

}
