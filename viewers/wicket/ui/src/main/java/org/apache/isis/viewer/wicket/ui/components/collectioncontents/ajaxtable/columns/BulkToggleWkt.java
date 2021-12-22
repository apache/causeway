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
package org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns;

import org.apache.wicket.model.ChainingModel;
import org.apache.wicket.model.IModel;

import org.apache.isis.core.metamodel.interactions.managed.nonscalar.DataTableModel;

public class BulkToggleWkt
extends ChainingModel<Boolean> {

    private static final long serialVersionUID = 1L;

    public BulkToggleWkt(final IModel<DataTableModel> dataTableModelHolder) {
        super(dataTableModelHolder);
    }

    @Override
    public Boolean getObject() {
        return dataTableModel().getSelectAllToggle().getValue();
    }

    @Override
    public void setObject(final Boolean value) {
        dataTableModel().getSelectAllToggle().setValue(value);
    }

    // -- HELPER

    @SuppressWarnings("unchecked")
    private DataTableModel dataTableModel() {
        return ((IModel<DataTableModel>) super.getTarget()).getObject();
    }

}
