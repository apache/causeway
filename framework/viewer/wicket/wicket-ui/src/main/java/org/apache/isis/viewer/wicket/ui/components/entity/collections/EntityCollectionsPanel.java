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

import com.google.common.base.Strings;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.repeater.RepeatingView;

import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.filter.Filters;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.facets.members.order.MemberOrderFacet;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionFilters;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociationFilters;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionContainer.Contributed;
import org.apache.isis.viewer.wicket.model.mementos.ActionMemento;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.collection.CollectionPanel;
import org.apache.isis.viewer.wicket.ui.components.entity.EntityActionLinkFactory;
import org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu.CssMenuBuilder;
import org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu.CssMenuLinkFactory;
import org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu.CssMenuPanel;
import org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu.CssMenuLinkFactory.LinkAndLabel;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.Components;
import org.apache.isis.viewer.wicket.ui.util.EvenOrOddCssClassAppenderFactory;

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

    private static final String ID_GROUPED_ACTIONS = "groupedActions";

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
        final EntityModel entityModel = (EntityModel) getModel();
        final ObjectAdapter adapter = entityModel.getObject();
        final ObjectSpecification noSpec = adapter.getSpecification();

        final List<ObjectAssociation> associations = visibleAssociations(adapter, noSpec);

        final RepeatingView collectionRv = new RepeatingView(ID_COLLECTIONS);
        final EvenOrOddCssClassAppenderFactory eo = new EvenOrOddCssClassAppenderFactory();
        add(collectionRv);

        for (final ObjectAssociation association : associations) {

            final WebMarkupContainer collectionRvContainer = new WebMarkupContainer(collectionRv.newChildId());
            collectionRv.add(collectionRvContainer);
            collectionRvContainer.add(eo.nextClass());
            
            addCollectionToForm(entityModel, association, collectionRvContainer);
        }
    }

    private void addCollectionToForm(final EntityModel entityModel,
			final ObjectAssociation association,
			final WebMarkupContainer collectionRvContainer) {
	    
        final WebMarkupContainer fieldset = new WebMarkupContainer(ID_COLLECTION_GROUP);
        collectionRvContainer.add(fieldset);
        
        final String name = association.getName();
        fieldset.add(new Label(ID_COLLECTION_NAME, name));

        buildEntityActionsGui(fieldset, association);

		final OneToManyAssociation otma = (OneToManyAssociation) association;

		final EntityCollectionModel entityCollectionModel = EntityCollectionModel.createParented(entityModel, otma);
		final CollectionPanel collectionPanel = new CollectionPanel(ID_COLLECTION, entityCollectionModel);
		fieldset.addOrReplace(collectionPanel);

		getComponentFactoryRegistry().addOrReplaceComponent(fieldset, ID_COLLECTION, ComponentType.COLLECTION_NAME_AND_CONTENTS, entityCollectionModel);
	}

    private void buildEntityActionsGui(WebMarkupContainer collectionRvContainer, ObjectAssociation association) {
        
        final EntityModel model = getModel();
        final ObjectSpecification adapterSpec = model.getTypeOfSpecification();
        final ObjectAdapter adapter = model.getObject();
        final ObjectAdapterMemento adapterMemento = model.getObjectAdapterMemento();
        
        @SuppressWarnings("unchecked")
        final List<ObjectAction> userActions = adapterSpec.getObjectActions(ActionType.USER, Contributed.INCLUDED,
                Filters.and(memberOrderOf(association), dynamicallyVisibleFor(adapter)));

        final CssMenuLinkFactory linkFactory = new EntityActionLinkFactory(getEntityModel());

        if(!userActions.isEmpty()) {
            final CssMenuBuilder cssMenuBuilder = new CssMenuBuilder(adapterMemento, getServiceAdapters(), userActions, linkFactory);
            // TODO: i18n
            final CssMenuPanel cssMenuPanel = cssMenuBuilder.buildPanel(ID_GROUPED_ACTIONS, "Actions");

            collectionRvContainer.addOrReplace(cssMenuPanel);
        } else {
            Components.permanentlyHide(collectionRvContainer, ID_GROUPED_ACTIONS);
        }
    }

    private Filter<ObjectAction> dynamicallyVisibleFor(final ObjectAdapter adapter) {
        return ObjectActionFilters.dynamicallyVisible(getAuthenticationSession(), adapter, Where.ANYWHERE);
    }

    private Filter<ObjectAction> memberOrderOf(ObjectAssociation association) {
        final String collectionName = association.getName();
        final String collectionId = association.getId();
        return new Filter<ObjectAction>() {

            @Override
            public boolean accept(ObjectAction t) {
                final MemberOrderFacet memberOrderFacet = t.getFacet(MemberOrderFacet.class);
                if(memberOrderFacet == null) {
                    return false; 
                }
                final String memberOrderName = memberOrderFacet.name();
                if(Strings.isNullOrEmpty(memberOrderName)) {
                    return false;
                }
                return memberOrderName.equals(collectionName) || memberOrderName.equals(collectionId);
            }
        };
    }


    private List<ObjectAssociation> visibleAssociations(final ObjectAdapter adapter, final ObjectSpecification noSpec) {
        return noSpec.getAssociations(visibleAssociationFilter(adapter));
    }

    @SuppressWarnings("unchecked")
	private Filter<ObjectAssociation> visibleAssociationFilter(final ObjectAdapter adapter) {
        return Filters.and(ObjectAssociationFilters.COLLECTIONS, ObjectAssociationFilters.dynamicallyVisible(getAuthenticationSession(), adapter, Where.PARENTED_TABLES));
    }

    private void requestRepaintPanel(final AjaxRequestTarget target) {
        if (target != null) {
            target.add(this);
        }
    }

    private EntityModel getEntityModel() {
        return (EntityModel) getModel();
    }

    void toViewMode(final AjaxRequestTarget target) {
        getEntityModel().toViewMode();
        requestRepaintPanel(target);
    }

}
