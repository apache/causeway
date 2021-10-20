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
package org.apache.isis.viewer.wicket.model.models.interaction.coll;

import java.util.UUID;

import org.apache.wicket.model.ChainingModel;

import org.apache.isis.core.metamodel.interactions.managed.nonscalar.DataRow;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;

public class DataRowWkt
extends ChainingModel<DataRow> {

    private static final long serialVersionUID = 1L;

    public static DataRowWkt chain(
            final EntityCollectionModel entityCollectionModel,
            final DataRow dataRow) {
        return new DataRowWkt(entityCollectionModel, dataRow);
    }

    private final UUID uuid; // in support of client side sorting
    private transient DataRow dataRow;

    private DataRowWkt(
            final EntityCollectionModel entityCollectionModel,
            final DataRow dataRow) {
        super(entityCollectionModel);
        this.dataRow = dataRow;
        this.uuid = dataRow.getUuid();
    }

    EntityCollectionModel parent() {
        return (EntityCollectionModel) super.getTarget();
    }

    @Override
    public DataRow getObject() {
        if(dataRow==null) {
            dataRow = parent().getDataTableModel().getDataRowsFiltered().getValue().stream()
                    .filter(dr->dr.getUuid().equals(uuid))
                    .findFirst()
                    .orElse(null);
        }
        return dataRow;
    }

}
