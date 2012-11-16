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

package org.apache.isis.viewer.wicket.ui.components.collection;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.ComponentFeedbackPanel;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.filter.Filters;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.members.order.MemberOrderFacet;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionFilters;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionContainer.Contributed;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.entity.EntityActionLinkFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract;
import org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu.CssMenuBuilder;
import org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu.CssMenuLinkFactory;
import org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu.CssMenuPanel;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.Components;

import com.google.common.base.Strings;

/**
 * Panel for rendering entity collection; analogous to (any concrete subclass
 * of) {@link ScalarPanelAbstract}.
 */
public class CollectionPanel extends PanelAbstract<EntityCollectionModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_COLLECTION = "collection";
    private static final String ID_GROUPED_ACTIONS = "groupedActions";
    private static final String ID_FEEDBACK = "feedback";

    private final EntityModel entityModel;
    private final OneToManyAssociation otma;

    private static EntityCollectionModel createEntityCollectionModel(EntityModel entityModel, OneToManyAssociation otma) {
        return EntityCollectionModel.createParented(entityModel, otma);
    }

    public CollectionPanel(final String id, final EntityModel entityModel, OneToManyAssociation otma) {
        this(id, entityModel, otma, createEntityCollectionModel(entityModel, otma));
    }

    public CollectionPanel(String id, EntityCollectionModel collectionModel) {
        this(id, null, null, collectionModel);
    }

    private CollectionPanel(String id, EntityModel entityModel, OneToManyAssociation otma, EntityCollectionModel collectionModel) {
        super(id, collectionModel);
        this.entityModel = entityModel;
        this.otma = otma;

        buildGui();
    }

    private void buildGui() {

        final WebMarkupContainer markupContainer = new WebMarkupContainer(ID_COLLECTION);
        final Component collectionContents = getComponentFactoryRegistry().addOrReplaceComponent(markupContainer, ComponentType.COLLECTION_CONTENTS, getModel());

        buildEntityActionsGui();
        
        addOrReplace(new ComponentFeedbackPanel(ID_FEEDBACK, collectionContents));
        addOrReplace(markupContainer);

    }

    private void buildEntityActionsGui() {

        if (entityModel == null || otma == null) {
            Components.permanentlyHide(this, ID_GROUPED_ACTIONS);
            return;
        }

        final ObjectSpecification adapterSpec = entityModel.getTypeOfSpecification();
        final ObjectAdapter adapter = entityModel.getObject();
        final ObjectAdapterMemento adapterMemento = entityModel.getObjectAdapterMemento();
        
        @SuppressWarnings("unchecked")
        final List<ObjectAction> userActions = adapterSpec.getObjectActions(ActionType.USER, Contributed.INCLUDED,
                Filters.and(memberOrderOf(otma), dynamicallyVisibleFor(adapter)));

        if(!userActions.isEmpty()) {
            final CssMenuLinkFactory linkFactory = new EntityActionLinkFactory(entityModel);
            final CssMenuBuilder cssMenuBuilder = new CssMenuBuilder(adapterMemento, getServiceAdapters(), userActions, linkFactory);
            // TODO: i18n
            final CssMenuPanel groupedActions = cssMenuBuilder.buildPanel(ID_GROUPED_ACTIONS, "Actions");

            addOrReplace(groupedActions);
        } else {
            Components.permanentlyHide(this, ID_GROUPED_ACTIONS);
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
                return memberOrderName.equalsIgnoreCase(collectionName) || memberOrderName.equalsIgnoreCase(collectionId);
            }
        };
    }



    
}
