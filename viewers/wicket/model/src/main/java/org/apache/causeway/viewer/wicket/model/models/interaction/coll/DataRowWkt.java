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
package org.apache.causeway.viewer.wicket.model.models.interaction.coll;

import java.util.Optional;
import java.util.UUID;

import org.apache.wicket.model.ChainingModel;
import org.apache.wicket.model.IModel;

import org.apache.causeway.core.metamodel.interactions.managed.nonscalar.DataRow;
import org.apache.causeway.core.metamodel.interactions.managed.nonscalar.DataTableModel;
import org.apache.causeway.viewer.wicket.model.util.WktContext;

import lombok.Getter;
import lombok.NonNull;

public class DataRowWkt
extends ChainingModel<DataRow> {

    private static final long serialVersionUID = 1L;

    public static DataRowWkt chain(
            final IModel<DataTableModel> dataTableModelHolder,
            final DataRow dataRow) {
        return new DataRowWkt(dataTableModelHolder, dataRow);
    }

    @Getter private final @NonNull UUID uuid; // in support of table sorting
    @Getter private final @NonNull DataRowToggleWkt dataRowToggle;

    private transient DataRow dataRow;

    private DataRowWkt(
            final IModel<DataTableModel> dataTableModelHolder,
            final DataRow dataRow) {
        super(dataTableModelHolder);
        this.dataRow = dataRow;
        this.uuid = dataRow.getUuid();
        this.dataRowToggle = new DataRowToggleWkt(this);
    }

    @Override
    public final DataRow getObject() {
        if(dataRow==null) {
            dataRow = getDataTableModel().lookupDataRow(uuid)
                    .orElse(null);
            if(dataRow==null) {
                // XXX[CAUSEWAY-3005] UI out of sync with model: reload page
                WktContext.pageReload();
            }
        }
        return dataRow;
    }

    public Optional<DataRow> dataRow() {
        return Optional.ofNullable(getObject());
    }

    // -- HELPER

    private DataTableModel getDataTableModel() {
        return ((DataTableModelWkt) super.getTarget()).getObject();
    }

}
