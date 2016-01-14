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
package org.apache.isis.viewer.wicket.ui.components.entity.tabgroup;

import java.util.List;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.extensions.markup.html.tabs.TabbedPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import org.apache.isis.applib.layout.v1_0.TabGroupMetadata;
import org.apache.isis.applib.layout.v1_0.TabMetadata;
import org.apache.isis.viewer.wicket.model.hints.UiHintPathSignificant;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.util.ScopedSessionAttribute;
import org.apache.isis.viewer.wicket.ui.components.entity.tabpanel.TabPanel;

import de.agilecoders.wicket.core.markup.html.bootstrap.tabs.AjaxBootstrapTabbedPanel;

public class TabGroupPanel extends AjaxBootstrapTabbedPanel implements UiHintPathSignificant {

    public static final String SESSION_ATTR_SELECTED_TAB = "selectedTab";
    private final EntityModel entityModel;
    // the view metadata
    private final TabGroupMetadata tabGroup;
    private final ScopedSessionAttribute<Integer> selectedTabInSession;

    private static final String ID_TAB_GROUP = "tabGroup";

    private static List<ITab> tabsFor(final EntityModel entityModel) {
        final List<ITab> tabs = Lists.newArrayList();

        final TabGroupMetadata tabGroup = entityModel.getTabGroupMetadata();
        final List<TabMetadata> tabMetadataList = FluentIterable
                .from(tabGroup.getTabs())
                .filter(TabMetadata.Predicates.notEmpty())
                .toList();

        for (final TabMetadata tabMetadata : tabMetadataList) {
            tabs.add(new AbstractTab(Model.of(tabMetadata.getName())) {
                private static final long serialVersionUID1 = 1L;

                @Override
                public Panel getPanel(String panelId) {
                    return new TabPanel(panelId, entityModel, tabMetadata);
                }
            });
        }
        return tabs;
    }

    public TabGroupPanel(final EntityModel entityModel) {
        super(ID_TAB_GROUP, tabsFor(entityModel));

        this.entityModel = entityModel;
        this.tabGroup = entityModel.getTabGroupMetadata();
        this.selectedTabInSession = ScopedSessionAttribute.create(entityModel, tabGroup, SESSION_ATTR_SELECTED_TAB);

        setSelectedTabFromSessionIfAny(this);
    }

    @Override
    public TabbedPanel setSelectedTab(final int index) {
        selectedTabInSession.set(index);
        return super.setSelectedTab(index);
    }

    private void setSelectedTabFromSessionIfAny(
            final AjaxBootstrapTabbedPanel ajaxBootstrapTabbedPanel) {
        final Integer tabIndex = selectedTabInSession.get();
        if (tabIndex != null) {
            final int numTabs = ajaxBootstrapTabbedPanel.getTabs().size();
            if (tabIndex < numTabs) {
                // to support dynamic reloading; the data in the session might not be compatible with current layout.
                ajaxBootstrapTabbedPanel.setSelectedTab(tabIndex);
            }
        }
    }
}
