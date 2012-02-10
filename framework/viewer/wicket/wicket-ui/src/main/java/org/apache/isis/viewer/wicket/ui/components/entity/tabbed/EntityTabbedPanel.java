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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.tabs.TabbedPanelAbstract;

/**
 * {@link PanelAbstract Panel} for entity, with separate tabs for the summary
 * info (icon, title, actions), the properties, and for each of the collections.
 */
public class EntityTabbedPanel extends TabbedPanelAbstract<EntityModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_TABS = "tabs";

    public EntityTabbedPanel(final String id, final EntityModel entityModel) {
        super(id, entityModel);
        buildGui();
    }

    private void buildGui() {

        // create a list of ITab objects used to feed the tabbed panel
        final List<ITab> tabs = new ArrayList<ITab>();
        tabs.add(new AbstractTab(new Model<String>("Summary")) {
            private static final long serialVersionUID = 1L;

            @Override
            public Panel getPanel(final String panelId) {
                return new EntitySummaryTab(panelId, getModel());
            }
        });

        tabs.add(new AbstractTab(new Model<String>("Properties")) {
            private static final long serialVersionUID = 1L;

            @Override
            public Panel getPanel(final String panelId) {
                return new EntityPropertiesTab(panelId, getModel());
            }
        });

        final List<OneToManyAssociation> collectionList = getModel().getTypeOfSpecification().getCollections();
        for (final OneToManyAssociation collection : collectionList) {
            final EntityCollectionModel collectionModel = EntityCollectionModel.createParented(getModel(), collection);
            tabs.add(new AbstractTab(new Model<String>(collection.getName())) {
                private static final long serialVersionUID = 1L;

                @Override
                public Panel getPanel(final String panelId) {
                    return new EntityCollectionTab(panelId, collectionModel);
                }
            });
        }

        add(new AjaxTabbedPanel(ID_TABS, tabs));
    }
}
