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
package org.apache.causeway.viewer.wicket.ui.components.standalonecollection;

import java.util.List;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.Model;

import org.apache.causeway.core.config.metamodel.facets.CollectionLayoutConfigOptions;
import org.apache.causeway.core.metamodel.interactions.managed.nonscalar.DataTableModel;
import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.causeway.viewer.wicket.model.models.EntityCollectionModelStandalone;
import org.apache.causeway.viewer.wicket.ui.ComponentFactory;
import org.apache.causeway.viewer.wicket.ui.components.collection.count.CollectionCountProvider;
import org.apache.causeway.viewer.wicket.ui.components.collection.selector.CollectionPresentationSelectorHelper;
import org.apache.causeway.viewer.wicket.ui.components.collection.selector.CollectionPresentationSelectorPanel;
import org.apache.causeway.viewer.wicket.ui.components.collection.selector.CollectionPresentationSelectorProvider;
import org.apache.causeway.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.WktComponents;

import lombok.val;

public class StandaloneCollectionPanel
extends PanelAbstract<DataTableModel, EntityCollectionModel>
implements CollectionCountProvider, CollectionPresentationSelectorProvider {

    private static final long serialVersionUID = 1L;

    private static final String ID_STANDALONE_COLLECTION = "standaloneCollection";
    private static final String ID_ACTION_NAME = "actionName";

    private static final String ID_SELECTOR_DROPDOWN = "selectorDropdown";

    private final CollectionPresentationSelectorPanel selectorDropdownPanel;


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

        Facets.tableDecoration(collectionModel.getElementType())
            .map(CollectionLayoutConfigOptions.TableDecoration::cssClass)
            .ifPresent(tableDecorationCssClass->Wkt.cssAppend(outerDiv, tableDecorationCssClass));

        // selector
        final CollectionPresentationSelectorHelper selectorHelper = new CollectionPresentationSelectorHelper(collectionModel, getComponentFactoryRegistry());

        final List<ComponentFactory> componentFactories = selectorHelper.getComponentFactories();

        if (componentFactories.size() <= 1) {
            WktComponents.permanentlyHide(outerDiv, ID_SELECTOR_DROPDOWN);
            this.selectorDropdownPanel = null;
        } else {
            CollectionPresentationSelectorPanel selectorDropdownPanel = new CollectionPresentationSelectorPanel(ID_SELECTOR_DROPDOWN, collectionModel);

            final Model<ComponentFactory> componentFactoryModel = new Model<>();

            final String selected = selectorHelper.honourViewHintElseDefault(selectorDropdownPanel);
            ComponentFactory selectedComponentFactory = selectorHelper.find(selected);

            componentFactoryModel.setObject(selectedComponentFactory);

            outerDiv.setOutputMarkupId(true);
            outerDiv.addOrReplace(selectorDropdownPanel);

            this.selectorDropdownPanel = selectorDropdownPanel;
        }

        getComponentFactoryRegistry()
            .addOrReplaceComponent(outerDiv, UiComponentType.COLLECTION_CONTENTS, collectionModel);
    }


    // -- CollectionSelectorProvider

    @Override
    public CollectionPresentationSelectorPanel getSelectorDropdownPanel() {
        return selectorDropdownPanel;
    }

    // -- CollectionCountProvider

    @Override
    public Integer getCount() {
        final EntityCollectionModel model = getModel();
        return model.getElementCount();
    }

}
