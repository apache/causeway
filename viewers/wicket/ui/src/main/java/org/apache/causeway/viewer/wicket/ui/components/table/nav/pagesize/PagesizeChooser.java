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
package org.apache.causeway.viewer.wicket.ui.components.table.nav.pagesize;

import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.panel.Panel;

import org.apache.causeway.viewer.wicket.ui.util.Wkt;

import lombok.Getter;

public class PagesizeChooser extends Panel {
    private static final long serialVersionUID = 1L;

    private static final String ID_PAGESIZE_CHOICE = "pagesizeChoice";
    private static final String ID_PAGESIZE_CHOICES = "pagesizeChoices";

    @Getter
    final DataTable<?, ?> table;

    public PagesizeChooser(final String id, final DataTable<?, ?> table) {
        super(id);
        this.table = table;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        buildGui();
    }

    // -- HELPER

    private void buildGui() {
        List<String> choices = List.of("10", "25", "100", "1000", "All");

        Wkt.listViewAdd(this, ID_PAGESIZE_CHOICES, choices, item->{
            Wkt.add(item, Wkt.label(ID_PAGESIZE_CHOICE, item.getModelObject()));
        });

    }

}
