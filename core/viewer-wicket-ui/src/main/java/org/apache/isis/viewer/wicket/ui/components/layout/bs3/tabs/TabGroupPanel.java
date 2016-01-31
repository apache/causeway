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
package org.apache.isis.viewer.wicket.ui.components.layout.bs3.tabs;

import java.util.List;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.extensions.markup.html.tabs.TabbedPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import org.apache.isis.applib.layout.bootstrap3.BS3Tab;
import org.apache.isis.applib.layout.bootstrap3.BS3TabGroup;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.util.ScopedSessionAttribute;

import de.agilecoders.wicket.core.markup.html.bootstrap.tabs.AjaxBootstrapTabbedPanel;

public class TabGroupPanel extends AjaxBootstrapTabbedPanel {

    public static final String SESSION_ATTR_SELECTED_TAB = "selectedTab";

    // the view metadata
    private final ScopedSessionAttribute<Integer> selectedTabInSession;

    private static List<ITab> tabsFor(final EntityModel entityModel) {
        final List<ITab> tabs = Lists.newArrayList();

        final BS3TabGroup tabGroup = (BS3TabGroup) entityModel.getLayoutMetadata();
        final List<BS3Tab> tablist = FluentIterable
                .from(tabGroup.getTabs())
                .filter(BS3Tab.Predicates.notEmpty())
                .toList();

        for (final BS3Tab bs3Tab : tablist) {
            tabs.add(new AbstractTab(Model.of(bs3Tab.getName())) {
                private static final long serialVersionUID = 1L;

                @Override
                public Panel getPanel(String panelId) {
                    return new TabPanel(panelId, entityModel, bs3Tab);
                }
            });
        }
        return tabs;
    }

    public TabGroupPanel(String id, final EntityModel entityModel) {
        super(id, tabsFor(entityModel));

        this.selectedTabInSession = ScopedSessionAttribute.create(entityModel, this, SESSION_ATTR_SELECTED_TAB);
    }

    @Override
    protected void onInitialize() {
        setSelectedTabFromSessionIfAny(this);
        super.onInitialize();
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
