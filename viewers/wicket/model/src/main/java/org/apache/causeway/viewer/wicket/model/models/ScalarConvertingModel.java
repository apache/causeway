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
package org.apache.causeway.viewer.wicket.model.models;

import org.apache.wicket.model.ChainingModel;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;

import org.jspecify.annotations.NonNull;

/**
 * @param <T> foreign type
 * @param <V> scalar value type
 */
public abstract class ScalarConvertingModel<T, V>
extends ChainingModel<T> {

    private static final long serialVersionUID = 1L;

    protected ScalarConvertingModel(final @NonNull UiAttributeWkt attributeModel) {
        super(attributeModel);
    }

    @Override
    public void setObject(final T modelValue) {
        var attributeModel = attributeModel();
        var value = toScalarValue(modelValue);
        var objectAdapter = value != null
                ? attributeModel().getMetaModelContext().getObjectManager().adapt(value)
                : ManagedObject.empty(attributeModel.getElementType());
        attributeModel.setObject(objectAdapter);
    }

    @Override
    public T getObject() {
        var adapter = attributeModel().getObject();
        final V scalarValue = !ManagedObjects.isNullOrUnspecifiedOrEmpty(adapter)
                ? _Casts.uncheckedCast(adapter.getPojo())
                : null;
        return fromScalarValue(scalarValue);
    }

    // -- HOOKS

    protected abstract V toScalarValue(@Nullable T t);
    protected abstract T fromScalarValue(@Nullable V value);

    // -- HELPER

    protected UiAttributeWkt attributeModel() {
        return (UiAttributeWkt) super.getTarget();
    }

}
