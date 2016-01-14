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
import java.util.concurrent.atomic.AtomicInteger;

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

        final AtomicInteger tabGroupRef = new AtomicInteger(0);
        final ListView<TabGroup> tabGroupsList =
                new ListView<TabGroup>(ID_TAB_GROUPS, tabGroups) {

            @Override
            protected void populateItem(final ListItem<TabGroup> item) {

                final List<ITab> tabs = Lists.newArrayList();
                final TabGroup tabGroup = item.getModelObject();
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
                final AjaxBootstrapTabbedPanel ajaxBootstrapTabbedPanel = newTabbedPanel(tabs, tabGroupRef.get());

                item.add(ajaxBootstrapTabbedPanel);

                tabGroupRef.incrementAndGet();
            }

            private AjaxBootstrapTabbedPanel newTabbedPanel(final List<ITab> tabs, final int tabGroupNumber) {
                final AjaxBootstrapTabbedPanel tabbedPanel = new AjaxBootstrapTabbedPanel(ID_TAB_GROUP, tabs) {
                    @Override
                    public TabbedPanel setSelectedTab(final int index) {
                        saveSelectedTabInSession(tabGroupNumber, index);
                        return super.setSelectedTab(index);
                    }
                };
                setSelectedTabFromSessionIfAny(tabbedPanel, tabGroupNumber);
                return tabbedPanel;

            }

            private void setSelectedTabFromSessionIfAny(
                    final AjaxBootstrapTabbedPanel ajaxBootstrapTabbedPanel,
                    final int tabGroupNumber) {
                final String key = buildKey(tabGroupNumber);
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

            private void saveSelectedTabInSession(final int tabGroupNumber, final int tabIndex) {
                final String key = buildKey(tabGroupNumber);
                getSession().setAttribute(key, "" + tabIndex);
            }

            private String buildKey(final int tabGroupNumber) {
                final ObjectAdapterMemento objectAdapterMemento = TabGroupListPanel.this.getModel().getObjectAdapterMemento();
                final RootOid oid = (RootOid) objectAdapterMemento.getObjectAdapter(
                        AdapterManager.ConcurrencyChecking.NO_CHECK).getOid();
                final String key =
                        IsisContext.getOidMarshaller().marshalNoVersion(oid) + "." + tabGroupNumber + ".selectedTab";
                return key;
            }

        };

        add(tabGroupsList);
    }

}
