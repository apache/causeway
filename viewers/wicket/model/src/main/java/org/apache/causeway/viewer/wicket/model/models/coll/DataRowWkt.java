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

import org.apache.causeway.core.metamodel.tabular.DataRow;
import org.apache.causeway.core.metamodel.tabular.DataTableInteractive;

public record DataRowWkt(
    int rowIndex,
    CollectionModel collectionModel)
implements IModel<DataRow> {

    @Override
    public DataRow getObject() {
        return dataRow().orElse(null);
    }

    public Optional<DataRow> dataRow() {
        return dataTable().lookupDataRow(rowIndex);
    }

    /**
     * Whether it is safe (free of side-effects) to load/access given model's object.
     * <p>
     * As of [CAUSEWAY-3658], don't call
     * {@link org.apache.causeway.viewer.wicket.model.models.coll.DataRowWkt#getObject()},
     * when the model's object is not transiently already loaded, because otherwise it would
     * enforce a page-reload as side-effect.
     */
    public boolean isTableDataLoaded() {
        return collectionModel().isTableDataLoaded();
    }

    // -- HELPER

    private DataTableInteractive dataTable() {
        return collectionModel().getObject();
    }

}
