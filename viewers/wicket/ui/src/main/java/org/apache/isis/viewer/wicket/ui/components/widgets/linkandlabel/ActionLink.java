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
package org.apache.isis.viewer.wicket.ui.components.widgets.linkandlabel;

import java.util.Optional;

import org.apache.wicket.Application;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.extensions.ajax.markup.html.AjaxIndicatorAppender;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.request.cycle.RequestCycle;

import org.apache.isis.commons.internal.base._Either;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.commons.internal.debug._Probe.EntryPoint;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.core.security.authentication.logout.LogoutMenu.LoginRedirect;
import org.apache.isis.viewer.common.model.components.ComponentType;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettingsAccessor;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.ActionPromptProvider;
import org.apache.isis.viewer.wicket.model.models.ActionPromptWithExtraContent;
import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.model.util.CommonContextUtils;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.components.actions.ActionParametersPanel;
import org.apache.isis.viewer.wicket.ui.components.layout.bs3.BS3GridPanel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.ui.pages.entity.EntityPage;
import org.apache.isis.viewer.wicket.ui.panels.FormExecutorDefault;
import org.apache.isis.viewer.wicket.ui.panels.PanelUtil;
import org.apache.isis.viewer.wicket.ui.util.Wkt;

import static org.apache.isis.commons.internal.base._Casts.castTo;

import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons;
import lombok.NonNull;
import lombok.val;

/**
 *
 * @implNote <pre>
 * according to
 * http://wicketinaction.com/2011/11/implement-wicket-component-visibility-changes-properly/
 * and
 * http://apache-wicket.1842946.n4.nabble.com/vote-release-Wicket-1-4-14-td3056628.html#a3063795
 * should be using onConfigure rather than overloading.
 * eg:
 *  setVisible(determineIfVisible());
 *  setEnabled(determineIfEnabled());
 *
 * and no longer override isVisible() and isEnabled().
 *
 * however, in the case of a button already rendered as visible/enabled that (due to changes
 * elsewhere in the state of the server-side system) should then become invisible/disabled, it seems
 * that onConfigure isn't called and so the action continues to display the prompt.
 * A check is only made when hit OK of the prompt.  This is too late to display a message, so (until
 * figure out a better way) gonna continue to override isVisible() and isEnabled()
 *  </pre>
 */
public final class ActionLink
extends IndicatingAjaxLink<ManagedObject> {

    private static final long serialVersionUID = 1L;

    public static ActionLink create(
            final @NonNull String linkId,
            final @NonNull ActionModel actionModel) {

        val actionLink = new ActionLink(linkId, actionModel);
        return Wkt.cssAppend(actionLink, "noVeil");
    }

    private final AjaxIndicatorAppender indicatorAppenderIfAny;
    protected transient IsisAppCommonContext commonContext;

    private ActionLink(
            final String id,
            final ActionModel model) {
        super(id, model);
        this.commonContext = model.getCommonContext();

        final boolean useIndicatorForNoArgAction = getSettings().isUseIndicatorForNoArgAction();
        this.indicatorAppenderIfAny =
                useIndicatorForNoArgAction
                ? new AjaxIndicatorAppender()
                : null;

        if(this.indicatorAppenderIfAny != null) {
            this.add(this.indicatorAppenderIfAny);
        }
    }

    ActionModel getActionModel() {
        return (ActionModel) getModel();
    }

    public ObjectAction getObjectAction() {
        return getActionModel().getAction();
    }

    @Override
    protected void updateAjaxAttributes(final AjaxRequestAttributes attributes) {
        super.updateAjaxAttributes(attributes);
        if(getSettings().isPreventDoubleClickForNoArgAction()) {
            PanelUtil.disableBeforeReenableOnComplete(attributes, this);
        }

        // allow the event to bubble so the menu is hidden after click on an item
        attributes.setEventPropagation(AjaxRequestAttributes.EventPropagation.BUBBLE);
    }

    public String getReasonDisabledIfAny() {
        // no point evaluating if not visible
        return isVisible() ? getActionModel().getUsabilityConsent().getReason() : null;
    }

    @Override
    public boolean isVisible() {
        return getActionModel().getVisibilityConsent().isAllowed();
    }

    @Override
    public boolean isEnabled() {
        return getActionModel().getUsabilityConsent().isAllowed();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onComponentTag(final ComponentTag tag) {
        super.onComponentTag(tag);
        Buttons.fixDisabledState(this, tag);
    }

    @Override
    public String getAjaxIndicatorMarkupId() {
        return this.indicatorAppenderIfAny != null
                ? this.indicatorAppenderIfAny.getMarkupId()
                : null;
    }

    protected WicketViewerSettings getSettings() {
        return ((WicketViewerSettingsAccessor) Application.get()).getSettings();
    }

    @Override
    public void onClick(final AjaxRequestTarget target) {

        _Probe.entryPoint(EntryPoint.USER_INTERACTION, "Wicket Ajax Request, "
                + "originating from User clicking an Action Link.");

        val actionModel = this.getActionModel();

        if(actionModel.getInlinePromptContext() == null ||
                actionModel.getPromptStyle().isDialog()) {

            if(actionModel.hasParameters()) {
                startDialogWithParams(target);
            } else {
                executeWithoutParams();
            }

        } else {
            startDialogInline(target);
        }
    }

    private void startDialogWithParams(final AjaxRequestTarget target) {

        val actionModel = this.getActionModel();
        val actionLink = this;
        val promptProvider = ActionPromptProvider.getFrom(actionLink.getPage());
        val actionOwnerSpec = actionModel.getActionOwner().getSpecification();
        val actionPrompt = promptProvider.getActionPrompt(actionModel.getPromptStyle(), actionOwnerSpec.getBeanSort());

        val actionParametersPanel = (ActionParametersPanel)
                getComponentFactoryRegistry()
                .createComponent(
                        ComponentType.ACTION_PROMPT, actionPrompt.getContentId(), actionModel);

        actionParametersPanel.setShowHeader(false);

        val label = Wkt.label(actionPrompt.getTitleId(), actionModel::getFriendlyName);
        actionPrompt.setTitle(label, target);
        actionPrompt.setPanel(actionParametersPanel, target);
        actionPrompt.showPrompt(target);

        castTo(actionPrompt, ActionPromptWithExtraContent.class)
        .ifPresent(promptWithExtraContent->{
            BS3GridPanel.extraContentForMixin(promptWithExtraContent.getExtraContentId(), actionModel)
            .ifPresent(gridPanel->promptWithExtraContent.setExtraContentPanel(gridPanel, target));
        });
    }

    private void executeWithoutParams() {
        val actionModel = this.getActionModel();
        val actionLink = this;
        val page = actionLink.getPage();

        // returns true - if redirecting to new page, or repainting all components.
        // returns false - if invalid args; if concurrency exception;

        val formExecutor = new FormExecutorDefault(_Either.left(actionModel));
        val outcome = formExecutor.executeAndProcessResults(page, null, null, actionModel.isWithinPrompt());

        if(outcome.isSuccess()) {

            // intercept redirect request to sign-in page
            Optional.ofNullable(actionModel.getObject())
            .ifPresent(actionResultAdapter->{
                val actionResultObjectType = actionResultAdapter.getSpecification().getLogicalTypeName();
                if(LoginRedirect.LOGICAL_TYPE_NAME.equals(actionResultObjectType)) {
                    val commonContext = actionModel.getCommonContext();
                    val pageClassRegistry = commonContext.lookupServiceElseFail(PageClassRegistry.class);
                    val signInPage = pageClassRegistry.getPageClass(PageType.SIGN_IN);
                    RequestCycle.get().setResponsePage(signInPage);
                }
            });

            // else nothing to do

            //
            // the formExecutor will have either redirected, or scheduled a response,
            // or repainted components as required
            //

        } else {

            // render the target entity again
            //
            // (One way this can occur is if an event subscriber has a defect and throws an exception; in which case
            // the EventBus' exception handler will automatically veto.  This results in a growl message rather than
            // an error page, but is probably 'good enough').
            val targetAdapter = actionModel.getParentObject();
            val entityPage = EntityPage.ofAdapter(getCommonContext(), targetAdapter);
            getCommonContext().getTransactionService().flushTransaction();

            // "redirect-after-post"
            RequestCycle.get().setResponsePage(entityPage);
        }
    }

    private void startDialogInline(final AjaxRequestTarget target) {

        val actionModel = this.getActionModel();
        val inlinePromptContext = actionModel.getInlinePromptContext();
        val scalarTypeContainer = inlinePromptContext.getScalarTypeContainer();

        getComponentFactoryRegistry().addOrReplaceComponent(scalarTypeContainer,
                ScalarPanelAbstract.ID_SCALAR_IF_REGULAR_INLINE_PROMPT_FORM, ComponentType.PARAMETERS, actionModel);

        inlinePromptContext.getScalarIfRegular().setVisible(false);
        inlinePromptContext.getScalarIfRegularInlinePromptForm().setVisible(true);
        target.add(scalarTypeContainer);
    }

    // -- DEPENDENCIES

    public IsisAppCommonContext getCommonContext() {
        return commonContext = CommonContextUtils.computeIfAbsent(commonContext);
    }

    private ComponentFactoryRegistry getComponentFactoryRegistry() {
        return ((ComponentFactoryRegistryAccessor) Application.get()).getComponentFactoryRegistry();
    }

}
