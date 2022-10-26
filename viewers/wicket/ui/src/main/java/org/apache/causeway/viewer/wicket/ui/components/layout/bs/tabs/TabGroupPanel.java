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
package org.apache.causeway.viewer.wicket.ui.components.layout.bs.tabs;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.extensions.markup.html.tabs.TabbedPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import org.apache.causeway.applib.layout.grid.bootstrap.BSTab;
import org.apache.causeway.applib.layout.grid.bootstrap.BSTabGroup;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.model.util.ComponentHintKey;
import org.apache.causeway.viewer.wicket.ui.panels.HasDynamicallyVisibleContent;

import de.agilecoders.wicket.core.markup.html.bootstrap.tabs.AjaxBootstrapTabbedPanel;
import lombok.val;

// hmmm... not sure how to make this implement HasDynamicallyVisibleContent
public class TabGroupPanel
extends AjaxBootstrapTabbedPanel<ITab>
implements HasDynamicallyVisibleContent {

    private static final long serialVersionUID = 1L;

    public static final String SESSION_ATTR_SELECTED_TAB = "selectedTab";

    // the view metadata
    private final ComponentHintKey selectedTabHintKey;
    private final UiObjectWkt entityModel;

    private static List<ITab> tabsFor(final UiObjectWkt entityModel, final BSTabGroup bsTabGroup) {
        final List<ITab> tabs = new ArrayList<>();

        final List<BSTab> tablist = _NullSafe.stream(bsTabGroup.getTabs())
                .filter(BSTab.Predicates.notEmpty())
                .collect(Collectors.toList());

        for (val bsTab : tablist) {
            val repeatingViewWithDynamicallyVisibleContent = TabPanel.newRows(entityModel, bsTab);
            String tabName = bsTab.getName();
            tabs.add(new AbstractTab(Model.of(tabName)) {
                private static final long serialVersionUID = 1L;

                @Override
                public Panel getPanel(final String panelId) {
                    return new TabPanel(panelId, entityModel, bsTab, repeatingViewWithDynamicallyVisibleContent);
                }

                @Override
                public boolean isVisible() {
                    return repeatingViewWithDynamicallyVisibleContent.isVisible();
                }
            });
        }
        return tabs;
    }

    public TabGroupPanel(final String id, final UiObjectWkt entityModel, final BSTabGroup bsTabGroup) {
        super(id, tabsFor(entityModel, bsTabGroup));
        this.entityModel = entityModel;

        this.selectedTabHintKey = ComponentHintKey.create(entityModel.getMetaModelContext(), this, SESSION_ATTR_SELECTED_TAB);
    }

    @Override
    protected void onInitialize() {
        setSelectedTabFromSessionIfAny(this);
        super.onInitialize();
    }

    @Override
    public TabbedPanel<ITab> setSelectedTab(final int index) {
        selectedTabHintKey.set(entityModel.getOwnerBookmark(), ""+index);
        return super.setSelectedTab(index);
    }

    private void setSelectedTabFromSessionIfAny(
            final AjaxBootstrapTabbedPanel<ITab> ajaxBootstrapTabbedPanel) {
        final String selectedTabStr = selectedTabHintKey.get(entityModel.getOwnerBookmark());
        final Integer tabIndex = parse(selectedTabStr);
        if (tabIndex != null) {
            final int numTabs = ajaxBootstrapTabbedPanel.getTabs().size();
            if (tabIndex < numTabs) {
                // to support dynamic reloading; the data in the session might not be compatible with current layout.
                ajaxBootstrapTabbedPanel.setSelectedTab(tabIndex);
            }
        }
    }

    private Integer parse(final String selectedTabStr) {
        try {
            return Integer.parseInt(selectedTabStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public boolean isVisible() {
        return _NullSafe.stream(getTabs())
                .anyMatch(ITab::isVisible);
    }


}
