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

package org.apache.isis.viewer.wicket.ui.components.entity.tabbed;

import java.util.List;

import com.google.common.collect.Lists;

import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import org.apache.isis.applib.layout.v1_0.DomainObject;
import org.apache.isis.applib.layout.v1_0.Tab;
import org.apache.isis.applib.layout.v1_0.TabGroup;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.isis.core.metamodel.facets.object.layoutxml.LayoutXmlFacet;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

import de.agilecoders.wicket.core.markup.html.bootstrap.tabs.AjaxBootstrapTabbedPanel;

/**
 * {@link PanelAbstract Panel} to represent an entity on a single page made up
 * of several &lt;div&gt; regions.
 */
public class EntityTabbedPanel extends PanelAbstract<EntityModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_ENTITY_PROPERTIES_AND_COLLECTIONS = "entityPropertiesAndCollections";

    
    public EntityTabbedPanel(final String id, final EntityModel entityModel) {
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
        final LayoutXmlFacet layoutXmlFacet = model.getTypeOfSpecification().getFacet(LayoutXmlFacet.class);
        final DomainObject domainObject = layoutXmlFacet.getLayoutMetadata();


        addOrReplace(ComponentType.ENTITY_SUMMARY, model);


        List<TabGroup> tabGroups = domainObject.getTabGroups();
        TabGroup tabGroup = tabGroups.get(0);

        List<ITab> tabs = Lists.newArrayList();
        List<Tab> tabList = tabGroup.getTabs();

        for (Tab tab : tabList) {

            tabs.add(new AbstractTab(Model.of(tab.getName())) {
                private static final long serialVersionUID = 1L;

                @Override
                public Panel getPanel(String panelId) {
                    return new EntityTabPanel(panelId, getModel());
                }

            });

        }


        addOrReplace(new AjaxBootstrapTabbedPanel("tabs", tabs));

    }

    private static class EntityTabPanel extends PanelAbstract {
        private static final long serialVersionUID = 1L;

        public EntityTabPanel(String id, final EntityModel model) {
            super(id);
            getComponentFactoryRegistry().addOrReplaceComponent(this, ID_ENTITY_PROPERTIES_AND_COLLECTIONS, ComponentType.ENTITY_PROPERTIES, model);

        }

    }


}
