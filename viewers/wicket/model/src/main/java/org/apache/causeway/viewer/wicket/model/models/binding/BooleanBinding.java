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
package org.apache.causeway.viewer.wicket.model.models.binding;

import java.util.Optional;

import org.apache.wicket.model.ChainingModel;
import org.apache.wicket.model.IModel;
import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.binding._BindableAbstract;

import lombok.val;

/**
 * Boolean {@link IModel} to bind to the associated {@code T} model`s
 * bindable boolean value.
 */
public abstract class BooleanBinding<T>
extends ChainingModel<Boolean> {

    private static final long serialVersionUID = 1L;

    protected BooleanBinding(final IModel<T> model) {
        super(model);
    }

    @Override
    public final Boolean getObject() {
        return getBindable(modelObject())
                .map(_BindableAbstract::getValue)
                .orElse(null);
    }

    @Override
    public final void setObject(final Boolean value) {
        getBindable(modelObject())
        .ifPresent(bindable->bindable.setValue(value));
    }

    protected abstract Optional<_BindableAbstract<Boolean>> getBindable(@Nullable T model);

    /**
     * For BulkToggleWkt returns its DataTableModel.<br>
     * For DataRowToggleWkt returns its DataRow.
     */
    protected T modelObject() {
        val model = _Casts.<IModel<T>>uncheckedCast(super.getTarget());
        return model.getObject();
    }

}

