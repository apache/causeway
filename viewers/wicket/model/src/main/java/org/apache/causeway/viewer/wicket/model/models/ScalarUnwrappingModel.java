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
import org.springframework.util.ClassUtils;

import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmUnwrapUtil;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

/**
 * Wraps and unwraps the contained value within {@link ManagedObject},
 * as provided by a {@link ScalarModel}.
 */
public class ScalarUnwrappingModel<T>
extends ChainingModel<T> {

    private static final long serialVersionUID = 1L;

    @Getter @NonNull private final Class<T> type;

    public ScalarUnwrappingModel(
            final @NonNull Class<T> type,
            final @NonNull ScalarModel scalarModel) {
        super(scalarModel);
        this.type = type;
        _Assert.assertTrue(scalarModel.getScalarTypeSpec().isAssignableFrom(type), ()->
                String.format("cannot possibly unwrap model of type %s into target type %s",
                        scalarModel.getScalarTypeSpec().getCorrespondingClass(),
                        type));
    }

    @Override
    public T getObject() {
        val objectAdapter = scalarModel().getObject();
        val pojo = unwrap(objectAdapter);
        return pojo;
    }

    @Override
    public void setObject(final T object) {
        val scalarModel = scalarModel();
        if (object == null) {
            scalarModel.setObject(null);
        } else {
            val objectAdapter = scalarModel.getMetaModelContext().getObjectManager().adapt(object);
            scalarModel.setObject(objectAdapter);
        }
    }

    // -- HELPER

    private T unwrap(final ManagedObject objectAdapter) {
        val pojo = MmUnwrapUtil.single(objectAdapter);
        if(pojo==null
                || !ClassUtils.resolvePrimitiveIfNecessary(type)
                        .isAssignableFrom(ClassUtils.resolvePrimitiveIfNecessary(pojo.getClass()))) {
            return null;
        }
        return _Casts.uncheckedCast(pojo);
    }

    private ScalarModel scalarModel() {
        return (ScalarModel) super.getTarget();
    }

}