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

import com.google.common.collect.Lists;

import org.apache.wicket.Component;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.basic.Label;

import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions.EntityActionUtil;
import org.apache.isis.viewer.wicket.ui.components.collection.selector.CollectionSelectorPanel;
import org.apache.isis.viewer.wicket.ui.components.collection.selector.CollectionSelectorProvider;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

/**
 * Panel for rendering entity collection; analogous to (any concrete subclass
 * of) {@link ScalarPanelAbstract}.
 */
public class CollectionPanel extends PanelAbstract<EntityCollectionModel> implements CollectionSelectorProvider {

    private static final long serialVersionUID = 1L;

    private static final String ID_FEEDBACK = "feedback";

    private Component collectionContents;

    private Label label;

    public CollectionPanel(
            final String id,
            final EntityCollectionModel collectionModel) {
        super(id, collectionModel);

        final List<LinkAndLabel> entityActionLinks = Lists.newArrayList();

        final OneToManyAssociation otma = collectionModel.getCollectionMemento().getCollection(collectionModel.getSpecificationLoader());
        final EntityModel entityModel = collectionModel.getEntityModel();

        final List<ObjectAction> associatedActions =
                EntityActionUtil.getObjectActionsForAssociation(entityModel, otma, getDeploymentCategory());

        entityActionLinks.addAll(
                EntityActionUtil.asLinkAndLabelsForAdditionalLinksPanel(entityModel, associatedActions));

        collectionModel.addEntityActions(entityActionLinks);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        buildGui();
    }

    private void buildGui() {
        collectionContents = getComponentFactoryRegistry().addOrReplaceComponent(this, ComponentType.COLLECTION_CONTENTS, getModel());

        addOrReplace(new NotificationPanel(ID_FEEDBACK, collectionContents, new ComponentFeedbackMessageFilter(collectionContents)));
    }

    public Label createLabel(final String id, final String collectionName) {
        this.label = new Label(id, collectionName);
        label.setOutputMarkupId(true);
        return this.label;
    }

    //region > SelectorDropdownPanel (impl)

    private CollectionSelectorPanel selectorDropdownPanel;

    @Override
    public CollectionSelectorPanel getSelectorDropdownPanel() {
        return selectorDropdownPanel;
    }
    public void setSelectorDropdownPanel(CollectionSelectorPanel selectorDropdownPanel) {
        this.selectorDropdownPanel = selectorDropdownPanel;
    }
    //endregion


}
