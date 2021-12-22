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
package org.apache.isis.core.metamodel.interactions.managed.nonscalar;

import java.util.UUID;

import org.apache.isis.commons.internal.binding._Bindables;
import org.apache.isis.commons.internal.binding._Bindables.BooleanBindable;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects.EntityUtil;

import lombok.Getter;

public class DataRow {

    @Getter private final UUID uuid = UUID.randomUUID(); // in support of client side sorting
    private final ManagedObject rowElement;
    @Getter private final BooleanBindable selectToggle;

    public DataRow(final DataTableModel parentTable, final ManagedObject rowElement) {
        this.rowElement = rowElement;

        selectToggle = _Bindables.forBoolean(false);
        selectToggle.addListener((e,o,n)->{

            //_ToggleDebug.onSelectRowToggle(rowElement, o, n, parentTable.isToggleAllEvent.get());

            if(parentTable.isToggleAllEvent.get()) {
                return;
            }
            parentTable.getDataRowsSelected().invalidate();
            // in any case, if we have a toggle state change, clear the toggle all bindable
            parentTable.clearToggleAll();
        });

    }

    public ManagedObject getRowElement() {
        return EntityUtil.refetch(rowElement);
    }

    public ManagedObject getCellElement(final DataColumn column) {
        return column.getPropertyMetaModel().get(getRowElement());
    }

}
