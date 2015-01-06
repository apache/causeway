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

package org.apache.isis.viewer.wicket.ui.components.entity.collections;

import java.util.List;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.filter.Filters;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions.AdditionalLinksPanel;
import org.apache.isis.viewer.wicket.ui.components.collection.CollectionPanel;
import org.apache.isis.viewer.wicket.ui.components.collection.selector.CollectionSelectorHelper;
import org.apache.isis.viewer.wicket.ui.components.collection.selector.CollectionSelectorPanel;
import org.apache.isis.viewer.wicket.ui.components.widgets.containers.UiHintPathSignificantWebMarkupContainer;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

/**
 * {@link PanelAbstract Panel} representing the properties of an entity, as per
 * the provided {@link EntityModel}.
 */
public class EntityCollectionsPanel extends PanelAbstract<EntityModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_ENTITY_COLLECTIONS = "entityCollections";
    private static final String ID_COLLECTION_GROUP = "collectionGroup";
    private static final String ID_COLLECTION_NAME = "collectionName";
    private static final String ID_COLLECTIONS = "collections";
    private static final String ID_COLLECTION = "collection";

    private static final String ID_ADDITIONAL_LINKS = "additionalLinks";
    private static final String ID_SELECTOR_DROPDOWN = "selectorDropdown";

    public EntityCollectionsPanel(final String id, final EntityModel entityModel) {
        super(id, entityModel);

        buildGui();
    }

    private void buildGui() {
        buildEntityPropertiesAndOrCollectionsGui();
        setOutputMarkupId(true); // so can repaint via ajax
    }

    private void buildEntityPropertiesAndOrCollectionsGui() {
        final EntityModel model = getModel();
        final ObjectAdapter adapter = model.getObject();
        if (adapter != null) {
            addCollections();
        } else {
            permanentlyHide(ID_ENTITY_COLLECTIONS);
        }
    }

    private void addCollections() {
        final EntityModel entityModel = getModel();
        final ObjectAdapter adapter = entityModel.getObject();
        final ObjectSpecification noSpec = adapter.getSpecification();
        final List<ObjectAssociation> associations = visibleCollections(adapter, noSpec);

        final RepeatingView collectionRv = new RepeatingView(ID_COLLECTIONS);
        add(collectionRv);

        for (final ObjectAssociation association : associations) {

            final WebMarkupContainer collectionRvContainer = new UiHintPathSignificantWebMarkupContainer(collectionRv.newChildId());
            collectionRv.add(collectionRvContainer);
            
            addCollectionToForm(entityModel, association, collectionRvContainer);
        }
    }

    private void addCollectionToForm(final EntityModel entityModel,
            final ObjectAssociation association,
            final WebMarkupContainer collectionRvContainer) {

        final CssClassFacet facet = association.getFacet(CssClassFacet.class);
        if(facet != null) {
            final ObjectAdapter objectAdapter = entityModel.getObject();
            final String cssClass = facet.cssClass(objectAdapter);
            CssClassAppender.appendCssClassTo(collectionRvContainer, cssClass);
        }
        final WebMarkupContainer fieldset = new WebMarkupContainer(ID_COLLECTION_GROUP);
        collectionRvContainer.add(fieldset);

        final OneToManyAssociation otma = (OneToManyAssociation) association;

        final CollectionPanel collectionPanel = new CollectionPanel(ID_COLLECTION, entityModel, otma);
        fieldset.addOrReplace(collectionPanel);

        Label labelComponent = collectionPanel.createLabel(ID_COLLECTION_NAME, association.getName());
        final NamedFacet namedFacet = association.getFacet(NamedFacet.class);
        labelComponent.setEscapeModelStrings(namedFacet == null || namedFacet.escaped());

        final String description = association.getDescription();
        if(description != null) {
            labelComponent.add(new AttributeAppender("title", Model.of(description)));
        }

        fieldset.add(labelComponent);

        final EntityCollectionModel entityCollectionModel = collectionPanel.getModel();
        List<LinkAndLabel> links = entityCollectionModel.getLinks();

        AdditionalLinksPanel.addAdditionalLinks (fieldset,ID_ADDITIONAL_LINKS, links, AdditionalLinksPanel.Style.INLINE_LIST);

        final CollectionSelectorHelper selectorHelper = new CollectionSelectorHelper(entityCollectionModel, getComponentFactoryRegistry());

        final List<ComponentFactory> componentFactories = selectorHelper.getComponentFactories();

        if (componentFactories.size() <= 1) {
            permanentlyHide(ID_SELECTOR_DROPDOWN);
        } else {
            CollectionSelectorPanel selectorDropdownPanel;
            selectorDropdownPanel = new CollectionSelectorPanel(ID_SELECTOR_DROPDOWN, entityCollectionModel);

            final Model<ComponentFactory> componentFactoryModel = new Model<>();

            final int selected = selectorHelper.honourViewHintElseDefault(selectorDropdownPanel);

            ComponentFactory selectedComponentFactory = componentFactories.get(selected);
            componentFactoryModel.setObject(selectedComponentFactory);

            this.setOutputMarkupId(true);
            fieldset.addOrReplace(selectorDropdownPanel);

            collectionPanel.setSelectorDropdownPanel(selectorDropdownPanel);
        }

    }

    private List<ObjectAssociation> visibleCollections(final ObjectAdapter adapter, final ObjectSpecification noSpec) {
        return noSpec.getAssociations(Contributed.INCLUDED, visibleCollectionsFilter(adapter));
    }

    @SuppressWarnings("unchecked")
    private Filter<ObjectAssociation> visibleCollectionsFilter(final ObjectAdapter adapter) {
        return Filters.and(ObjectAssociation.Filters.COLLECTIONS, ObjectAssociation.Filters.dynamicallyVisible(getAuthenticationSession(), adapter, Where.PARENTED_TABLES));
    }

}
