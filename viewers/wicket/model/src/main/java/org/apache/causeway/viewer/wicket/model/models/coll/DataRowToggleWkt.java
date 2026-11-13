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
package org.apache.causeway.viewer.wicket.model.models.coll;

import java.util.Optional;

import org.apache.wicket.model.IModel;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.commons.binding.Bindable;
import org.apache.causeway.core.metamodel.tabular.DataRow;
import org.apache.causeway.core.metamodel.tabular.DataTableInteractive;

/**
 * Boolean {@link IModel} to bind to the associated {@link DataTableInteractive}'s
 * {@link DataRow} model to handle check-box selection.
 */
public record DataRowToggleWkt(
    IModel<DataRow> delegate) implements IModel<Boolean> {

    @Override
    public final Boolean getObject() {
        return getBindable(modelObject())
            .map(Bindable::getValue)
            .orElse(null);
    }

    @Override
    public final void setObject(final Boolean value) {
        getBindable(modelObject())
            .ifPresent(bindable->bindable.setValue(value));
    }

    private Optional<Bindable<Boolean>> getBindable(
        final @Nullable DataRow dataRow) {
        return dataRow!=null
            ? Optional.ofNullable(dataRow.selectToggleBindable())
            : Optional.empty();
    }

    /**
     * For DataRowToggleWkt returns its DataRow.
     */
    private DataRow modelObject() {
        return delegate.getObject();
    }

}
