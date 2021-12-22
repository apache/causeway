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
package org.apache.isis.viewer.wicket.ui.components.standalonecollection;

import java.util.List;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.Model;

import org.apache.isis.core.metamodel.interactions.managed.nonscalar.DataTableModel;
import org.apache.isis.viewer.common.model.components.ComponentType;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModelStandalone;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.components.collection.count.CollectionCountProvider;
import org.apache.isis.viewer.wicket.ui.components.collection.selector.CollectionSelectorHelper;
import org.apache.isis.viewer.wicket.ui.components.collection.selector.CollectionSelectorPanel;
import org.apache.isis.viewer.wicket.ui.components.collection.selector.CollectionSelectorProvider;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.Components;
import org.apache.isis.viewer.wicket.ui.util.Wkt;

import lombok.val;

public class StandaloneCollectionPanel
extends PanelAbstract<DataTableModel, EntityCollectionModel>
implements CollectionCountProvider, CollectionSelectorProvider {

    private static final long serialVersionUID = 1L;

    private static final String ID_STANDALONE_COLLECTION = "standaloneCollection";
    private static final String ID_ACTION_NAME = "actionName";

    private static final String ID_SELECTOR_DROPDOWN = "selectorDropdown";

    private final CollectionSelectorPanel selectorDropdownPanel;


    private MarkupContainer outerDiv = this;

    public StandaloneCollectionPanel(
            final String id,
            final EntityCollectionModelStandalone collectionModel) {
        super(id, collectionModel);

        outerDiv = new WebMarkupContainer(ID_STANDALONE_COLLECTION);

        addOrReplace(outerDiv);

        val table = collectionModel.getDataTableModel();
        val featureId = collectionModel.getIdentifier();

        Wkt.labelAdd(outerDiv, StandaloneCollectionPanel.ID_ACTION_NAME,
                table.getTitle().getValue());

        Wkt.cssAppend(outerDiv, featureId);
        Wkt.cssAppend(outerDiv, collectionModel.getElementType().getFeatureIdentifier());

        // selector
        final CollectionSelectorHelper selectorHelper = new CollectionSelectorHelper(collectionModel, getComponentFactoryRegistry());

        final List<ComponentFactory> componentFactories = selectorHelper.getComponentFactories();

        if (componentFactories.size() <= 1) {
            Components.permanentlyHide(outerDiv, ID_SELECTOR_DROPDOWN);
            this.selectorDropdownPanel = null;
        } else {
            CollectionSelectorPanel selectorDropdownPanel = new CollectionSelectorPanel(ID_SELECTOR_DROPDOWN, collectionModel);

            final Model<ComponentFactory> componentFactoryModel = new Model<>();

            final String selected = selectorHelper.honourViewHintElseDefault(selectorDropdownPanel);
            ComponentFactory selectedComponentFactory = selectorHelper.find(selected);

            componentFactoryModel.setObject(selectedComponentFactory);

            outerDiv.setOutputMarkupId(true);
            outerDiv.addOrReplace(selectorDropdownPanel);

            this.selectorDropdownPanel = selectorDropdownPanel;
        }

        getComponentFactoryRegistry()
            .addOrReplaceComponent(outerDiv, ComponentType.COLLECTION_CONTENTS, collectionModel);
    }


    // -- CollectionSelectorProvider

    @Override
    public CollectionSelectorPanel getSelectorDropdownPanel() {
        return selectorDropdownPanel;
    }

    // -- CollectionCountProvider

    @Override
    public Integer getCount() {
        final EntityCollectionModel model = getModel();
        return model.getElementCount();
    }

}
