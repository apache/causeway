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

import java.util.Optional;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;

import org.apache.causeway.applib.annotation.TableDecorator;
import org.apache.causeway.core.metamodel.tabular.DataTableInteractive;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.causeway.viewer.wicket.model.models.EntityCollectionModelStandalone;
import org.apache.causeway.viewer.wicket.ui.components.collection.count.CollectionCountProvider;
import org.apache.causeway.viewer.wicket.ui.components.collection.selector.CollectionPresentationSelectorHelper;
import org.apache.causeway.viewer.wicket.ui.components.collection.selector.CollectionPresentationSelectorPanel;
import org.apache.causeway.viewer.wicket.ui.components.collection.selector.CollectionPresentationSelectorProvider;
import org.apache.causeway.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.WktComponents;

class StandaloneCollectionPanel
extends PanelAbstract<DataTableInteractive, EntityCollectionModel>
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

        var table = collectionModel.getDataTableModel();
        var featureId = collectionModel.getIdentifier();

        Wkt.labelAdd(outerDiv, StandaloneCollectionPanel.ID_ACTION_NAME,
                table.getTitle().getValue());

        Wkt.cssAppend(outerDiv, featureId);
        Wkt.cssAppend(outerDiv, collectionModel.getElementType().getFeatureIdentifier());

        this.tableDecorator = collectionModel.getTableDecoratorIfAny();
        tableDecorator.ifPresent(tableDecorator->{
            Wkt.cssAppend(outerDiv, tableDecorator.cssClass());
        });

        // selector
        final CollectionPresentationSelectorHelper selectorHelper =
                new CollectionPresentationSelectorHelper(collectionModel, getComponentFactoryRegistry());

        if (selectorHelper.getComponentFactories().isCardinalityMultiple()) {
            final CollectionPresentationSelectorPanel selectorDropdownPanel =
                    new CollectionPresentationSelectorPanel(ID_SELECTOR_DROPDOWN, collectionModel);

            outerDiv.setOutputMarkupId(true);
            outerDiv.addOrReplace(selectorDropdownPanel);

            this.selectorDropdownPanel = selectorDropdownPanel;
        } else {
            WktComponents.permanentlyHide(outerDiv, ID_SELECTOR_DROPDOWN);
            this.selectorDropdownPanel = null;
        }

        getComponentFactoryRegistry()
            .addOrReplaceComponent(outerDiv, UiComponentType.COLLECTION_CONTENTS, collectionModel);
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);
        tableDecorator().ifPresent(tableDecorator->
            renderHeadForTableDecorator(response, tableDecorator));
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

    // -- HELPER

    // TableDecorator caching
    private transient Optional<TableDecorator> tableDecorator;
    private Optional<TableDecorator> tableDecorator() {
        //noinspection OptionalAssignedToNull
        if(tableDecorator==null) {  // this is NOT a bug; we are caching an Optional
            var collectionModel = getModel();
            this.tableDecorator = collectionModel.getTableDecoratorIfAny();
        }
        return tableDecorator;
    }

}
