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

package org.apache.isis.viewer.wicket.ui.components.entity.collection;

import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.viewer.wicket.model.hints.UiHintPathSignificant;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.util.ScopedSessionAttribute;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions.AdditionalLinksPanel;
import org.apache.isis.viewer.wicket.ui.components.collection.CollectionPanel;
import org.apache.isis.viewer.wicket.ui.components.collection.selector.CollectionSelectorHelper;
import org.apache.isis.viewer.wicket.ui.components.collection.selector.CollectionSelectorPanel;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

/**
 * {@link PanelAbstract Panel} representing the properties of an entity, as per
 * the provided {@link EntityModel}.
 */
public class EntityCollectionPanel extends PanelAbstract<EntityModel> implements UiHintPathSignificant {

    private static final long serialVersionUID = 1L;

    private static final String ID_COLLECTION_GROUP = "collectionGroup";
    private static final String ID_COLLECTION_NAME = "collectionName";
    private static final String ID_COLLECTION = "collection";

    private static final String ID_ADDITIONAL_LINKS = "additionalLinks";
    private static final String ID_SELECTOR_DROPDOWN = "selectorDropdown";

    private final ScopedSessionAttribute<Integer> selectedItemSessionAttribute;

    public EntityCollectionPanel(final String id, final EntityModel entityModel) {
        super(id, entityModel);

        selectedItemSessionAttribute = ScopedSessionAttribute.create(
                entityModel, this, EntityCollectionModel.SESSION_ATTRIBUTE_SELECTED_ITEM);

        buildGui();
    }

    private void buildGui() {

        final WebMarkupContainer collectionRvContainer = this;

        final EntityCollectionModel entityCollectionModel = EntityCollectionModel.createParented(getModel());
        final OneToManyAssociation association = entityCollectionModel.getCollectionMemento().getCollection();

        final CssClassFacet facet = association.getFacet(CssClassFacet.class);
        if(facet != null) {
            final ObjectAdapter objectAdapter = getModel().getObject();
            final String cssClass = facet.cssClass(objectAdapter);
            CssClassAppender.appendCssClassTo(collectionRvContainer, cssClass);
        }

        final WebMarkupContainer fieldSet = new WebMarkupContainer(ID_COLLECTION_GROUP);
        collectionRvContainer.add(fieldSet);

        final CollectionPanel collectionPanel = new CollectionPanel(ID_COLLECTION, entityCollectionModel);
        fieldSet.addOrReplace(collectionPanel);

        Label labelComponent = collectionPanel.createLabel(ID_COLLECTION_NAME, association.getName());
        final NamedFacet namedFacet = association.getFacet(NamedFacet.class);
        labelComponent.setEscapeModelStrings(namedFacet == null || namedFacet.escaped());
        fieldSet.add(labelComponent);

        final String description = association.getDescription();
        if(description != null) {
            labelComponent.add(new AttributeAppender("title", Model.of(description)));
        }

        final List<LinkAndLabel> links = entityCollectionModel.getLinks();
        AdditionalLinksPanel.addAdditionalLinks (fieldSet,ID_ADDITIONAL_LINKS, links, AdditionalLinksPanel.Style.INLINE_LIST);

        final CollectionSelectorHelper selectorHelper =
                new CollectionSelectorHelper(entityCollectionModel, getComponentFactoryRegistry(),
                        selectedItemSessionAttribute);

        final List<ComponentFactory> componentFactories = selectorHelper.getComponentFactories();

        if (componentFactories.size() <= 1) {
            permanentlyHide(ID_SELECTOR_DROPDOWN);
        } else {
            CollectionSelectorPanel selectorDropdownPanel;
            selectorDropdownPanel = new CollectionSelectorPanel(ID_SELECTOR_DROPDOWN, entityCollectionModel, selectedItemSessionAttribute);

            final Model<ComponentFactory> componentFactoryModel = new Model<>();

            final int selected = selectorHelper.honourViewHintElseDefault(selectorDropdownPanel);

            ComponentFactory selectedComponentFactory = componentFactories.get(selected);
            componentFactoryModel.setObject(selectedComponentFactory);

            this.setOutputMarkupId(true);
            fieldSet.addOrReplace(selectorDropdownPanel);

            collectionPanel.setSelectorDropdownPanel(selectorDropdownPanel);
        }
    }
}
