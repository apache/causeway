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

package org.apache.isis.viewer.wicket.ui.components.entity.tabgroups;

import java.util.List;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import org.apache.isis.applib.layout.v1_0.ObjectLayoutMetadata;
import org.apache.isis.applib.layout.v1_0.Tab;
import org.apache.isis.applib.layout.v1_0.TabGroup;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.isis.core.metamodel.facets.object.layoutmetadata.ObjectLayoutMetadataFacet;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

import de.agilecoders.wicket.core.markup.html.bootstrap.tabs.AjaxBootstrapTabbedPanel;

/**
 * {@link PanelAbstract Panel} to represent an entity on a single page made up
 * of several &lt;div&gt; regions.
 */
public class EntityTabGroupsPanel extends PanelAbstract<EntityModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_ENTITY_PROPERTIES_AND_COLLECTIONS = "entityPropertiesAndCollections";
    private static final String ID_TAB_GROUPS = "tabGroups";
    private static final String ID_TAB_GROUP = "tabGroup";

    public EntityTabGroupsPanel(final String id, final EntityModel entityModel) {
        super(id, entityModel);
        buildGui();
    }

    private void buildGui() {
        final EntityModel model = getModel();
        final ObjectAdapter objectAdapter = model.getObject();
        final CssClassFacet facet = objectAdapter.getSpecification().getFacet(CssClassFacet.class);
        if(facet != null) {
            final String cssClass = facet.cssClass(objectAdapter);
            CssClassAppender.appendCssClassTo(this, cssClass);
        }

        // forces metadata to be derived && synced
        final ObjectLayoutMetadataFacet objectLayoutMetadataFacet = model.getTypeOfSpecification().getFacet(ObjectLayoutMetadataFacet.class);
        final ObjectLayoutMetadata objectLayoutMetadata = objectLayoutMetadataFacet.getMetadata();

        // TODO: debugging, remove
        final String xml = getServicesInjector().lookupService(JaxbService.class).toXml(objectLayoutMetadata);
        System.out.println(xml);

        addOrReplace(ComponentType.ENTITY_SUMMARY, model);

        final List<TabGroup> tabGroups = FluentIterable
                .from(objectLayoutMetadata.getTabGroups())
                .filter(TabGroup.Predicates.notEmpty())
                .toList();
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
                            return new EntityTabPanel(panelId, model, tab);
                        }
                    });
                }
                item.add(new AjaxBootstrapTabbedPanel(ID_TAB_GROUP, tabs));
            }
        };
        add(tabGroupsList);
    }

    private static class EntityTabPanel extends PanelAbstract {
        private static final long serialVersionUID = 1L;

        public EntityTabPanel(String id, final EntityModel model, final Tab tab) {
            super(id);

            final EntityModel modelWithTabHints = new EntityModel(model.getPageParameters());
            modelWithTabHints.withTabMetadata(tab);

            getComponentFactoryRegistry().addOrReplaceComponent(this, ID_ENTITY_PROPERTIES_AND_COLLECTIONS, ComponentType.ENTITY_PROPERTIES, modelWithTabHints);
        }
    }
}
