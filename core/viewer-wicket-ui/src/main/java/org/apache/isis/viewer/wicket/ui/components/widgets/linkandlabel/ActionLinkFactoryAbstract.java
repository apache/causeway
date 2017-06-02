/* Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License. */

package org.apache.isis.viewer.wicket.ui.components.widgets.linkandlabel;

import java.util.concurrent.Callable;

import org.apache.wicket.Application;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.link.AbstractLink;

import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettingsAccessor;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.ActionPrompt;
import org.apache.isis.viewer.wicket.model.models.ActionPromptProvider;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.InlinePromptContext;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.components.actionprompt.ActionPromptHeaderPanel;
import org.apache.isis.viewer.wicket.ui.components.actions.ActionParametersFormExecutor;
import org.apache.isis.viewer.wicket.ui.components.actions.ActionParametersPanel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract2;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

public abstract class ActionLinkFactoryAbstract implements ActionLinkFactory {

    private static final long serialVersionUID = 1L;

    protected final EntityModel targetEntityModel;
    private final ScalarModel scalarModelForAssociationIfAny;

    protected ActionLinkFactoryAbstract(
            final EntityModel targetEntityModel,
            final ScalarModel scalarModelForAssociationIfAny) {
        this.targetEntityModel = targetEntityModel;
        this.scalarModelForAssociationIfAny = scalarModelForAssociationIfAny;
    }

    protected ActionLink newLink(
            final String linkId,
            final ObjectAction action) {

        final ActionModel actionModel = ActionModel.create(this.targetEntityModel, action);

        final ActionLink link =
                new ActionLink(linkId, actionModel, action) {
                    private static final long serialVersionUID = 1L;

                    protected void doOnClick(AjaxRequestTarget target) {

                        ActionLinkFactoryAbstract.this.onClick(this, target);
                    }

                };

        link.add(new CssClassAppender("noVeil"));
        return link;
    }


    private void onClick(
            final ActionLink actionLink,
            final AjaxRequestTarget target) {

        final ActionModel actionModel = actionLink.getActionModel();

        InlinePromptContext inlinePromptContext = determineInlinePromptContext();
        PromptStyle promptStyle = actionModel.getPromptStyle();

        if(inlinePromptContext == null || promptStyle.isDialog()) {
            final ActionPromptProvider promptProvider = ActionPromptProvider.Util.getFrom(actionLink.getPage());
            final ActionPrompt prompt = promptProvider.getActionPrompt();

            // REVIEW: I wonder if this is still needed after the ISIS-1613 rework?
            final ActionPromptHeaderPanel titlePanel =
                    PersistenceSession.ConcurrencyChecking.executeWithConcurrencyCheckingDisabled(
                            new Callable<ActionPromptHeaderPanel>() {
                                @Override
                                public ActionPromptHeaderPanel call() throws Exception {
                                    final String titleId = prompt.getTitleId();
                                    return new ActionPromptHeaderPanel(titleId, actionModel);
                                }
                            });

            final ActionParametersPanel actionParametersPanel =
                    (ActionParametersPanel) getComponentFactoryRegistry().createComponent(
                            ComponentType.ACTION_PROMPT, prompt.getContentId(), actionModel);

            actionParametersPanel.setShowHeader(false);

            prompt.setTitle(titlePanel, target);
            prompt.setPanel(actionParametersPanel, target);
            actionParametersPanel.setActionPrompt(prompt);
            prompt.showPrompt(target);

        } else {

            MarkupContainer scalarTypeContainer = inlinePromptContext.getScalarTypeContainer();
            actionModel.setFormExecutor(new ActionParametersFormExecutor(/*scalarTypeContainer, */actionModel));
            actionModel.setInlinePromptContext(inlinePromptContext);
            getComponentFactoryRegistry().addOrReplaceComponent(scalarTypeContainer,
                    ScalarPanelAbstract2.ID_SCALAR_IF_REGULAR_INLINE_PROMPT_FORM, ComponentType.PARAMETERS, actionModel);

            inlinePromptContext.getScalarIfRegular().setVisible(false);
            inlinePromptContext.getScalarIfRegularInlinePromptForm().setVisible(true);

            target.add(scalarTypeContainer);
        }
    }


    protected LinkAndLabel newLinkAndLabel(
            final ObjectAdapter objectAdapter,
            final ObjectAction objectAction,
            final AbstractLink link,
            final String disabledReasonIfAny) {

        final boolean whetherReturnsBlobOrClob = ObjectAction.Util.returnsBlobOrClob(objectAction);

        return LinkAndLabel.newLinkAndLabel(objectAdapter, objectAction, link, disabledReasonIfAny, whetherReturnsBlobOrClob);
    }

    private InlinePromptContext determineInlinePromptContext() {
        return scalarModelForAssociationIfAny != null
                ? scalarModelForAssociationIfAny.getInlinePromptContext()
                : null;
    }


    //region > dependencies

    protected ComponentFactoryRegistry getComponentFactoryRegistry() {
        return ((ComponentFactoryRegistryAccessor) Application.get()).getComponentFactoryRegistry();
    }

    protected PageClassRegistry getPageClassRegistry() {
        return ((PageClassRegistryAccessor) Application.get()).getPageClassRegistry();
    }

    protected WicketViewerSettings getSettings() {
        return ((WicketViewerSettingsAccessor)Application.get()).getSettings();
    }

    //endregion

}
