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

package org.apache.isis.viewer.wicket.ui.components.entity.tabgrouplist;

import java.util.List;

import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;

import org.apache.isis.applib.layout.fixedcols.TabGroupMetadata;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.components.entity.tabgroup.TabGroupPanel;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

public class TabGroupListPanel extends PanelAbstract<EntityModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_TAB_GROUPS = "tabGroups";

    // the view metadata
    private final List<TabGroupMetadata> tabGroups;

    public TabGroupListPanel(final String id, final EntityModel entityModel) {
        super(id, entityModel);

        this.tabGroups = entityModel.getTabGroupListMetadata();

        buildGui();
    }

    private void buildGui() {
        final EntityModel model = getModel();

        final ListView<TabGroupMetadata> tabGroupsList = new ListView<TabGroupMetadata>(ID_TAB_GROUPS, this.tabGroups) {

            @Override
            protected void populateItem(final ListItem<TabGroupMetadata> item) {

                final TabGroupMetadata tabGroup = item.getModelObject();
                final EntityModel entityModelWithHints = model.cloneWithTabGroupMetadata(tabGroup);
                final TabGroupPanel tabGroupPanel = new TabGroupPanel(entityModelWithHints);
                item.add(tabGroupPanel);
            }
        };

        add(tabGroupsList);
    }

}
