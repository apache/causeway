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

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.basic.Label;

import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.models.ActionPromptProvider;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.actionprompt.ActionPromptModalWindow;
import org.apache.isis.viewer.wicket.ui.components.additionallinks.EntityActionUtil;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.selector.dropdown.CollectionContentsSelectorDropdownPanel;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.selector.dropdown.HasSelectorDropdownPanel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

/**
 * Panel for rendering entity collection; analogous to (any concrete subclass
 * of) {@link ScalarPanelAbstract}.
 */
public class CollectionPanel extends PanelAbstract<EntityCollectionModel> implements ActionPromptProvider, HasSelectorDropdownPanel {


    private static final long serialVersionUID = 1L;

    private static final String ID_FEEDBACK = "feedback";
    private static final String ID_ACTION_PROMPT_MODAL_WINDOW = "actionPromptModalWindow";

    private Component collectionContents;

    private String collectionName;
    private Label label;

    public CollectionPanel(final String id, final EntityModel entityModel, OneToManyAssociation otma) {
        this(id, newEntityCollectionModel(entityModel, otma), entityModel, otma);
    }

    private static EntityCollectionModel newEntityCollectionModel(final EntityModel entityModel, OneToManyAssociation otma) {
        EntityCollectionModel collectionModel = EntityCollectionModel.createParented(entityModel, otma);
        return collectionModel;
    }

    CollectionPanel(String id, EntityCollectionModel collectionModel) {
        this(id, collectionModel, new EntityModel(collectionModel.getParentObjectAdapterMemento()), collectionModel.getCollectionMemento().getCollection());
    }

    CollectionPanel(final String id, final EntityCollectionModel collectionModel, final EntityModel entityModel, final OneToManyAssociation otma) {
        super(id, collectionModel);

        addActionPromptModalWindow();
        
        List<LinkAndLabel> entityActions = EntityActionUtil.entityActionsForAssociation(entityModel, otma, this, getDeploymentType(), "additionalLink");
        collectionModel.addEntityActions(entityActions);
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
        this.collectionName = collectionName;
        this.label = new Label(id, collectionName);
    	label.setOutputMarkupId(true);
    	return this.label;
    }

    public void updateLabel(AjaxRequestTarget target) {
        target.add(label);
    }

    /**
     * Returns true if a collection count is available from the rendered component 
     * (ie an eagerly rendered/expanded view).
     */
    public boolean hasCount() {
        if(label == null) {
            return false;
        }
        final Integer count = getCount();
        label.setDefaultModelObject(collectionName);
        return count != null;
    }

    private Integer getCount() {
        if(collectionContents instanceof CollectionCountProvider) {
            final CollectionCountProvider collectionCountProvider = (CollectionCountProvider) collectionContents;
            return collectionCountProvider.getCount();
        } else {
            return null;
        }
    }

    
    // ///////////////////////////////////////////////////////////////////
    // ActionPromptModalWindowProvider
    // ///////////////////////////////////////////////////////////////////

    private ActionPromptModalWindow actionPromptModalWindow;
    public ActionPromptModalWindow getActionPrompt() {
        return ActionPromptModalWindow.getActionPromptModalWindowIfEnabled(actionPromptModalWindow);
    }

    private void addActionPromptModalWindow() {
        this.actionPromptModalWindow = ActionPromptModalWindow.newModalWindow(ID_ACTION_PROMPT_MODAL_WINDOW);
        addOrReplace(actionPromptModalWindow);
    }


    // ///////////////////////////////////////////////////////////////////

    protected DeploymentType getDeploymentType() {
        return IsisContext.getDeploymentType();
    }


    //region > SelectorDropdownPanel (impl)

    private CollectionContentsSelectorDropdownPanel selectorDropdownPanel;

    @Override
    public CollectionContentsSelectorDropdownPanel getSelectorDropdownPanel() {
        return selectorDropdownPanel;
    }
    public void setSelectorDropdownPanel(CollectionContentsSelectorDropdownPanel selectorDropdownPanel) {
        this.selectorDropdownPanel = selectorDropdownPanel;
    }
    //endregion

}
