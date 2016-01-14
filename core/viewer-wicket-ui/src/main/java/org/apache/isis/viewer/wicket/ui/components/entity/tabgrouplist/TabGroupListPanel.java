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

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.extensions.markup.html.tabs.TabbedPanel;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import org.apache.isis.applib.layout.v1_0.Tab;
import org.apache.isis.applib.layout.v1_0.TabGroup;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.components.entity.tabpanel.TabPanel;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

import de.agilecoders.wicket.core.markup.html.bootstrap.tabs.AjaxBootstrapTabbedPanel;

public class TabGroupListPanel extends PanelAbstract<EntityModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_TAB_GROUPS = "tabGroups";
    private static final String ID_TAB_GROUP = "tabGroup";

    public TabGroupListPanel(final String id, final EntityModel entityModel) {
        super(id, entityModel);
        buildGui();
    }

    private void buildGui() {
        final EntityModel model = getModel();

        final List<TabGroup> tabGroups = model.getTabGroupListMetadata();

        final ListView<TabGroup> tabGroupsList = new ListView<TabGroup>(ID_TAB_GROUPS, tabGroups) {

            @Override
            protected void populateItem(final ListItem<TabGroup> item) {

                final TabGroup tabGroup = item.getModelObject();

                final EntityModel entityModelWithHints = model.cloneWithTabGroupMetadata(tabGroup);

                final AjaxBootstrapTabbedPanel ajaxBootstrapTabbedPanel = newTabbedPanel(entityModelWithHints);

                item.add(ajaxBootstrapTabbedPanel);
            }

                private AjaxBootstrapTabbedPanel newTabbedPanel(
                        final EntityModel entityModel) {

                    final TabGroup tabGroup = entityModel.getTabGroupMetadata();
                    final List<ITab> tabs = Lists.newArrayList();
                    final List<Tab> tabList = FluentIterable
                            .from(tabGroup.getTabs())
                            .filter(Tab.Predicates.notEmpty())
                            .toList();

                    for (final Tab tab : tabList) {
                        tabs.add(new AbstractTab(Model.of(tab.getName())) {
                            private static final long serialVersionUID = 1L;

                            @Override
                            public Panel getPanel(String panelId) {
                                return new TabPanel(panelId, model, tab);
                            }
                        });
                    }
                    final AjaxBootstrapTabbedPanel tabbedPanel = new MyAjaxBootstrapTabbedPanel(tabs, tabGroup,
                            entityModel);
                    return tabbedPanel;
                }


        };

        add(tabGroupsList);
    }

    private static class MyAjaxBootstrapTabbedPanel extends AjaxBootstrapTabbedPanel {
        private final TabGroup tabGroup;
        private final EntityModel entityModel;

        public MyAjaxBootstrapTabbedPanel(
                final List<ITab> tabs,
                final TabGroup tabGroup,
                final EntityModel entityModel) {
            super(TabGroupListPanel.ID_TAB_GROUP, tabs);
            this.tabGroup = tabGroup;
            this.entityModel = entityModel;

            setSelectedTabFromSessionIfAny(tabGroup, this, entityModel);
        }

        @Override
        public TabbedPanel setSelectedTab(final int index) {
            saveSelectedTabInSession(tabGroup, index, entityModel);
            return super.setSelectedTab(index);
        }

        private void setSelectedTabFromSessionIfAny(
                final TabGroup tabGroup,
                final AjaxBootstrapTabbedPanel ajaxBootstrapTabbedPanel,
                final EntityModel entityModel) {
            final String key = buildKey(tabGroup, entityModel);
            final String value = (String) getSession().getAttribute(key);
            if(value != null) {
                final int tabIndex = Integer.parseInt(value);
                final int numTabs = ajaxBootstrapTabbedPanel.getTabs().size();
                if(tabIndex < numTabs) {
                    // to support dynamic reloading; the data in the session might not be compatible with current layout.
                    ajaxBootstrapTabbedPanel.setSelectedTab(tabIndex);
                }
            }
        }

        private void saveSelectedTabInSession(
                final TabGroup tabGroup,
                final int tabIndex,
                final EntityModel entityModel) {
            final String key = buildKey(tabGroup, entityModel);
            getSession().setAttribute(key, "" + tabIndex);
        }

        private String buildKey(final TabGroup tabGroup, final EntityModel entityModel) {
            final ObjectAdapterMemento objectAdapterMemento = entityModel.getObjectAdapterMemento();
            final RootOid oid = (RootOid) objectAdapterMemento.getObjectAdapter(
                    AdapterManager.ConcurrencyChecking.NO_CHECK).getOid();
            final String key =
                    IsisContext.getOidMarshaller().marshalNoVersion(oid) + ":" + tabGroup.getPath() + "#selectedTab";
            return key;
        }

    }
}
