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
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.request.IRequestHandler;

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
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.components.actionprompt.ActionPromptHeaderPanel;
import org.apache.isis.viewer.wicket.ui.components.actions.ActionPanel;
import org.apache.isis.viewer.wicket.ui.components.actions.ActionParametersFormExecutor;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.panels.PanelUtil;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons;

public abstract class ActionLinkFactoryAbstract implements ActionLinkFactory {

    private static final long serialVersionUID = 1L;

    private final ScalarModel scalarModelForAssociationIfAny;

    protected ActionLinkFactoryAbstract(final ScalarModel scalarModelForAssociationIfAny) {
        this.scalarModelForAssociationIfAny = scalarModelForAssociationIfAny;
    }

    protected AbstractLink newLink(
            final String linkId,
            final ObjectAdapter objectAdapter,
            final ObjectAction action) {

        final ActionModel actionModel = ActionModel.create(objectAdapter, action);

        // this returns non-null if the action is no-arg and returns a URL or a Blob or a Clob.  Otherwise can use default handling
        // TODO: the method looks at the actual compile-time return type; cannot see a way to check at runtime what is returned.
        // TODO: see https://issues.apache.org/jira/browse/ISIS-1264 for further detail.
        final AjaxDeferredBehaviour ajaxDeferredBehaviour = determineDeferredBehaviour(action, actionModel);

        final AbstractLink link = getSettings().isUseIndicatorForNoArgAction()
                ? new IndicatingAjaxLink<Object>(linkId) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        ActionLinkFactoryAbstract.this.onClick(target, ajaxDeferredBehaviour, actionModel, this);
                    }

                    @Override
                    protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                        super.updateAjaxAttributes(attributes);
                        ActionLinkFactoryAbstract.this.updateAjaxAttributes(attributes, this);
                    }

                    @Override
                    protected void onComponentTag(ComponentTag tag) {
                        super.onComponentTag(tag);
                        Buttons.fixDisabledState(this, tag);
                    }
                } :
                new AjaxLink<Object>(linkId) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        ActionLinkFactoryAbstract.this.onClick(target, ajaxDeferredBehaviour, actionModel, this);
                    }

                    @Override
                    protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                        super.updateAjaxAttributes(attributes);
                        ActionLinkFactoryAbstract.this.updateAjaxAttributes(attributes, this);
                    }

                    @Override
                    protected void onComponentTag(ComponentTag tag) {
                        super.onComponentTag(tag);
                        Buttons.fixDisabledState(this, tag);
                    }
                };

        if (ajaxDeferredBehaviour != null) {
            link.add(ajaxDeferredBehaviour);
        }

        link.add(new CssClassAppender("noVeil"));

        return link;
    }

    private static AjaxDeferredBehaviour determineDeferredBehaviour(final ObjectAction action,
            final ActionModel actionModel) {
        // TODO: should unify with ActionResultResponseType (as used in ActionPanel)
        if (isNoArgReturnTypeRedirect(action)) {
            /**
             * adapted from:
             * 
             * @see https://cwiki.apache.org/confluence/display/WICKET/AJAX+update+and+file+download+in+one+blow
             */
            return new AjaxDeferredBehaviour(AjaxDeferredBehaviour.OpenUrlStrategy.NEW_WINDOW) {

                private static final long serialVersionUID = 1L;

                @Override
                protected IRequestHandler getRequestHandler() {
                    ObjectAdapter resultAdapter = actionModel.executeHandlingApplicationExceptions();
                    final Object value = resultAdapter.getObject();
                    return ActionModel.redirectHandler(value);
                }
            };
        }
        if (isNoArgReturnTypeDownload(action)) {

            /**
             * adapted from:
             * 
             * @see https://cwiki.apache.org/confluence/display/WICKET/AJAX+update+and+file+download+in+one+blow
             */
            return new AjaxDeferredBehaviour(AjaxDeferredBehaviour.OpenUrlStrategy.SAME_WINDOW) {

                private static final long serialVersionUID = 1L;

                @Override
                protected IRequestHandler getRequestHandler() {
                    final ObjectAdapter resultAdapter = actionModel.executeHandlingApplicationExceptions();
                    final Object value = resultAdapter.getObject();
                    return ActionModel.downloadHandler(value);
                }
            };
        }
        return null;
    }

    // TODO: should unify with ActionResultResponseType (as used in ActionPanel)
    private static boolean isNoArgReturnTypeRedirect(final ObjectAction action) {
        return action.getParameterCount() == 0 &&
                action.getReturnType() != null &&
                action.getReturnType().getCorrespondingClass() == java.net.URL.class;
    }

    // TODO: should unify with ActionResultResponseType (as used in ActionPanel)
    private static boolean isNoArgReturnTypeDownload(final ObjectAction action) {
        return action.getParameterCount() == 0 && action.getReturnType() != null &&
                (action.getReturnType().getCorrespondingClass() == org.apache.isis.applib.value.Blob.class ||
                action.getReturnType().getCorrespondingClass() == org.apache.isis.applib.value.Clob.class);
    }

    private void onClick(
            final AjaxRequestTarget target,
            final AjaxDeferredBehaviour ajaxDeferredBehaviourIfAny,
            final ActionModel actionModel,
            final AjaxLink<Object> ajaxLink) {

        if (ajaxDeferredBehaviourIfAny != null) {
            ajaxDeferredBehaviourIfAny.initiate(target);
            return;
        }



        ScalarModel.InlinePromptContext inlinePromptContext = determineInlinePromptContext();
        PromptStyle promptStyle = actionModel.getPromptStyle();

        if(inlinePromptContext == null || promptStyle != PromptStyle.INLINE) {
            final ActionPromptProvider promptProvider = ActionPromptProvider.Util.getFrom(ajaxLink.getPage());
            final ActionPrompt prompt = promptProvider.getActionPrompt();

            final ActionPromptHeaderPanel titlePanel =
                    PersistenceSession.ConcurrencyChecking.executeWithConcurrencyCheckingDisabled(
                            new Callable<ActionPromptHeaderPanel>() {
                                @Override
                                public ActionPromptHeaderPanel call() throws Exception {
                                    final String titleId = prompt.getTitleId();
                                    return new ActionPromptHeaderPanel(titleId, actionModel);
                                }
                            });
            final ActionPanel actionPanel =
                    (ActionPanel) getComponentFactoryRegistry().createComponent(
                            ComponentType.ACTION_PROMPT, prompt.getContentId(), actionModel);

            actionPanel.setShowHeader(false);

            prompt.setTitle(titlePanel, target);
            prompt.setPanel(actionPanel, target);
            actionPanel.setActionPrompt(prompt);
            prompt.showPrompt(target);

        } else {

            MarkupContainer scalarTypeContainer = inlinePromptContext.getScalarTypeContainer();
            actionModel.setFormExecutor(new ActionParametersFormExecutor(scalarTypeContainer, actionModel));
            actionModel.setInlinePromptContext(inlinePromptContext);
            getComponentFactoryRegistry().addOrReplaceComponent(scalarTypeContainer,
                    ScalarPanelAbstract.ID_SCALAR_IF_REGULAR_INLINE_PROMPT_FORM, ComponentType.PARAMETERS, actionModel);

            // TODO: probably needs to be like the switchXxx, and update the parent of the placeholder editform
            inlinePromptContext.getScalarIfRegular().setVisible(false);
            inlinePromptContext.getScalarIfRegularInlinePromptForm().setVisible(true);

            target.add(scalarTypeContainer);
        }

    }

    private void updateAjaxAttributes(
            final AjaxRequestAttributes attributes,
            final AjaxLink<Object> ajaxLink) {
        if(getSettings().isPreventDoubleClickForNoArgAction()) {
            PanelUtil.disableBeforeReenableOnComplete(attributes, ajaxLink);
        }

        // allow the event to bubble so the menu is hidden after click on an item
        attributes.setEventPropagation(AjaxRequestAttributes.EventPropagation.BUBBLE);
    }

    protected LinkAndLabel newLinkAndLabel(
            final ObjectAdapter objectAdapter,
            final ObjectAction objectAction,
            final AbstractLink link,
            final String disabledReasonIfAny) {

        final boolean whetherReturnsBlobOrClob = ObjectAction.Util.returnsBlobOrClob(objectAction);

        return LinkAndLabel.newLinkAndLabel(objectAdapter, objectAction, link, disabledReasonIfAny, whetherReturnsBlobOrClob);
    }

    private ScalarModel.InlinePromptContext determineInlinePromptContext() {
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
