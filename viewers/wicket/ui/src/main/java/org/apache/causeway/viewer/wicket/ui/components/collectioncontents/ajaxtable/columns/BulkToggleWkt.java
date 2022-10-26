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
package org.apache.causeway.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns;

import java.util.Optional;

import org.apache.wicket.model.IModel;
import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal.binding._BindableAbstract;
import org.apache.causeway.core.metamodel.interactions.managed.nonscalar.DataTableModel;
import org.apache.causeway.viewer.wicket.model.models.binding.BooleanBinding;

/**
 * Boolean {@link IModel} to bind to the associated {@link DataTableModel}'s
 * select-all-toggle model to handle bulk check-box selection.
 */
public class BulkToggleWkt
extends BooleanBinding<DataTableModel> {

    private static final long serialVersionUID = 1L;

    public BulkToggleWkt(final IModel<DataTableModel> dataTableModelHolder) {
        super(dataTableModelHolder);
    }

    @Override
    protected Optional<_BindableAbstract<Boolean>> getBindable(
            final @Nullable DataTableModel dataTable) {
        return dataTable!=null
                ? Optional.ofNullable(dataTable.getSelectAllToggle())
                : Optional.empty();
    }

}
