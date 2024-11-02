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
package org.apache.causeway.viewer.wicket.ui.components.entity.collection;

import java.util.Optional;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;

import org.apache.causeway.applib.annotation.TableDecorator;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.layout.component.CollectionLayoutData;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.consent.Consent;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.viewer.wicket.model.models.ActionModel;
import org.apache.causeway.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.causeway.viewer.wicket.model.models.EntityCollectionModelParented;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.model.util.ComponentHintKey;
import org.apache.causeway.viewer.wicket.ui.components.actionlinks.entityactions.ActionLinksPanel;
import org.apache.causeway.viewer.wicket.ui.components.collection.CollectionPanel;
import org.apache.causeway.viewer.wicket.ui.components.collection.selector.CollectionPresentationSelectorHelper;
import org.apache.causeway.viewer.wicket.ui.components.collection.selector.CollectionPresentationSelectorPanel;
import org.apache.causeway.viewer.wicket.ui.panels.HasDynamicallyVisibleContent;
import org.apache.causeway.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.WktComponents;
import org.apache.causeway.viewer.wicket.ui.util.WktTooltips;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * {@link PanelAbstract Panel} representing the properties of an entity, as per
 * the provided {@link UiObjectWkt}.
 */
class EntityCollectionPanel
extends PanelAbstract<ManagedObject, UiObjectWkt>
implements HasDynamicallyVisibleContent {

    private static final long serialVersionUID = 1L;

    private static final String ID_COLLECTION_GROUP = "collectionGroup";
    private static final String ID_COLLECTION_NAME = "collectionName";
    private static final String ID_COLLECTION = "collection";

    private static final String ID_ADDITIONAL_LINKS = "additionalLinks";
    private static final String ID_SELECTOR_DROPDOWN = "selectorDropdown";

    private final ComponentHintKey selectedItemHintKey;

    @Getter(onMethod_= {@Override})
    private boolean visible = false;

    @Getter(value = AccessLevel.PROTECTED)
    private CollectionPresentationSelectorPanel selectorDropdownPanel;

    private final CollectionLayoutData layoutData;
    private final WebMarkupContainer div;

    public EntityCollectionPanel(final String id, final UiObjectWkt entityModel, final CollectionLayoutData layoutData) {
        super(id, entityModel);

        this.layoutData = layoutData;
        this.div = new WebMarkupContainer(ID_COLLECTION_GROUP);

        selectedItemHintKey = ComponentHintKey.create(super.getMetaModelContext(),
                this::getSelectorDropdownPanel,
                EntityCollectionModelParented.HINT_KEY_SELECTED_ITEM);

        buildGui();
    }

    /**
     * Attach UI only after added to parent.
     */
    @Override
    public void onInitialize() {
        super.onInitialize();

        final WebMarkupContainer panel = this;
        if(visible) {
            panel.add(div);
            this.setOutputMarkupId(true);
        } else {
            WktComponents.permanentlyHide(panel, div.getId());
        }

    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);
        tableDecorator().ifPresent(tableDecorator->
            renderHeadForTableDecorator(response, tableDecorator));
    }

    // -- HELPER

    private void buildGui() {
        var collectionModel = entityCollectionModelParented();
        div.setMarkupId("collection-" + collectionModel.getLayoutData().getId());

        var collectionMetaModel = collectionModel.getMetaModel();

        Wkt.cssAppend(div, collectionModel.getIdentifier());
        Wkt.cssAppend(div, collectionModel.getElementType().getFeatureIdentifier());

        var objectAdapter = getModel().getObject();
        final Consent visibility = collectionMetaModel
                .isVisible(objectAdapter, InteractionInitiatedBy.USER, Where.OBJECT_FORMS);

        if(visibility.isAllowed()) {

            visible = true;

            Facets.cssClass(collectionMetaModel, objectAdapter)
                .ifPresent(cssClass->Wkt.cssAppend(div, cssClass));

            tableDecorator().ifPresent(tableDecorator->
                Wkt.cssAppend(div, tableDecorator.cssClass()));

            var collectionPanel = new CollectionPanel(ID_COLLECTION, collectionModel);
            div.addOrReplace(collectionPanel);

            var labelComponent = Wkt.label(ID_COLLECTION_NAME,
                    collectionMetaModel.getFriendlyName(collectionModel::getParentObject));
            labelComponent.setEscapeModelStrings(true);
            div.add(labelComponent);

            collectionMetaModel.getDescription(collectionModel::getParentObject)
                .ifPresent(description->WktTooltips.addTooltip(labelComponent, description));

            final Can<ActionModel> links = collectionModel.getLinks();
            ActionLinksPanel.addActionLinks(
                    div, ID_ADDITIONAL_LINKS, links, ActionLinksPanel.Style.INLINE_LIST);

            createSelectorDropdownPanel(collectionModel);
            collectionPanel.setSelectorDropdownPanel(selectorDropdownPanel);

        }
    }

    // EntityCollectionModelParented caching
    private transient EntityCollectionModelParented entityCollectionModelParented;
    private EntityCollectionModelParented entityCollectionModelParented() {
        if(entityCollectionModelParented == null) {
            this.entityCollectionModelParented = EntityCollectionModelParented.forParentObjectModel(getModel(), layoutData);
        }
        return entityCollectionModelParented;
    }

    // TableDecorator caching
    private transient Optional<TableDecorator> tableDecorator;
    private Optional<TableDecorator> tableDecorator() {
        if(tableDecorator == null) {
            this.tableDecorator = entityCollectionModelParented()
                    .getMetaModel()
                    .getTableDecorator();
        }
        return tableDecorator;
    }

    private void createSelectorDropdownPanel(final EntityCollectionModel collectionModel) {

        final CollectionPresentationSelectorHelper selectorHelper =
                new CollectionPresentationSelectorHelper(collectionModel, getComponentFactoryRegistry(),
                        selectedItemHintKey);

        if (selectorHelper.getComponentFactories().isCardinalityMultiple()) {
            selectorDropdownPanel = new CollectionPresentationSelectorPanel(ID_SELECTOR_DROPDOWN,
                    collectionModel, selectedItemHintKey);
            div.addOrReplace(selectorDropdownPanel);
        } else {
            WktComponents.permanentlyHide(div, ID_SELECTOR_DROPDOWN);
        }
    }

}
