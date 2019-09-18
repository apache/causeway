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

package org.apache.isis.viewer.wicket.ui.components.actions;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;

import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.ActionPrompt;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.actionresponse.ActionResultResponse;
import org.apache.isis.viewer.wicket.ui.actionresponse.ActionResultResponseType;
import org.apache.isis.viewer.wicket.ui.components.property.PropertyEditPanel;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

/**
 * {@link PanelAbstract Panel} representing an action invocation, backed by an
 * {@link ActionModel}.
 *
 * <p>
 * Based on the {@link ActionModel.Mode mode}, will render either parameter
 * dialog or the results.
 *
 * <p>
 * Corresponding component to edit properties is {@link PropertyEditPanel}.
 */
public class ActionParametersPanel extends PanelAbstract<ActionModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_HEADER = "header";
    private static final String ID_ACTION_NAME = "actionName";

    public ActionParametersPanel(final String id, final ActionModel actionModel) {
        super(id, actionModel);
    }

    ActionModel getActionModel() {
        return super.getModel();
    }

    /**
     * Sets the owning action prompt (modal window), if any.
     *
     * REVIEW: I wonder if this is necessary... there isn't anything exactly the same for property edits...
     */
    public void setActionPrompt(ActionPrompt actionPrompt) {
        ActionFormExecutorStrategy formExecutor = new ActionFormExecutorStrategy(getActionModel());
        formExecutor.setActionPrompt(actionPrompt);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        final ActionModel actionModel = getModel();

        if (!actionModel.hasParameters()) {
            // the factory should check for this already, so this should never occur...
            throw new IllegalStateException("model has no parameters!");
        }


        WebMarkupContainer header = new WebMarkupContainer(ID_HEADER) {
            private static final long serialVersionUID = 5410724436024228792L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(showHeader);
            }
        };

        addOrReplace(header);

        ObjectAdapter targetAdapter = null;
        try {
            targetAdapter = actionModel.getTargetAdapter();

            getComponentFactoryRegistry().addOrReplaceComponent(this, ComponentType.PARAMETERS, getActionModel());
            getComponentFactoryRegistry().addOrReplaceComponent(header, ComponentType.ENTITY_ICON_AND_TITLE, actionModel
                    .getParentEntityModel());

            final String actionName = getActionModel().getActionMemento().getAction(actionModel.getSpecificationLoader()).getName();
            header.add(new Label(ID_ACTION_NAME, Model.of(actionName)));

        } catch (final ConcurrencyException ex) {

            // second attempt should succeed, because the Oid would have
            // been updated in the attempt
            if (targetAdapter == null) {
                targetAdapter = getModel().getTargetAdapter();
            }

            // forward onto the target page with the concurrency exception
            ActionResultResponse resultResponse = ActionResultResponseType.OBJECT.interpretResult(this.getActionModel(), targetAdapter, ex);
            resultResponse.getHandlingStrategy().handleResults(resultResponse, getIsisSessionFactory());

            final MessageService messageService = getServiceRegistry().lookupServiceElseFail(MessageService.class);
            messageService.warnUser(ex.getMessage());
        }
    }


    /**
     * Gives a chance to hide the header part of this action panel,
     * e.g. when shown in an action prompt
     */
    private boolean showHeader = true;

    public void setShowHeader(boolean showHeader) {
        this.showHeader = showHeader;
    }

}
